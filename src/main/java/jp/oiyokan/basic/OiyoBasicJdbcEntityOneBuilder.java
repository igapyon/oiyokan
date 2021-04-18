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
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.core.uri.UriParameterImpl;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanConstants.DatabaseType;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.basic.sql.OiyoSqlDeleteOneBuilder;
import jp.oiyokan.basic.sql.OiyoSqlInsertOneBuilder;
import jp.oiyokan.basic.sql.OiyoSqlQueryOneBuilder;
import jp.oiyokan.basic.sql.OiyoSqlUpdateOneBuilder;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.common.OiyoSqlInfo;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;

/**
 * Entity 1件の検索に関する基本的なJDBC処理
 */
public class OiyoBasicJdbcEntityOneBuilder {
    /**
     * Oiyokan Info.
     */
    private OiyoInfo oiyoInfo;

    public OiyoBasicJdbcEntityOneBuilder(OiyoInfo oiyoInfo) {
        this.oiyoInfo = oiyoInfo;
    }

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
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, edmEntitySet.getName());

        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: TRACE: ENTITY: READ: " + edmEntitySet.getName());

        final OiyoSqlInfo sqlInfo = new OiyoSqlInfo(oiyoInfo, entitySet.getName());
        new OiyoSqlQueryOneBuilder(oiyoInfo, sqlInfo).buildSelectOneQuery(edmEntitySet, keyPredicates);

        final String sql = sqlInfo.getSqlBuilder().toString();
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: TRACE: SQL single: " + sql);

        final long startMillisec = System.currentTimeMillis();
        try (var stmt = connTargetDb.prepareStatement(sql)) {
            // set query timeout
            stmt.setQueryTimeout(OiyokanConstants.JDBC_STMT_TIMEOUT);

            int idxColumn = 1;
            for (Object look : sqlInfo.getSqlParamList()) {
                OiyoCommonJdbcUtil.bindPreparedParameter(stmt, idxColumn++, look);
            }

            stmt.executeQuery();
            var rset = stmt.getResultSet();
            if (!rset.next()) {
                // [M207] No such Entity data
                System.err.println(OiyokanMessages.IY3105 + ": " + sql);
                throw new ODataApplicationException(OiyokanMessages.IY3105 + ": " //
                        + sql, OiyokanMessages.IY3105_CODE, Locale.ENGLISH);
            }

            final ResultSetMetaData rsmeta = rset.getMetaData();
            final Entity ent = new Entity();
            for (int column = 1; column <= rsmeta.getColumnCount(); column++) {
                Property prop = OiyoCommonJdbcUtil.resultSet2Property(oiyoInfo, rset, rsmeta, column, entitySet);
                ent.addProperty(prop);
            }

            if (rset.next()) {
                // [M215] UNEXPECTED: Too many rows found (readEntity)
                System.err.println(OiyokanMessages.IY3112 + ": " + sql);
                throw new ODataApplicationException(OiyokanMessages.IY3112 + ": " + sql, //
                        OiyokanMessages.IY3112_CODE, Locale.ENGLISH);
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
            System.err.println(OiyokanMessages.IY3501 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.IY3501 + ": " + sql, //
                    OiyokanMessages.IY3501_CODE, Locale.ENGLISH);
        } catch (SQLException ex) {
            // [M209] Fail to execute SQL (readEntity)
            System.err.println(OiyokanMessages.IY3106 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.IY3106 + ": " + sql, //
                    OiyokanMessages.IY3106_CODE, Locale.ENGLISH);
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
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, edmEntitySet.getName());

        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: TRACE: ENTITY: CREATE: " + edmEntitySet.getName());

        final OiyoSqlInfo sqlInfo = new OiyoSqlInfo(oiyoInfo, entitySet.getName());
        new OiyoSqlInsertOneBuilder(oiyoInfo, sqlInfo).buildInsertIntoDml(edmEntitySet, null, requestEntity);

        final OiyoSettingsDatabase database = OiyoInfoUtil.getOiyoDatabaseByEntitySetName(oiyoInfo,
                entitySet.getName());
        final OiyokanConstants.DatabaseType databaseType = OiyoInfoUtil.getOiyoDatabaseTypeByEntitySetName(oiyoInfo,
                entitySet.getName());

        // データベースに接続.
        boolean isTranSuccessed = false;
        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(database)) {
            // Set auto commit OFF.
            connTargetDb.setAutoCommit(false);
            try {
                final List<String> generatedKeys = OiyoCommonJdbcUtil.executeDml(connTargetDb, sqlInfo, true);
                // 生成されたキーをその後の処理に反映。
                final List<UriParameter> keyPredicates = new ArrayList<>();
                if (DatabaseType.ORACLE == databaseType) {
                    // ORACLEの特殊ルール。ROWIDが戻るので決め打ちで検索.
                    final UriParameterImpl newParam = new UriParameterImpl();
                    newParam.setName("ROWID");
                    newParam.setText(generatedKeys.get(0));
                    keyPredicates.add(newParam);
                } else {
                    for (String keyName : entitySet.getEntityType().getKeyName()) {
                        String propValue = null;
                        for (Property look : requestEntity.getProperties()) {
                            if (look.getName().equals(keyName)) {
                                if (look.getValue() instanceof java.util.Calendar) {
                                    // TODO この箇所がどのようなケースで動作するのか調査。
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
                            System.err.println("TRACE: propKey:" + keyName + "に対応する入力なし.");
                            if (generatedKeys.size() == 0) {
                                // [M217] UNEXPECTED: Can't retrieve PreparedStatement#getGeneratedKeys: Fail to
                                // map auto generated key field.
                                System.err.println(OiyokanMessages.IY3114 + ": " + keyName);
                                throw new ODataApplicationException(OiyokanMessages.IY3114 + ": " + keyName, //
                                        OiyokanMessages.IY3114_CODE, Locale.ENGLISH);
                            }
                            propValue = generatedKeys.get(0);
                            generatedKeys.remove(0);
                        }

                        final UriParameterImpl newParam = new UriParameterImpl();
                        newParam.setName(keyName);
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
            System.err.println(OiyokanMessages.IY3103 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.IY3103, //
                    OiyokanMessages.IY3103_CODE, Locale.ENGLISH);
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
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, edmEntitySet.getName());

        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: TRACE: ENTITY: DELETE: " + edmEntitySet.getName());

        final OiyoSqlInfo sqlInfo = new OiyoSqlInfo(oiyoInfo, entitySet.getName());
        new OiyoSqlDeleteOneBuilder(oiyoInfo, sqlInfo).buildDeleteDml(edmEntitySet, keyPredicates);

        final OiyoSettingsDatabase database = OiyoInfoUtil.getOiyoDatabaseByEntitySetName(oiyoInfo,
                entitySet.getName());

        // データベースに接続.
        boolean isTranSuccessed = false;
        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(database)) {
            // Set auto commit OFF.
            connTargetDb.setAutoCommit(false);
            try {
                OiyoCommonJdbcUtil.executeDml(connTargetDb, sqlInfo, false);

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
            System.err.println(OiyokanMessages.IY3103 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.IY3103, //
                    OiyokanMessages.IY3103_CODE, Locale.ENGLISH);
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
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, edmEntitySet.getName());

        final OiyoSqlInfo sqlInfo = new OiyoSqlInfo(oiyoInfo, entitySet.getName());

        final OiyoSettingsDatabase database = OiyoInfoUtil.getOiyoDatabaseByEntitySetName(oiyoInfo,
                entitySet.getName());

        // データベースに接続.
        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(database)) {
            // Set auto commit OFF.
            connTargetDb.setAutoCommit(false);
            boolean isTranSuccessed = false;

            if (ifMatch) {
                // If-Match header が '*' 指定されたら UPDATE.
                if (OiyokanConstants.IS_TRACE_ODATA_V4)
                    System.err.println("OData v4: TRACE: ENTITY: PATCH: UPDATE (If-Match): " + edmEntitySet.getName());
                new OiyoSqlUpdateOneBuilder(oiyoInfo, sqlInfo).buildUpdatePatchDml(edmEntitySet, keyPredicates,
                        requestEntity);

            } else if (ifNoneMatch) {
                // If-None-Match header が '*' 指定されたら INSERT.
                if (OiyokanConstants.IS_TRACE_ODATA_V4)
                    System.err.println(
                            "OData v4: TRACE: ENTITY: PATCH: INSERT (If-None-Match): " + edmEntitySet.getName());
                new OiyoSqlInsertOneBuilder(oiyoInfo, sqlInfo).buildInsertIntoDml(edmEntitySet, keyPredicates,
                        requestEntity);

            } else {
                // If-Match header も If-None-Match header も指定がない場合は UPSERT.
                if (OiyokanConstants.IS_TRACE_ODATA_V4)
                    System.err.println("OData v4: TRACE: ENTITY: PATCH: UPSERT: " + edmEntitySet.getName());

                try {
                    // SELECT to check exists
                    readEntityData(connTargetDb, uriInfo, edmEntitySet, keyPredicates);

                    // UPDATE
                    new OiyoSqlUpdateOneBuilder(oiyoInfo, sqlInfo).buildUpdatePatchDml(edmEntitySet, keyPredicates,
                            requestEntity);
                } catch (ODataApplicationException ex) {
                    if (OiyokanMessages.IY3105_CODE != ex.getStatusCode()) {
                        // そのまま throw.
                        throw ex;
                    }

                    // INSERT
                    new OiyoSqlInsertOneBuilder(oiyoInfo, sqlInfo).buildInsertIntoDml(edmEntitySet, keyPredicates,
                            requestEntity);
                }
            }

            try {
                OiyoCommonJdbcUtil.executeDml(connTargetDb, sqlInfo, false);

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
            System.err.println(OiyokanMessages.IY3103 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.IY3103, //
                    OiyokanMessages.IY3103_CODE, Locale.ENGLISH);
        }
    }
}