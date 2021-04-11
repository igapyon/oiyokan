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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
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

    /**
     * Read Entity data.
     * 
     * @param connTargetDb  コネクション.
     * @param uriInfo       URI info.
     * @param edmEntitySet  EdmEntitySet.
     * @param keyPredicates List of UriParameter.
     * @return Entity.
     * @throws ODataApplicationException OData App exception occured.
     */
    public Entity readEntityData(Connection connTargetDb, UriInfo uriInfo, EdmEntitySet edmEntitySet,
            List<UriParameter> keyPredicates) throws ODataApplicationException {
        final OiyokanCsdlEntitySet entitySet = findEntitySet(edmEntitySet);
        if (entitySet == null) {
            // [M206] No such EntitySet found (readEntity)
            System.err.println(OiyokanMessages.M206);
            throw new ODataApplicationException(OiyokanMessages.M206, OiyokanMessages.M206_CODE, Locale.ENGLISH);
        }

        sqlInfo = new BasicSqlInfo(entitySet);
        getSelectOneQuery(edmEntitySet, keyPredicates);

        final String sql = sqlInfo.getSqlBuilder().toString();
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: TRACE: SQL single: " + sql);

        final long startMillisec = System.currentTimeMillis();
        try (var stmt = connTargetDb.prepareStatement(sql)) {
            // set query timeout
            stmt.setQueryTimeout(OiyokanConstants.JDBC_STMT_TIMEOUT);

            int idxColumn = 1;
            for (Object look : sqlInfo.getSqlParamList()) {
                BasicJdbcUtil.bindPreparedParameter(stmt, idxColumn++, look);
            }

            stmt.executeQuery();
            var rset = stmt.getResultSet();
            if (!rset.next()) {
                // [M207] No such Entity data
                System.err.println(OiyokanMessages.M207 + ": " + sql);
                throw new ODataApplicationException(OiyokanMessages.M207 + ": " //
                        + sql, OiyokanMessages.M207_CODE, Locale.ENGLISH);
            }

            final ResultSetMetaData rsmeta = rset.getMetaData();
            final Entity ent = new Entity();
            for (int column = 1; column <= rsmeta.getColumnCount(); column++) {
                Property prop = BasicJdbcUtil.resultSet2Property(rset, rsmeta, column, entitySet);
                ent.addProperty(prop);
            }

            if (rset.next()) {
                // [M215] UNEXPECTED: Too many rows found (readEntity)
                System.err.println(OiyokanMessages.M215 + ": " + sql);
                throw new ODataApplicationException(OiyokanMessages.M215 + ": " + sql, //
                        OiyokanMessages.M215_CODE, Locale.ENGLISH);
            }

            final long endMillisec = System.currentTimeMillis();
            if (OiyokanConstants.IS_TRACE_ODATA_V4) {
                final long elapsed = endMillisec - startMillisec;
                if (elapsed >= 10) {
                    System.err.println("OData v4: TRACE: SQL: elapsed: " + (endMillisec - startMillisec));
                }
            }

            return ent;
        } catch (SQLTimeoutException ex) {
            // [M208] SQL timeout at execute (readEntity)
            System.err.println(OiyokanMessages.M208 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M208 + ": " + sql, //
                    OiyokanMessages.M208_CODE, Locale.ENGLISH);
        } catch (SQLException ex) {
            // [M209] Fail to execute SQL (readEntity)
            System.err.println(OiyokanMessages.M209 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M209 + ": " + sql, //
                    OiyokanMessages.M209_CODE, Locale.ENGLISH);
        }
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

        sqlInfo.getSqlBuilder().append(" FROM " + sqlInfo.getEntitySet().getDbTableNameTargetIyo());

        sqlInfo.getSqlBuilder().append(" WHERE ");
        boolean isFirst = true;
        for (UriParameter param : keyPredicates) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(" AND ");
            }
            sqlInfo.getSqlBuilder()
                    .append(BasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyokanNamingUtil.entity2Db(param.getName())));
            sqlInfo.getSqlBuilder().append("=");

            CsdlProperty csdlProp = sqlInfo.getEntitySet().getEntityType().getProperty(param.getName());
            BasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, csdlProp.getType(), param.getText());
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
        final OiyokanCsdlEntitySet entitySet = findEntitySet(edmEntitySet);
        if (entitySet == null) {
            // [M211] No such EntitySet found (createEntity)
            System.err.println(OiyokanMessages.M211);
            throw new ODataApplicationException(OiyokanMessages.M211, //
                    OiyokanMessages.M211_CODE, Locale.ENGLISH);
        }

        sqlInfo = new BasicSqlInfo(entitySet);
        getInsertIntoDml(edmEntitySet, requestEntity);

        // データベースに接続.
        boolean isTranSuccessed = false;
        try (Connection connTargetDb = BasicJdbcUtil.getConnection(sqlInfo.getEntitySet().getSettingsDatabase())) {
            // Set auto commit OFF.
            connTargetDb.setAutoCommit(false);
            try {
                final List<String> generatedKeys = BasicJdbcUtil.executeDml(connTargetDb, sqlInfo);
                // 生成されたキーをその後の処理に反映。
                final List<UriParameter> keyPredicates = new ArrayList<>();
                for (CsdlPropertyRef propKey : entitySet.getEntityType().getKey()) {
                    String propValue = null;
                    for (Property look : requestEntity.getProperties()) {
                        if (look.getName().equals(propKey.getName())) {
                            if (look.getValue() instanceof java.util.Calendar) {
                                java.util.Calendar cal = (java.util.Calendar) look.getValue();
                                Instant instant = cal.toInstant();
                                ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
                                propValue = zdt.format(DateTimeFormatter.ISO_INSTANT);
                            } else {
                                propValue = String.valueOf(look.getValue());
                            }
                            break;
                        }
                    }
                    if (propValue == null) {
                        System.err.println("TRACE: propKey:" + propKey.getName() + "に対応する入力なし.");
                        propValue = generatedKeys.get(0);
                        generatedKeys.remove(0);
                    }
                    final String propValueFixed = propValue;
                    UriParameter newParam = new UriParameter() {
                        @Override
                        public String getAlias() {
                            return null;
                        }

                        @Override
                        public String getText() {
                            return propValueFixed;
                        }

                        @Override
                        public Expression getExpression() {
                            return null;
                        }

                        @Override
                        public String getName() {
                            return propKey.getName();
                        }

                        @Override
                        public String getReferencedProperty() {
                            return null;
                        }
                    };
                    keyPredicates.add(newParam);
                }

                // 更新後のデータをリロード.
                Entity result = readEntityData(connTargetDb, uriInfo, edmEntitySet, keyPredicates);

                // トランザクションを成功としてマーク.
                isTranSuccessed = true;
                return result;
            } finally {
                if (isTranSuccessed) {
                    connTargetDb.commit();
                } else {
                    connTargetDb.rollback();
                }
                // Set auto commit ON.
                connTargetDb.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            // [M205] Fail to execute SQL.
            System.err.println(OiyokanMessages.M205 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M205, //
                    OiyokanMessages.M205_CODE, Locale.ENGLISH);
        }
    }

    private void getInsertIntoDml(EdmEntitySet edmEntitySet, Entity requestEntity) throws ODataApplicationException {
        sqlInfo.getSqlBuilder().append("INSERT INTO ");
        sqlInfo.getSqlBuilder().append(sqlInfo.getEntitySet().getDbTableNameTargetIyo());
        sqlInfo.getSqlBuilder().append(" (");
        boolean isFirst = true;
        for (Property prop : requestEntity.getProperties()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }

            final String colName = BasicJdbcUtil.escapeKakkoFieldName(sqlInfo,
                    OiyokanNamingUtil.entity2Db(prop.getName()));
            sqlInfo.getSqlBuilder().append(colName);
        }

        sqlInfo.getSqlBuilder().append(") VALUES (");
        isFirst = true;
        for (Property prop : requestEntity.getProperties()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }
            BasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, prop.getType(), prop.getValue());
        }

        sqlInfo.getSqlBuilder().append(")");
    }

    ////////////////////////
    // DELETE

    public void deleteEntityData(UriInfo uriInfo, EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates)
            throws ODataApplicationException {
        final OiyokanCsdlEntitySet entitySet = findEntitySet(edmEntitySet);
        if (entitySet == null) {
            // [M212] No such EntitySet found (deleteEntity)
            System.err.println(OiyokanMessages.M212);
            throw new ODataApplicationException(OiyokanMessages.M212, //
                    OiyokanMessages.M212_CODE, Locale.ENGLISH);
        }

        sqlInfo = new BasicSqlInfo(entitySet);
        getDeleteDml(edmEntitySet, keyPredicates);

        // データベースに接続.
        boolean isTranSuccessed = false;
        try (Connection connTargetDb = BasicJdbcUtil.getConnection(sqlInfo.getEntitySet().getSettingsDatabase())) {
            // Set auto commit OFF.
            connTargetDb.setAutoCommit(false);
            try {
                BasicJdbcUtil.executeDml(connTargetDb, sqlInfo);

                // トランザクションを成功としてマーク.
                isTranSuccessed = true;
            } finally {
                if (isTranSuccessed) {
                    connTargetDb.commit();
                } else {
                    connTargetDb.rollback();
                }
                // Set auto commit ON.
                connTargetDb.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            // [M205] Fail to execute SQL.
            System.err.println(OiyokanMessages.M205 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M205, //
                    OiyokanMessages.M205_CODE, Locale.ENGLISH);
        }
    }

    private void getDeleteDml(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates)
            throws ODataApplicationException {
        sqlInfo.getSqlBuilder().append("DELETE FROM ");
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

            sqlInfo.getSqlBuilder().append(
                    BasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyokanNamingUtil.entity2Db(csdlProp.getName())));
            sqlInfo.getSqlBuilder().append("=");
            BasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, csdlProp.getType(), param.getText());
        }
    }

    ////////////////////////
    // UPDATE

    public void updateEntityDataPatch(UriInfo uriInfo, EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates,
            Entity requestEntity) throws ODataApplicationException {
        final OiyokanCsdlEntitySet entitySet = findEntitySet(edmEntitySet);
        if (entitySet == null) {
            // [M213] No such EntitySet found (updateEntity(PATCH))
            System.err.println(OiyokanMessages.M213);
            throw new ODataApplicationException(OiyokanMessages.M213, //
                    OiyokanMessages.M213_CODE, Locale.ENGLISH);
        }

        sqlInfo = new BasicSqlInfo(entitySet);
        getUpdatePatchDml(edmEntitySet, keyPredicates, requestEntity);

        // データベースに接続.
        try (Connection connTargetDb = BasicJdbcUtil.getConnection(sqlInfo.getEntitySet().getSettingsDatabase())) {
            // Set auto commit OFF.
            connTargetDb.setAutoCommit(false);
            boolean isTranSuccessed = false;
            try {
                BasicJdbcUtil.executeDml(connTargetDb, sqlInfo);

                // トランザクションを成功としてマーク.
                isTranSuccessed = true;
            } finally {
                if (isTranSuccessed) {
                    connTargetDb.commit();
                } else {
                    connTargetDb.rollback();
                }
                // Set auto commit ON.
                connTargetDb.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            // [M205] Fail to execute SQL.
            System.err.println(OiyokanMessages.M205 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M205, //
                    OiyokanMessages.M205_CODE, Locale.ENGLISH);
        }
    }

    private void getUpdatePatchDml(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates, Entity requestEntity)
            throws ODataApplicationException {
        sqlInfo.getSqlBuilder().append("UPDATE ");
        sqlInfo.getSqlBuilder().append(sqlInfo.getEntitySet().getDbTableNameTargetIyo());
        sqlInfo.getSqlBuilder().append(" SET ");
        boolean isFirst = true;
        for (Property prop : requestEntity.getProperties()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }

            sqlInfo.getSqlBuilder()
                    .append(BasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyokanNamingUtil.entity2Db(prop.getName())));
            sqlInfo.getSqlBuilder().append("=");

            BasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, prop.getType(), prop.getValue());
        }

        sqlInfo.getSqlBuilder().append(" WHERE ");

        isFirst = true;
        for (UriParameter param : keyPredicates) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(" AND ");
            }
            sqlInfo.getSqlBuilder()
                    .append(BasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyokanNamingUtil.entity2Db(param.getName())));
            sqlInfo.getSqlBuilder().append("=");

            CsdlProperty csdlProp = sqlInfo.getEntitySet().getEntityType().getProperty(param.getName());
            BasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, csdlProp.getType(), param.getText());
        }
    }

    public void updateEntityDataPut(UriInfo uriInfo, EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates,
            Entity requestEntity) throws ODataApplicationException {
        final OiyokanCsdlEntitySet entitySet = findEntitySet(edmEntitySet);
        if (entitySet == null) {
            // [M214] No such EntitySet found (updateEntity(PUT))
            System.err.println(OiyokanMessages.M214);
            throw new ODataApplicationException(OiyokanMessages.M214, //
                    OiyokanMessages.M214_CODE, Locale.ENGLISH);
        }

        sqlInfo = new BasicSqlInfo(entitySet);
        getUpdatePutDml(edmEntitySet, keyPredicates, requestEntity);

        // データベースに接続.
        try (Connection connTargetDb = BasicJdbcUtil.getConnection(sqlInfo.getEntitySet().getSettingsDatabase())) {
            // Set auto commit OFF.
            connTargetDb.setAutoCommit(false);
            boolean isTranSuccessed = false;

            try {
                BasicJdbcUtil.executeDml(connTargetDb, sqlInfo);

                // トランザクションを成功としてマーク.
                isTranSuccessed = true;
            } finally {
                if (isTranSuccessed) {
                    connTargetDb.commit();
                } else {
                    connTargetDb.rollback();
                }
                // Set auto commit ON.
                connTargetDb.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            // [M205] Fail to execute SQL.
            System.err.println(OiyokanMessages.M205 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M205, //
                    OiyokanMessages.M205_CODE, Locale.ENGLISH);
        }
    }

    private void getUpdatePutDml(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates, Entity requestEntity)
            throws ODataApplicationException {
        sqlInfo.getSqlBuilder().append("UPDATE ");
        sqlInfo.getSqlBuilder().append(sqlInfo.getEntitySet().getDbTableNameTargetIyo());
        sqlInfo.getSqlBuilder().append(" SET ");

        // primary key 以外の全てが対象。指定のないものは null。
        final List<CsdlPropertyRef> keys = sqlInfo.getEntitySet().getEntityType().getKey();
        boolean isFirst = true;
        CSDL_LOOP: for (CsdlProperty csdlProp : sqlInfo.getEntitySet().getEntityType().getProperties()) {
            // KEY以外が対象。
            for (CsdlPropertyRef key : keys) {
                if (key.getName().equals(csdlProp.getName())) {
                    // これはキー項目です。処理対象外.
                    continue CSDL_LOOP;
                }
            }

            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }

            sqlInfo.getSqlBuilder().append(
                    BasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyokanNamingUtil.entity2Db(csdlProp.getName())));

            sqlInfo.getSqlBuilder().append("=");
            Property prop = requestEntity.getProperty(csdlProp.getName());
            if (prop != null) {
                BasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, csdlProp.getType(), prop.getValue());
            } else {
                // 指定のないものには nullをセット.
                BasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, csdlProp.getType(), null);
            }
        }

        sqlInfo.getSqlBuilder().append(" WHERE ");

        isFirst = true;
        for (UriParameter param : keyPredicates) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(" AND ");
            }
            sqlInfo.getSqlBuilder()
                    .append(BasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyokanNamingUtil.entity2Db(param.getName())));
            sqlInfo.getSqlBuilder().append("=");

            CsdlProperty csdlProp = sqlInfo.getEntitySet().getEntityType().getProperty(param.getName());
            BasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, csdlProp.getType(), param.getText());
        }
    }

    public static OiyokanCsdlEntitySet findEntitySet(EdmEntitySet edmEntitySet) throws ODataApplicationException {
        final OiyokanEdmProvider provider = new OiyokanEdmProvider();
        if (!edmEntitySet.getEntityContainer().getName().equals(provider.getEntityContainer().getName())) {
            // Container 名が不一致. 処理せずに戻します.
            // 例外処理は呼び出し元で実施.
            return null;
        }

        for (CsdlEntitySet look : provider.getEntityContainer().getEntitySets()) {
            if (edmEntitySet.getName().equals(look.getName())) {
                return (OiyokanCsdlEntitySet) look;
            }
        }

        // 例外処理は呼び出し元で実施.
        return null;
    }
}