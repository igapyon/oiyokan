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
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
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
import jp.oiyokan.common.OiyoCommonJdbcBindParamUtil;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.common.OiyoSqlInfo;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsProperty;

/**
 * Entity 1件の検索に関する基本的なJDBC処理
 */
public class OiyoBasicJdbcEntityOneBuilder {
    private static final Log log = LogFactory.getLog(OiyoBasicJdbcEntityOneBuilder.class);

    /**
     * Oiyokan Info.
     */
    private OiyoInfo oiyoInfo;

    private int lastPatchStatusCode = 500;

    public OiyoBasicJdbcEntityOneBuilder(OiyoInfo oiyoInfo) {
        this.oiyoInfo = oiyoInfo;
    }

    public int getLastPatchStatusCode() {
        return lastPatchStatusCode;
    }

    /////////////////////////
    // SELECT

    /**
     * Read Entity data.
     * 
     * @param uriInfo       URI info.
     * @param entitySet     EntitySet info.
     * @param keyPredicates List of UriParameter.
     * @return Entity.
     * @throws ODataApplicationException OData App exception occured.
     */
    public Entity readEntityData(UriInfo uriInfo, OiyoSettingsEntitySet entitySet, List<UriParameter> keyPredicates)
            throws ODataApplicationException {
        final OiyoSettingsDatabase database = OiyoInfoUtil.getOiyoDatabaseByEntitySetName(oiyoInfo,
                entitySet.getName());

        // [IY1071] INFO: ENTITY: READ
        log.info(OiyokanMessages.IY1071 + ": " + entitySet.getName());

        // データベースに接続.
        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(database)) {
            log.trace("[database transaction] WITHOUT database transaction.");
            return readInternal(connTargetDb, uriInfo, entitySet, keyPredicates);
        } catch (SQLException ex) {
            // [IY3107] Database exception occured (readEntity)
            log.error(OiyokanMessages.IY3107 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.IY3107, //
                    OiyokanMessages.IY3107_CODE, Locale.ENGLISH);
        }
    }

    /////////////////////////
    // INSERT

    /**
     * Create Entity data.
     * 
     * @param uriInfo       URI info.
     * @param entitySet     EntitySet info.
     * @param requestEntity Entity to create.
     * @return Entity created.
     * @throws ODataApplicationException OData App exception occured.
     */
    public Entity createEntityData(UriInfo uriInfo, OiyoSettingsEntitySet entitySet, Entity requestEntity)
            throws ODataApplicationException {
        final OiyoSettingsDatabase database = OiyoInfoUtil.getOiyoDatabaseByEntitySetName(oiyoInfo,
                entitySet.getName());

        // [IY1072] INFO: ENTITY: CREATE
        log.info(OiyokanMessages.IY1072 + ": " + entitySet.getName());

        // データベースに接続.
        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(database)) {
            log.trace("[database transaction] WITHOUT database transaction.");
            return createInternal(connTargetDb, uriInfo, entitySet, null/* キーの与えられないパターン */, requestEntity);
        } catch (SQLException ex) {
            // [IY3155] UNEXPECTED database error occured.
            log.error(OiyokanMessages.IY3155 + ": " + ex.toString(), ex);
            throw new ODataApplicationException(OiyokanMessages.IY3155, OiyokanMessages.IY3155_CODE, Locale.ENGLISH);
        }
    }

    ////////////////////////
    // DELETE

    /**
     * Delete Entity data.
     * 
     * @param uriInfo       URI info.
     * @param entitySet     entitySet info.
     * @param keyPredicates Keys to delete.
     * @throws ODataApplicationException OData App exception occured.
     */
    public void deleteEntityData(UriInfo uriInfo, OiyoSettingsEntitySet entitySet, List<UriParameter> keyPredicates)
            throws ODataApplicationException {
        final OiyoSettingsDatabase database = OiyoInfoUtil.getOiyoDatabaseByEntitySetName(oiyoInfo,
                entitySet.getName());

        // [IY1073] INFO: ENTITY: DELETE
        log.info(OiyokanMessages.IY1073 + ": " + entitySet.getName());

        // データベースに接続.
        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(database)) {
            log.trace("[database transaction] WITHOUT database transaction.");
            deleteInternal(connTargetDb, uriInfo, entitySet, keyPredicates);
        } catch (SQLException ex) {
            // [M205] Fail to execute SQL.
            log.error(OiyokanMessages.IY3153 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.IY3153, //
                    OiyokanMessages.IY3153_CODE, Locale.ENGLISH);
        }
    }

    ////////////////////////
    // UPDATE (PATCH)
    // OiyokanはPUTをサポートしない

    /**
     * Update Entity data (PATCH).
     * 
     * @param uriInfo       URI info.
     * @param entitySet     EntitySet info.
     * @param keyPredicates Keys to update.
     * @param requestEntity Entity date for update.
     * @param ifMatch       Header If-Match. force UPDATE.
     * @param ifNoneMatch   Header If-None-Match. force INSERT.
     * @throws ODataApplicationException OData App exception occured.
     */
    public Entity updateEntityDataPatch(UriInfo uriInfo, OiyoSettingsEntitySet entitySet,
            List<UriParameter> keyPredicates, Entity requestEntity, final boolean ifMatch, final boolean ifNoneMatch)
            throws ODataApplicationException {
        final OiyoSettingsDatabase database = OiyoInfoUtil.getOiyoDatabaseByEntitySetName(oiyoInfo,
                entitySet.getName());

        /////////////////////////////////
        // KEYに autoGenKey があるかどうか確認
        boolean isAutoGenKeyIncludedInKey = false;
        for (String keyName : entitySet.getEntityType().getKeyName()) {
            for (OiyoSettingsProperty prop : entitySet.getEntityType().getProperty()) {
                if (prop.getName().equals(keyName)) {
                    if (prop.getAutoGenKey() != null && prop.getAutoGenKey()) {
                        // KEYにautoGenKeyが含まれる。
                        isAutoGenKeyIncludedInKey = true;
                    }
                }
            }
        }

        if (ifNoneMatch && isAutoGenKeyIncludedInKey) {
            // [IY3122] ERROR: If-None-Match NOT allowed because there is property that was
            // set as autoGenKey.
            log.warn(OiyokanMessages.IY3122);
            throw new ODataApplicationException(OiyokanMessages.IY3122, OiyokanMessages.IY3122_CODE, Locale.ENGLISH);
        }

        // データベースに接続.
        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(database)) {
            log.trace("[database transaction] BEGIN database transaction.");
            if (database.getAutoCommit() == null || database.getAutoCommit()) {
                // autoCommit 指定なし、または、true の場合、autoCommitをfalseに設定.
                log.trace("conn.setAutoCommit(false)");
                connTargetDb.setAutoCommit(false);
            }
            boolean isTranSuccessed = false;

            try {
                if (ifMatch || isAutoGenKeyIncludedInKey) {
                    // If-Match header が '*' 指定されたら UPDATE.
                    // KEYにautoGenKeyが含まれる場合も If-Match 指定と同様と扱って UPDATE.
                    // [IY1074] INFO: ENTITY: PATCH: UPDATE (If-Match)
                    log.info(OiyokanMessages.IY1074 + ": " + entitySet.getName());
                    updateInternal(connTargetDb, uriInfo, entitySet, keyPredicates, requestEntity);

                    // 更新後の Entity を読み込み.
                    final Entity entity = readInternal(connTargetDb, uriInfo, entitySet, keyPredicates);
                    lastPatchStatusCode = 200; /* READ */

                    // トランザクションを成功としてマーク.
                    isTranSuccessed = true;
                    return entity;
                } else if (ifNoneMatch) {
                    // If-None-Match header が '*' 指定されたら INSERT.
                    // [IY1075] INFO: ENTITY: PATCH: INSERT (If-None-Match)
                    log.info(OiyokanMessages.IY1075 + ": " + entitySet.getName());
                    final Entity entity = createInternal(connTargetDb, uriInfo, entitySet,
                            keyPredicates/* キーの与えられるパターン */, requestEntity);
                    lastPatchStatusCode = 201; /* CREATED */

                    // トランザクションを成功としてマーク.
                    isTranSuccessed = true;
                    return entity;
                } else {
                    // If-Match header も If-None-Match header も指定がない場合は UPSERT.
                    // [IY1076] INFO: ENTITY: PATCH: UPSERT
                    log.info(OiyokanMessages.IY1076 + ": " + entitySet.getName());

                    try {
                        // SELECT to check exists
                        readInternal(connTargetDb, uriInfo, entitySet, keyPredicates);

                        // 読み込み成功。対象のレコードの更新に着手
                        // UPDATE
                        updateInternal(connTargetDb, uriInfo, entitySet, keyPredicates, requestEntity);

                        // 更新後の Entity を読み込み.
                        final Entity entity = readInternal(connTargetDb, uriInfo, entitySet, keyPredicates);
                        lastPatchStatusCode = 200; /* READ */

                        // トランザクションを成功としてマーク.
                        isTranSuccessed = true;
                        return entity;
                    } catch (ODataApplicationException ex) {
                        // 404以外が返却は想定外でエラー。処理中断。
                        if (OiyokanMessages.IY3105_CODE != ex.getStatusCode()) {
                            // そのまま throw.
                            throw ex;
                        }

                        // 404 (該当レコードなし) であったので、新規にレコード作成.

                        // INSERT
                        final Entity entity = createInternal(connTargetDb, uriInfo, entitySet,
                                keyPredicates/* キーの与えられるパターン */, requestEntity);
                        lastPatchStatusCode = 201; /* CREATED */

                        // トランザクションを成功としてマーク.
                        isTranSuccessed = true;
                        return entity;
                    }
                }
            } finally {
                if (isTranSuccessed) {
                    log.trace("[database transaction] COMMIT database transaction.");
                    connTargetDb.commit();
                } else {
                    log.trace("[database transaction] ROLLBACK database transaction.");
                    connTargetDb.rollback();
                }

                log.trace("[database transaction] END database transaction.");
                if (database.getAutoCommit() == null || database.getAutoCommit()) {
                    // autoCommit 指定なし、または、true の場合、autoCommitをtrueに戻す。
                    log.trace("conn.setAutoCommit(true)");
                    connTargetDb.setAutoCommit(true);
                }
            }
        } catch (SQLException ex) {
            // [IY3154] Fail to update entity with SQL error.
            log.error(OiyokanMessages.IY3154 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.IY3154, OiyokanMessages.IY3154_CODE, Locale.ENGLISH);
        } catch (ODataApplicationException ex) {
            // [IY3108] Fail to update entity.
            log.error(OiyokanMessages.IY3108 + ": " + ex.toString());
            throw ex;
        }
    }

    /////////////////////////////////////////////////////////////////
    // Internal methods.

    Entity readInternal(Connection connTargetDb, UriInfo uriInfo, OiyoSettingsEntitySet entitySet,
            List<UriParameter> keyPredicates) throws ODataApplicationException {

        final OiyoSqlInfo sqlInfo = new OiyoSqlInfo(oiyoInfo, entitySet.getName());
        new OiyoSqlQueryOneBuilder(oiyoInfo, sqlInfo).buildSelectOneQuery(entitySet.getName(), keyPredicates);

        final String sql = sqlInfo.getSqlBuilder().toString();

        if (sqlInfo.getSelectColumnNameList().size() == 0) {
            // [IY7106] UNEXPECTED: At least one selected column is required.
            log.error(OiyokanMessages.IY7106);
            throw new ODataApplicationException(OiyokanMessages.IY7106, 500, Locale.ENGLISH);
        }

        // [IY1081] INFO: SQL single
        log.info(OiyokanMessages.IY1081 + ": " + sql);

        final long startMillisec = System.currentTimeMillis();
        try (var stmt = connTargetDb.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            final int jdbcStmtTimeout = (entitySet.getJdbcStmtTimeout() == null ? 30 : entitySet.getJdbcStmtTimeout());
            stmt.setQueryTimeout(jdbcStmtTimeout);

            int idxColumn = 1;
            for (OiyoSqlInfo.SqlParam look : sqlInfo.getSqlParamList()) {
                OiyoCommonJdbcBindParamUtil.bindPreparedParameter(stmt, idxColumn++, look);
            }

            stmt.executeQuery();
            final Entity ent = new Entity();
            try (var rset = stmt.getResultSet()) {
                if (!rset.next()) {
                    // [IY3105] WARN: No such Entity data
                    log.warn(OiyokanMessages.IY3105 + ": " + sql);
                    throw new ODataApplicationException(OiyokanMessages.IY3105 + ": " //
                            + sql, OiyokanMessages.IY3105_CODE, Locale.ENGLISH);
                }

                for (int index = 0; index < sqlInfo.getSelectColumnNameList().size(); index++) {
                    OiyoSettingsProperty oiyoProp = null;
                    for (OiyoSettingsProperty prop : entitySet.getEntityType().getProperty()) {
                        if (prop.getName().equals(sqlInfo.getSelectColumnNameList().get(index))) {
                            oiyoProp = prop;
                            break;
                        }
                    }
                    if (oiyoProp == null) {
                        // [IY3161] UNEXPECTED: OiyoSettingsProperty NOT found.
                        log.fatal(OiyokanMessages.IY3161 + ": " + sqlInfo.getSelectColumnNameList().get(index));
                        throw new ODataApplicationException(
                                OiyokanMessages.IY3161 + ": " + sqlInfo.getSelectColumnNameList().get(index), //
                                OiyokanMessages.IY3161_CODE, Locale.ENGLISH);
                    }

                    Property prop = OiyoCommonJdbcUtil.resultSet2Property(oiyoInfo, rset, index + 1, entitySet,
                            oiyoProp);
                    ent.addProperty(prop);
                }

                if (rset.next()) {
                    // [M215] UNEXPECTED: Too many rows found (readEntity)
                    log.fatal(OiyokanMessages.IY3112 + ": " + sql);
                    throw new ODataApplicationException(OiyokanMessages.IY3112 + ": " + sql, //
                            OiyokanMessages.IY3112_CODE, Locale.ENGLISH);
                }
            }

            final long endMillisec = System.currentTimeMillis();
            final long elapsed = endMillisec - startMillisec;
            if (elapsed >= 10) {
                // [IY1082] INFO: SQL: elapsed
                log.info(OiyokanMessages.IY1082 + ": " + (endMillisec - startMillisec));
            }

            OiyoBasicJdbcEntityCollectionBuilder.setEntityId(oiyoInfo, entitySet, ent);

            return ent;
        } catch (SQLTimeoutException ex) {
            // [IY3501] SQL timeout at query one
            log.error(OiyokanMessages.IY3501 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.IY3501 + ": " + sql, //
                    OiyokanMessages.IY3501_CODE, Locale.ENGLISH);
        } catch (SQLException ex) {
            if (ex.toString().indexOf("timed out") >= 0 /* SQL Server 2008 */) {
                // [IY3502] SQL timeout at query one
                log.error(OiyokanMessages.IY3502 + ": " + sql + ", " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.IY3502 + ": " + sql, //
                        OiyokanMessages.IY3502_CODE, Locale.ENGLISH);
            } else {
                // [IY3106] Fail to execute SQL (readEntity)
                log.error(OiyokanMessages.IY3106 + ": " + sql + ", " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.IY3106 + ": " + sql, //
                        OiyokanMessages.IY3106_CODE, Locale.ENGLISH);
            }
        }
    }

    Entity createInternal(Connection connTargetDb, UriInfo uriInfo, OiyoSettingsEntitySet entitySet,
            List<UriParameter> keyPredicatesInput, Entity requestEntity) throws ODataApplicationException {

        final OiyoSqlInfo sqlInfo = new OiyoSqlInfo(oiyoInfo, entitySet.getName());
        new OiyoSqlInsertOneBuilder(oiyoInfo, sqlInfo).buildInsertIntoDml(entitySet.getName(), keyPredicatesInput,
                requestEntity);

        final OiyokanConstants.DatabaseType databaseType = OiyoInfoUtil.getOiyoDatabaseTypeByEntitySetName(oiyoInfo,
                entitySet.getName());

        final List<String> generatedKeys = OiyoCommonJdbcUtil.executeDml(connTargetDb, sqlInfo, entitySet, true);
        // 生成されたキーをその後の処理に反映。
        final List<UriParameter> keyPredicatesAfter = new ArrayList<>();
        if (DatabaseType.ORCL18 == databaseType) {
            // ORCL18 の特殊ルール。ROWIDが戻るので決め打ちで検索.
            final UriParameterImpl newParam = new UriParameterImpl();
            newParam.setName("ROWID");
            newParam.setText(generatedKeys.get(0));
            keyPredicatesAfter.add(newParam);
        } else {
            // 最初に generatedKeys の対応づけを実施.
            int generatedKeyIndex = 0;
            for (OiyoSettingsProperty property : entitySet.getEntityType().getProperty()) {
                if (property.getAutoGenKey() != null && property.getAutoGenKey()) {
                    // 自動生成対象.
                    final UriParameterImpl newParam = new UriParameterImpl();
                    newParam.setName(property.getName());
                    try {
                        newParam.setText(generatedKeys.get(generatedKeyIndex++));
                    } catch (IndexOutOfBoundsException ex) {
                        // [IY3115] UNEXPECTED: Fail to map generated keys (autoGenKey) to new key.
                        log.error(OiyokanMessages.IY3115 + ": " + entitySet.getName(), ex);
                        throw new ODataApplicationException(OiyokanMessages.IY3115 + ": " + entitySet.getName(),
                                OiyokanMessages.IY3115_CODE, Locale.ENGLISH);
                    }
                    keyPredicatesAfter.add(newParam);
                }
            }

            // generatedKeys で対応づかなかった分はPOSTリクエストから導出
            KEYLOOP: for (String keyName : entitySet.getEntityType().getKeyName()) {
                String propValue = null;
                for (OiyoSettingsProperty property : entitySet.getEntityType().getProperty()) {
                    if (property.getAutoGenKey() != null && property.getAutoGenKey()) {
                        // すでに autoGenKeyから導出済み。スキップ。
                        continue KEYLOOP;
                    }
                }

                if (keyPredicatesInput != null) {
                    for (UriParameter look : keyPredicatesInput) {
                        if (look.getName().equals(keyName)) {
                            propValue = look.getText();
                            // TODO v2.x にて、ここで得られる getText() に文字列クオートが入るかどうか確認すること.
                        }
                    }
                }

                for (Property look : requestEntity.getProperties()) {
                    if (look.getName().equals(keyName)) {
                        if (look.getValue() instanceof java.util.Calendar) {
                            log.trace("TRACE: OiyoBasicJdbcEntityOneBuilder#createInternal: java.util.Calendar");
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
                    log.trace("TRACE: propKey:" + keyName + "に対応する入力なし.");
                    // [IY3114] UNEXPECTED: Can't retrieve PreparedStatement#getGeneratedKeys: Fail
                    // to map auto generated key field.
                    log.error(OiyokanMessages.IY3114 + ": " + keyName);
                    throw new ODataApplicationException(OiyokanMessages.IY3114 + ": " + keyName, //
                            OiyokanMessages.IY3114_CODE, Locale.ENGLISH);
                }

                final UriParameterImpl newParam = new UriParameterImpl();
                newParam.setName(keyName);
                newParam.setText(propValue);
                keyPredicatesAfter.add(newParam);
            }
        }

        // 更新後のデータをリロード.
        return readInternal(connTargetDb, uriInfo, entitySet, keyPredicatesAfter);
    }

    void updateInternal(Connection connTargetDb, UriInfo uriInfo, OiyoSettingsEntitySet entitySet,
            List<UriParameter> keyPredicates, Entity requestEntity) throws ODataApplicationException {

        final OiyoSqlInfo sqlInfo = new OiyoSqlInfo(oiyoInfo, entitySet.getName());

        // データベースに接続.
        try {
            new OiyoSqlUpdateOneBuilder(oiyoInfo, sqlInfo).buildUpdatePatchDml(entitySet.getName(), keyPredicates,
                    requestEntity);

            OiyoCommonJdbcUtil.executeDml(connTargetDb, sqlInfo, entitySet, false);
        } catch (ODataApplicationException ex) {
            // [IY3108] Fail to update entity.
            log.error(OiyokanMessages.IY3108 + ": " + ex.toString());
            throw ex;
        }
    }

    void deleteInternal(Connection connTargetDb, UriInfo uriInfo, OiyoSettingsEntitySet entitySet,
            List<UriParameter> keyPredicates) throws ODataApplicationException {
        final OiyoSqlInfo sqlInfo = new OiyoSqlInfo(oiyoInfo, entitySet.getName());
        new OiyoSqlDeleteOneBuilder(oiyoInfo, sqlInfo).buildDeleteDml(entitySet.getName(), keyPredicates);

        OiyoCommonJdbcUtil.executeDml(connTargetDb, sqlInfo, entitySet, false);
    }
}