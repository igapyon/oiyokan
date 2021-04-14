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
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.core.uri.UriParameterImpl;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanConstants.DatabaseType;
import jp.oiyokan.OiyokanCsdlEntitySet;
import jp.oiyokan.OiyokanEdmProvider;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.basic.sql.OiyoSqlDeleteOneBuilder;
import jp.oiyokan.basic.sql.OiyoSqlInfo;
import jp.oiyokan.basic.sql.OiyoSqlInsertOneBuilder;
import jp.oiyokan.basic.sql.OiyoSqlQueryOneBuilder;
import jp.oiyokan.basic.sql.OiyoSqlUpdateOneBuilder;

/**
 * Entity 1件の検索に関する基本的なJDBC処理
 */
public class OiyoBasicJdbcEntityOneBuilder {
    /////////////////////////
    // SELECT

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

        final OiyoSqlInfo sqlInfo = new OiyoSqlInfo(entitySet);
        new OiyoSqlQueryOneBuilder(sqlInfo).buildSelectOneQuery(edmEntitySet, keyPredicates);

        final String sql = sqlInfo.getSqlBuilder().toString();
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: TRACE: SQL single: " + sql);

        final long startMillisec = System.currentTimeMillis();
        try (var stmt = connTargetDb.prepareStatement(sql)) {
            // set query timeout
            stmt.setQueryTimeout(OiyokanConstants.JDBC_STMT_TIMEOUT);

            int idxColumn = 1;
            for (Object look : sqlInfo.getSqlParamList()) {
                OiyoBasicJdbcUtil.bindPreparedParameter(stmt, idxColumn++, look);
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
                Property prop = OiyoBasicJdbcUtil.resultSet2Property(rset, rsmeta, column, entitySet);
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

    /////////////////////////
    // INSERT

    /**
     * Create Entity data.
     * 
     * @param uriInfo       URI info.
     * @param edmEntitySet  EdmEntitySet.
     * @param requestEntity Entity to create.
     * @return Entity created.
     * @throws ODataApplicationException OData App exception occured.
     */
    public Entity createEntityData(UriInfo uriInfo, EdmEntitySet edmEntitySet, Entity requestEntity)
            throws ODataApplicationException {
        final OiyokanCsdlEntitySet entitySet = findEntitySet(edmEntitySet);
        if (entitySet == null) {
            // [M211] No such EntitySet found (createEntity)
            System.err.println(OiyokanMessages.M211);
            throw new ODataApplicationException(OiyokanMessages.M211, //
                    OiyokanMessages.M211_CODE, Locale.ENGLISH);
        }

        final OiyoSqlInfo sqlInfo = new OiyoSqlInfo(entitySet);
        new OiyoSqlInsertOneBuilder(sqlInfo).buildInsertIntoDml(edmEntitySet, null, requestEntity);

        // データベースに接続.
        boolean isTranSuccessed = false;
        try (Connection connTargetDb = OiyoBasicJdbcUtil.getConnection(sqlInfo.getEntitySet().getSettingsDatabase())) {
            // Set auto commit OFF.
            connTargetDb.setAutoCommit(false);
            try {
                final List<String> generatedKeys = OiyoBasicJdbcUtil.executeDml(connTargetDb, sqlInfo, true);
                // 生成されたキーをその後の処理に反映。
                final List<UriParameter> keyPredicates = new ArrayList<>();
                if (DatabaseType.ORACLE == sqlInfo.getEntitySet().getDatabaseType()) {
                    // ORACLEの特殊ルール。ROWIDが戻るので決め打ちで検索.
                    final UriParameterImpl newParam = new UriParameterImpl();
                    newParam.setName("ROWID");
                    newParam.setText(generatedKeys.get(0));
                    keyPredicates.add(newParam);
                } else {
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
                            if (generatedKeys.size() == 0) {
                                // [M217] UNEXPECTED: Can't retrieve PreparedStatement#getGeneratedKeys: Fail to
                                // map auto generated key field.
                                System.err.println(OiyokanMessages.M217 + ": " + propKey.getName());
                                throw new ODataApplicationException(OiyokanMessages.M217 + ": " + propKey.getName(), //
                                        OiyokanMessages.M217_CODE, Locale.ENGLISH);
                            }
                            propValue = generatedKeys.get(0);
                            generatedKeys.remove(0);
                        }

                        final UriParameterImpl newParam = new UriParameterImpl();
                        newParam.setName(propKey.getName());
                        newParam.setText(propValue);
                        keyPredicates.add(newParam);
                    }
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

    ////////////////////////
    // DELETE

    /**
     * Delete Entity data.
     * 
     * @param uriInfo       URI info.
     * @param edmEntitySet  EdmEntitySet.
     * @param keyPredicates Keys to delete.
     * @throws ODataApplicationException OData App exception occured.
     */
    public void deleteEntityData(UriInfo uriInfo, EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates)
            throws ODataApplicationException {
        final OiyokanCsdlEntitySet entitySet = findEntitySet(edmEntitySet);
        if (entitySet == null) {
            // [M212] No such EntitySet found (deleteEntity)
            System.err.println(OiyokanMessages.M212);
            throw new ODataApplicationException(OiyokanMessages.M212, //
                    OiyokanMessages.M212_CODE, Locale.ENGLISH);
        }

        final OiyoSqlInfo sqlInfo = new OiyoSqlInfo(entitySet);
        new OiyoSqlDeleteOneBuilder(sqlInfo).buildDeleteDml(edmEntitySet, keyPredicates);

        // データベースに接続.
        boolean isTranSuccessed = false;
        try (Connection connTargetDb = OiyoBasicJdbcUtil.getConnection(sqlInfo.getEntitySet().getSettingsDatabase())) {
            // Set auto commit OFF.
            connTargetDb.setAutoCommit(false);
            try {
                OiyoBasicJdbcUtil.executeDml(connTargetDb, sqlInfo, false);

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

    ////////////////////////
    // UPDATE (PATCH)
    // OiyokanはPUTをサポートしない

    /**
     * Update Entity data (PATCH).
     * 
     * @param uriInfo       URI info.
     * @param edmEntitySet  EdmEntitySet.
     * @param keyPredicates Keys to update.
     * @param requestEntity Entity date for update.
     * @param ifMatch       Header If-Match.
     * @param ifNoneMatch   Header If-None-Match.
     * @throws ODataApplicationException OData App exception occured.
     */
    public void updateEntityDataPatch(UriInfo uriInfo, EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates,
            Entity requestEntity, final boolean ifMatch, final boolean ifNoneMatch) throws ODataApplicationException {
        final OiyokanCsdlEntitySet entitySet = findEntitySet(edmEntitySet);
        if (entitySet == null) {
            // [M213] No such EntitySet found (updateEntity(PATCH))
            System.err.println(OiyokanMessages.M213);
            throw new ODataApplicationException(OiyokanMessages.M213, //
                    OiyokanMessages.M213_CODE, Locale.ENGLISH);
        }

        final OiyoSqlInfo sqlInfo = new OiyoSqlInfo(entitySet);

        // データベースに接続.
        try (Connection connTargetDb = OiyoBasicJdbcUtil.getConnection(sqlInfo.getEntitySet().getSettingsDatabase())) {
            // Set auto commit OFF.
            connTargetDb.setAutoCommit(false);
            boolean isTranSuccessed = false;

            if (ifMatch) {
                // If-Match header が '*' 指定されたら UPDATE.
                if (OiyokanConstants.IS_TRACE_ODATA_V4)
                    System.err.println("OData v4: TRACE: PATCH: UPDATE (If-Match): " + edmEntitySet.getName());
                new OiyoSqlUpdateOneBuilder(sqlInfo).buildUpdatePatchDml(edmEntitySet, keyPredicates, requestEntity);

            } else if (ifNoneMatch) {
                // If-None-Match header が '*' 指定されたら INSERT.
                if (OiyokanConstants.IS_TRACE_ODATA_V4)
                    System.err.println("OData v4: TRACE: PATCH: INSERT (If-None-Match): " + edmEntitySet.getName());
                new OiyoSqlInsertOneBuilder(sqlInfo).buildInsertIntoDml(edmEntitySet, keyPredicates, requestEntity);

            } else {
                // If-Match header も If-None-Match header も指定がない場合は UPSERT.
                if (OiyokanConstants.IS_TRACE_ODATA_V4)
                    System.err.println("OData v4: TRACE: PATCH: UPSERT: " + edmEntitySet.getName());

                try {
                    // SELECT to check exists
                    readEntityData(connTargetDb, uriInfo, edmEntitySet, keyPredicates);

                    // UPDATE
                    new OiyoSqlUpdateOneBuilder(sqlInfo).buildUpdatePatchDml(edmEntitySet, keyPredicates,
                            requestEntity);
                } catch (ODataApplicationException ex) {
                    if (OiyokanMessages.M207_CODE != ex.getStatusCode()) {
                        // そのまま throw.
                        throw ex;
                    }

                    // INSERT
                    new OiyoSqlInsertOneBuilder(sqlInfo).buildInsertIntoDml(edmEntitySet, keyPredicates, requestEntity);
                }
            }

            try {
                OiyoBasicJdbcUtil.executeDml(connTargetDb, sqlInfo, false);

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

    // TODO FIXME 以下のメソッドは共通関数化を検討すること.
    /**
     * Find EntitySet.
     * 
     * @param edmEntitySet EdmEntitySet.
     * @return OiyokanCsdlEntitySet for specified edmEntitySet.
     * @throws ODataApplicationException OData App exception occured.
     */
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