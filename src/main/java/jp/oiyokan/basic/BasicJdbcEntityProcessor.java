/*
 * Copyright 2021 Toshiki Iga
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.oiyokan.basic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanCsdlEntitySet;
import jp.oiyokan.OiyokanEdmProvider;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.basic.sql.BasicSqlInfo;
import jp.oiyokan.settings.OiyokanNamingUtil;

public class BasicJdbcEntityProcessor {
    private BasicSqlInfo sqlInfo;

    public Entity readEntityData(UriInfo uriInfo, EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates)
            throws ODataApplicationException {
        OiyokanEdmProvider provider = new OiyokanEdmProvider();
        if (!edmEntitySet.getEntityContainer().getName().equals(provider.getEntityContainer().getName())) {
            // Container 名が不一致. 処理せずに戻します.
            // TODO FIXME 例外.
            return null;
        }

        OiyokanCsdlEntitySet entitySet = null;
        for (CsdlEntitySet look : provider.getEntityContainer().getEntitySets()) {
            if (edmEntitySet.getName().equals(look.getName())) {
                entitySet = (OiyokanCsdlEntitySet) look;
                break;
            }
        }
        if (entitySet == null) {
            // TODO FIXME 例外.
            return null;
        }

        sqlInfo = new BasicSqlInfo(entitySet);
        getSelectOneQuery(edmEntitySet, keyPredicates);

        // データベースに接続.
        try (Connection connTargetDb = BasicJdbcUtil.getConnection(entitySet.getSettingsDatabase())) {
            final String sql = sqlInfo.getSqlBuilder().toString();
            if (OiyokanConstants.IS_TRACE_ODATA_V4)
                System.err.println("OData v4: TRACE: SQL one: " + sql);

            final long startMillisec = System.currentTimeMillis();
            try (var stmt = connTargetDb.prepareStatement(sql)) {
                // set query timeout
                stmt.setQueryTimeout(OiyokanConstants.JDBC_STMT_TIMEOUT);

                int idxColumn = 1;
                for (Object look : sqlInfo.getSqlParamList()) {
                    // System.err.println("TRACE: param: " + look.toString());
                    BasicJdbcUtil.bindPreparedParameter(stmt, idxColumn++, look);
                }

                stmt.executeQuery();
                var rset = stmt.getResultSet();
                ResultSetMetaData rsmeta = null;
                for (; rset.next();) {
                    if (rsmeta == null) {
                        rsmeta = rset.getMetaData();
                    }
                    final Entity ent = new Entity();
                    for (int column = 1; column <= rsmeta.getColumnCount(); column++) {
                        Property prop = BasicJdbcUtil.resultSet2Property(rset, rsmeta, column, entitySet);
                        ent.addProperty(prop);
                    }

                    final long endMillisec = System.currentTimeMillis();
                    if (OiyokanConstants.IS_TRACE_ODATA_V4) {
                        final long elapsed = endMillisec - startMillisec;
                        if (elapsed >= 10) {
                            System.err.println("OData v4: TRACE: SQL: elapsed: " + (endMillisec - startMillisec));
                        }
                    }

                    return ent;
                }
            } catch (SQLTimeoutException ex) {
                // TODO FIXME メッセージ番号取り直し
                // [M036] SQL timeout at execute
                System.err.println(OiyokanMessages.M036 + ": " + sql + ", " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.M036 + ": " + sql, 500, Locale.ENGLISH);
            } catch (SQLException ex) {
                // TODO FIXME メッセージ番号取り直し
                // [M017] Fail to execute SQL
                System.err.println(OiyokanMessages.M017 + ": " + sql + ", " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.M017 + ": " + sql, 500, Locale.ENGLISH);
            }

        } catch (SQLException ex) {
            // TODO メッセージ処理

            // TODO FIXME メッセージ番号取り直し
            // [M999] NOT IMPLEMENTED: Generic NOT implemented message.
            System.err.println(OiyokanMessages.M999 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M999, 500, Locale.ENGLISH);
        }

        // TODO FIXME メッセージ番号取り直し
        // [M999] NOT IMPLEMENTED: Generic NOT implemented message.
        System.err.println(OiyokanMessages.M999 + ": 終端に到達");
        throw new ODataApplicationException(OiyokanMessages.M999, 500, Locale.ENGLISH);
    }

    /**
     * 1件の検索用のSQLを生成.
     * 
     * @param uriInfo URI情報.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    private void getSelectOneQuery(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates)
            throws ODataApplicationException {
        sqlInfo.getSqlBuilder().append("SELECT ");

        expandSelectKey(edmEntitySet);

        // TODO FIXME テーブル名の空白を含むパターンの対応.
        sqlInfo.getSqlBuilder().append(" FROM " + sqlInfo.getEntitySet().getDbTableNameTargetIyo());

        sqlInfo.getSqlBuilder().append(" WHERE ");
        boolean isFirst = true;
        for (UriParameter param : keyPredicates) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(" AND ");
            }
            sqlInfo.getSqlBuilder().append(param.getName());
            sqlInfo.getSqlBuilder().append(" = ");

            CsdlProperty csdlProp = sqlInfo.getEntitySet().getEntityType().getProperty(param.getName());
            BasicJdbcUtil.buildLiteralOrPlaceholder(sqlInfo, csdlProp.getType(), param.getText());
        }
    }

    private void expandSelectKey(EdmEntitySet edmEntitySet) throws ODataApplicationException {
        int itemCount = 0;
        for (String name : edmEntitySet.getEntityType().getPropertyNames()) {
            sqlInfo.getSqlBuilder().append(itemCount++ == 0 ? "" : ",");
            sqlInfo.getSqlBuilder().append(BasicJdbcUtil.escapeKakkoFieldName(sqlInfo,
                    OiyokanNamingUtil.entity2Db(BasicJdbcUtil.unescapeKakkoFieldName(name))));
        }
    }

    /////////////////////////
    // INSERT

    public Entity createEntityData(UriInfo uriInfo, EdmEntitySet edmEntitySet, Entity requestEntity)
            throws ODataApplicationException {
        OiyokanEdmProvider provider = new OiyokanEdmProvider();
        if (!edmEntitySet.getEntityContainer().getName().equals(provider.getEntityContainer().getName())) {
            // Container 名が不一致. 処理せずに戻します.
            // TODO FIXME 例外.
            return null;
        }

        OiyokanCsdlEntitySet entitySet = null;
        for (CsdlEntitySet look : provider.getEntityContainer().getEntitySets()) {
            if (edmEntitySet.getName().equals(look.getName())) {
                entitySet = (OiyokanCsdlEntitySet) look;
                break;
            }
        }
        if (entitySet == null) {
            // TODO FIXME 例外.
            return null;
        }

        sqlInfo = new BasicSqlInfo(entitySet);
        getInsertIntoDml(edmEntitySet, requestEntity);

        // TODO FIXME 戻り値を受け取ること。
        executeDml();

        // TODO FIXME 戻り値を反映させること。
        final List<UriParameter> keyPredicates = new ArrayList<>();
        for (Property prop : requestEntity.getProperties()) {
            UriParameter newParam = new UriParameter() {
                @Override
                public String getAlias() {
                    return null;
                }

                @Override
                public String getText() {
                    return prop.getValue().toString();
                }

                @Override
                public Expression getExpression() {
                    return null;
                }

                @Override
                public String getName() {
                    return prop.getName();
                }

                @Override
                public String getReferencedProperty() {
                    return null;
                }
            };
            keyPredicates.add(newParam);
        }

        return readEntityData(uriInfo, edmEntitySet, keyPredicates);
    }

    private void getInsertIntoDml(EdmEntitySet edmEntitySet, Entity requestEntity) throws ODataApplicationException {
        sqlInfo.getSqlBuilder().append("INSERT INTO ");
        // TODO FIXME テーブル名の空白を含むパターンの対応.
        sqlInfo.getSqlBuilder().append(sqlInfo.getEntitySet().getDbTableNameTargetIyo());
        sqlInfo.getSqlBuilder().append(" (");
        boolean isFirst = true;
        for (Property prop : requestEntity.getProperties()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }
            sqlInfo.getSqlBuilder().append(prop.getName());
        }

        sqlInfo.getSqlBuilder().append(") VALUES (");
        isFirst = true;
        for (Property prop : requestEntity.getProperties()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }
            sqlInfo.getSqlBuilder().append("?");
            sqlInfo.getSqlParamList().add(prop.getValue());
        }

        sqlInfo.getSqlBuilder().append(")");
    }

    ////////////////////////////
    // EXECUTE DML

    /**
     * TODO FIXME 自動採集番された項目の値をreturnすること。
     * 
     * @throws ODataApplicationException
     */
    private void executeDml() throws ODataApplicationException {
        // データベースに接続.
        try (Connection connTargetDb = BasicJdbcUtil.getConnection(sqlInfo.getEntitySet().getSettingsDatabase())) {
            final String sql = sqlInfo.getSqlBuilder().toString();
            if (OiyokanConstants.IS_TRACE_ODATA_V4)
                System.err.println("OData v4: TRACE: SQL exec: " + sql);

            final long startMillisec = System.currentTimeMillis();
            try (var stmt = connTargetDb.prepareStatement(sql)) {
                // set query timeout
                stmt.setQueryTimeout(OiyokanConstants.JDBC_STMT_TIMEOUT);

                int idxColumn = 1;
                for (Object look : sqlInfo.getSqlParamList()) {
                    // System.err.println("TRACE: param: " + look.toString());
                    BasicJdbcUtil.bindPreparedParameter(stmt, idxColumn++, look);
                }

                final int result = stmt.executeUpdate();
                if (result != 1) {
                    // TODO FIXME メッセージ番号取り直し
                    System.err.println(OiyokanMessages.M036 + ": " + sql);
                    throw new ODataApplicationException(OiyokanMessages.M036 + ": " + sql, 500, Locale.ENGLISH);
                }

                // 生成されたキーがあればそれを採用。
                final ResultSet rsKeys = stmt.getGeneratedKeys();
                if (rsKeys.next()) {
                    final ResultSetMetaData rsmetaKeys = rsKeys.getMetaData();
                    for (int column = 1; column <= rsmetaKeys.getColumnCount(); column++) {
                        System.out.println(rsKeys.getInt(column));

                        // TODO FIXME メッセージ番号取り直し
                        // [M999] NOT IMPLEMENTED: Generic NOT implemented message.
                        System.err.println(OiyokanMessages.M999);
                        throw new ODataApplicationException(OiyokanMessages.M999, 500, Locale.ENGLISH);
                    }
                }

                final long endMillisec = System.currentTimeMillis();
                if (OiyokanConstants.IS_TRACE_ODATA_V4) {
                    final long elapsed = endMillisec - startMillisec;
                    if (elapsed >= 10) {
                        System.err.println("OData v4: TRACE: SQL: elapsed: " + (endMillisec - startMillisec));
                    }
                }
            } catch (SQLIntegrityConstraintViolationException ex) {
                // [M038] Integrity constraint violation occured. 一位制約違反.
                System.err.println(OiyokanMessages.M038 + ": " + sql + ", " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.M038 + ": " + sql, 500, Locale.ENGLISH);
            } catch (SQLTimeoutException ex) {
                // TODO FIXME メッセージ番号取り直し
                // [M036] SQL timeout at execute
                System.err.println(OiyokanMessages.M036 + ": " + sql + ", " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.M036 + ": " + sql, 500, Locale.ENGLISH);
            } catch (SQLException ex) {
                // TODO FIXME メッセージ番号取り直し
                // [M017] Fail to execute SQL
                System.err.println(OiyokanMessages.M017 + ": " + sql + ", " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.M017 + ": " + sql, 500, Locale.ENGLISH);
            }

        } catch (SQLException ex) {
            // TODO メッセージ処理

            // TODO FIXME メッセージ番号取り直し
            // [M999] NOT IMPLEMENTED: Generic NOT implemented message.
            System.err.println(OiyokanMessages.M999 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M999, 500, Locale.ENGLISH);
        }
    }

    ////////////////////////
    // DELETE

    public void deleteEntityData(UriInfo uriInfo, EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates)
            throws ODataApplicationException {
        OiyokanEdmProvider provider = new OiyokanEdmProvider();
        if (!edmEntitySet.getEntityContainer().getName().equals(provider.getEntityContainer().getName())) {
            // Container 名が不一致. 処理せずに戻します.
            // TODO FIXME 例外.
            return;
        }

        OiyokanCsdlEntitySet entitySet = null;
        for (CsdlEntitySet look : provider.getEntityContainer().getEntitySets()) {
            if (edmEntitySet.getName().equals(look.getName())) {
                entitySet = (OiyokanCsdlEntitySet) look;
                break;
            }
        }
        if (entitySet == null) {
            // TODO FIXME 例外.
            return;
        }

        sqlInfo = new BasicSqlInfo(entitySet);
        getDeleteDml(edmEntitySet, keyPredicates);
        executeDml();
    }

    private void getDeleteDml(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates)
            throws ODataApplicationException {
        sqlInfo.getSqlBuilder().append("DELETE FROM ");
        // TODO FIXME テーブル名の空白を含むパターンの対応.
        sqlInfo.getSqlBuilder().append(sqlInfo.getEntitySet().getDbTableNameTargetIyo());
        sqlInfo.getSqlBuilder().append(" WHERE ");
        boolean isFirst = true;

        for (UriParameter param : keyPredicates) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(" AND ");
            }

            CsdlProperty csdlProp = sqlInfo.getEntitySet().getEntityType().getProperty(param.getName());

            // TODO 項目名の変形対応。
            sqlInfo.getSqlBuilder().append(csdlProp.getName());
            sqlInfo.getSqlBuilder().append(" = ");
            BasicJdbcUtil.buildLiteralOrPlaceholder(sqlInfo, csdlProp.getType(), param.getText());
        }
    }

    ////////////////////////
    // UPDATE

    public void updateEntityDataPatch(UriInfo uriInfo, EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates,
            Entity requestEntity) throws ODataApplicationException {
        OiyokanEdmProvider provider = new OiyokanEdmProvider();
        if (!edmEntitySet.getEntityContainer().getName().equals(provider.getEntityContainer().getName())) {
            // Container 名が不一致. 処理せずに戻します.
            // TODO FIXME 例外.
            return;
        }

        OiyokanCsdlEntitySet entitySet = null;
        for (CsdlEntitySet look : provider.getEntityContainer().getEntitySets()) {
            if (edmEntitySet.getName().equals(look.getName())) {
                entitySet = (OiyokanCsdlEntitySet) look;
                break;
            }
        }
        if (entitySet == null) {
            // TODO FIXME 例外.
            return;
        }

        sqlInfo = new BasicSqlInfo(entitySet);
        getUpdatePatchDml(edmEntitySet, keyPredicates, requestEntity);
        executeDml();
    }

    private void getUpdatePatchDml(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates, Entity requestEntity)
            throws ODataApplicationException {
        sqlInfo.getSqlBuilder().append("UPDATE ");
        // TODO FIXME テーブル名の空白を含むパターンの対応.
        sqlInfo.getSqlBuilder().append(sqlInfo.getEntitySet().getDbTableNameTargetIyo());
        sqlInfo.getSqlBuilder().append(" SET ");
        boolean isFirst = true;
        for (Property prop : requestEntity.getProperties()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }

            sqlInfo.getSqlBuilder().append(prop.getName());
            sqlInfo.getSqlBuilder().append(" = ");

            BasicJdbcUtil.buildLiteralOrPlaceholder(sqlInfo, prop.getType(), prop.getValue().toString());
        }

        sqlInfo.getSqlBuilder().append(" WHERE ");

        isFirst = true;
        for (UriParameter param : keyPredicates) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(" AND ");
            }
            sqlInfo.getSqlBuilder().append(param.getName());
            sqlInfo.getSqlBuilder().append(" = ");

            CsdlProperty csdlProp = sqlInfo.getEntitySet().getEntityType().getProperty(param.getName());
            BasicJdbcUtil.buildLiteralOrPlaceholder(sqlInfo, csdlProp.getType(), param.getText());
        }
    }

    public void updateEntityDataPut(UriInfo uriInfo, EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates,
            Entity requestEntity) throws ODataApplicationException {
        OiyokanEdmProvider provider = new OiyokanEdmProvider();
        if (!edmEntitySet.getEntityContainer().getName().equals(provider.getEntityContainer().getName())) {
            // Container 名が不一致. 処理せずに戻します.
            // TODO FIXME 例外.
            return;
        }

        OiyokanCsdlEntitySet entitySet = null;
        for (CsdlEntitySet look : provider.getEntityContainer().getEntitySets()) {
            if (edmEntitySet.getName().equals(look.getName())) {
                entitySet = (OiyokanCsdlEntitySet) look;
                break;
            }
        }
        if (entitySet == null) {
            // TODO FIXME 例外.
            return;
        }

        sqlInfo = new BasicSqlInfo(entitySet);
        ///////// getDeleteDml(edmEntitySet, keyPredicates);
        executeDml();

        // TODO FIXME メッセージ番号取り直し
        // [M999] NOT IMPLEMENTED: Generic NOT implemented message.
        System.err.println(OiyokanMessages.M999);
        throw new ODataApplicationException(OiyokanMessages.M999, 500, Locale.ENGLISH);
    }
}