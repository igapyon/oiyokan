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

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanEdmProvider;
import jp.oiyokan.OiyokanEntityCollectionBuilderInterface;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.basic.sql.OiyoSqlQueryListBuilder;
import jp.oiyokan.basic.sql.OiyoSqlQueryListExpr;
import jp.oiyokan.common.OiyoCommonJdbcBindParamUtil;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.common.OiyoSqlInfo;
import jp.oiyokan.common.OiyoUrlUtil;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsProperty;
import jp.oiyokan.h2.data.OiyoExperimentalH2FullTextSearch;

/**
 * ???????????????????????????????????????????????????????????????.
 * 
 * EDM????????????????????????????????????????????????????????????.
 */
public class OiyoBasicJdbcEntityCollectionBuilder implements OiyokanEntityCollectionBuilderInterface {
    private static final Log log = LogFactory.getLog(OiyoBasicJdbcEntityCollectionBuilder.class);

    /**
     * Oiyokan Info.
     */
    private OiyoInfo oiyoInfo;

    public OiyoBasicJdbcEntityCollectionBuilder(OiyoInfo oiyoInfo) {
        this.oiyoInfo = oiyoInfo;
    }

    /**
     * ?????????EDM???????????????????????????????????????????????????????????????.
     * 
     * @param edmEntitySet EDM???????????????.
     * @param uriInfo      SQL????????????????????????.
     * @return ????????????????????????.
     * @throws ODataApplicationException OData????????????????????????????????????.
     */
    public EntityCollection build(EdmEntitySet edmEntitySet, UriInfo uriInfo) throws ODataApplicationException {
        final EntityCollection entityCollection = new EntityCollection();

        OiyokanEdmProvider provider = new OiyokanEdmProvider(oiyoInfo);
        if (!edmEntitySet.getEntityContainer().getName().equals(provider.getEntityContainer().getName())) {
            // Container ???????????????. ???????????????????????????.
            return entityCollection;
        }

        CsdlEntitySet csdlEntitySet = null;
        for (CsdlEntitySet look : provider.getEntityContainer().getEntitySets()) {
            if (edmEntitySet.getName().equals(look.getName())) {
                csdlEntitySet = look;
                break;
            }
        }

        if (csdlEntitySet == null) {
            // ???????????????????????????????????????. ???????????????????????????.
            return entityCollection;
        }

        // [IY1061] DEBUG: QUERY
        log.debug(OiyokanMessages.IY1061 + ": " + edmEntitySet.getName());

        //////////////////////////////////////////////
        // Oiyokan ??????????????????????????????????????????????????????.
        if (uriInfo.getSearchOption() != null && !OiyokanConstants.IS_EXPERIMENTAL_SEARCH_ENABLED) {
            // [M032] NOT SUPPORTED: URI: $search
            log.error(OiyokanMessages.IY1107);
            throw new ODataApplicationException(OiyokanMessages.IY1107, OiyokanMessages.IY1107_CODE, Locale.ENGLISH);
        }
        if (uriInfo.getApplyOption() != null) {
            // [IY1102] NOT SUPPORTED: URI: $apply
            log.error(OiyokanMessages.IY1102);
            throw new ODataApplicationException(OiyokanMessages.IY1102, OiyokanMessages.IY1102_CODE, Locale.ENGLISH);
        }
        if (uriInfo.getCustomQueryOptions() != null && uriInfo.getCustomQueryOptions().size() > 0) {
            // [IY1103] NOT SUPPORTED: URI: customQuery
            log.error(OiyokanMessages.IY1103);
            throw new ODataApplicationException(OiyokanMessages.IY1103, OiyokanMessages.IY1103_CODE, Locale.ENGLISH);
        }
        if (uriInfo.getDeltaTokenOption() != null) {
            // [IY1104] NOT SUPPORTED: URI: deltaToken
            log.error(OiyokanMessages.IY1104);
            throw new ODataApplicationException(OiyokanMessages.IY1104, OiyokanMessages.IY1104_CODE, Locale.ENGLISH);
        }
        if (uriInfo.getExpandOption() != null && uriInfo.getExpandOption().getExpandItems().size() > 0) {
            // [M014] NOT SUPPORTED: URI: $expand
            log.error(OiyokanMessages.IY1105);
            throw new ODataApplicationException(OiyokanMessages.IY1105, OiyokanMessages.IY1105_CODE, Locale.ENGLISH);
        }

        // ???????????????????????????.
        final OiyoSettingsDatabase database = OiyoInfoUtil.getOiyoDatabaseByEntitySetName(oiyoInfo,
                edmEntitySet.getName());
        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(database)) {
            if (uriInfo.getSearchOption() != null) {
                // $search.
                new OiyoExperimentalH2FullTextSearch().process(connTargetDb, edmEntitySet, uriInfo, entityCollection);
                return entityCollection;
            }

            final OiyoSettingsEntitySet oiyoEntitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, edmEntitySet.getName());

            if (uriInfo.getCountOption() != null && uriInfo.getCountOption().getValue()) {
                // ?????????????????????ON??????????????????????????????????????????????????????
                if (uriInfo.getFilterOption() == null //
                        && oiyoEntitySet.getOmitCountAll() != null && oiyoEntitySet.getOmitCountAll().booleanValue()) {
                    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????omitCountAll ??? true ????????????????????????????????????.
                    // [IY2101] INFO: Skip count all by omitCountAll option.
                    log.info(OiyokanMessages.IY2101);
                } else {
                    // $count.
                    processCountQuery(uriInfo, csdlEntitySet.getName(), connTargetDb, entityCollection);
                }
            }

            // ??????????????????????????????????????????
            processCollectionQuery(uriInfo, csdlEntitySet.getName(), connTargetDb, entityCollection);

            return entityCollection;
        } catch (SQLException ex) {
            // [M015] UNEXPECTED: An error occurred in SQL that counts the number of search
            // results.
            log.error(OiyokanMessages.IY2103 + ": " + ex.toString(), ex);
            throw new ODataApplicationException(OiyokanMessages.IY2103, 500, Locale.ENGLISH);
        }
    }

    private void processCountQuery(UriInfo uriInfo, String entitySetName, Connection connTargetDb,
            EntityCollection entityCollection) throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, entitySetName);

        // ????????????????????????????????????
        OiyoSqlQueryListBuilder basicSqlBuilder = new OiyoSqlQueryListBuilder(oiyoInfo, entitySetName);
        basicSqlBuilder.buildSelectCountQuery(uriInfo);
        final String sql = basicSqlBuilder.getSqlInfo().getSqlBuilder().toString();

        // [IY1062] INFO: COUNT
        log.info(OiyokanMessages.IY1062 + ": " + sql);

        int countWithWhere = 0;
        final long startMillisec = System.currentTimeMillis();
        try (var stmt = (basicSqlBuilder.getSqlInfo().getSqlParamList().size() == 0 //
                ? connTargetDb.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
                : connTargetDb.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY))) {
            final int jdbcStmtTimeout = (entitySet.getJdbcStmtTimeout() == null ? 30 : entitySet.getJdbcStmtTimeout());
            stmt.setQueryTimeout(jdbcStmtTimeout);

            ResultSet prepRset = null;
            if (basicSqlBuilder.getSqlInfo().getSqlParamList().size() == 0) {
                prepRset = stmt.executeQuery(sql);
            } else {
                final PreparedStatement pstmt = (PreparedStatement) stmt;
                int column = 1;
                for (OiyoSqlInfo.SqlParam look : basicSqlBuilder.getSqlInfo().getSqlParamList()) {
                    OiyoCommonJdbcBindParamUtil.bindPreparedParameter(pstmt, column++, look);
                }
                pstmt.executeQuery();
                prepRset = pstmt.getResultSet();
            }
            try (var rset = prepRset) {
                rset.next();
                countWithWhere = rset.getInt(1);
            }
        } catch (SQLTimeoutException ex) {
            // [IY2501] SQL timeout at count query
            log.error(OiyokanMessages.IY2501 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.IY2501 + ": " + sql, //
                    OiyokanMessages.IY2501_CODE, Locale.ENGLISH);
        } catch (SQLException ex) {
            if (ex.toString().indexOf("timed out") >= 0 /* SQL Server 2008 */) {
                // [IY2502] SQL timeout at count query
                log.error(OiyokanMessages.IY2502 + ": " + sql + ", " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.IY2502 + ": " + sql, //
                        OiyokanMessages.IY2502_CODE, Locale.ENGLISH);
            } else {
                // [IY2104] UNEXPECTED: An error occurred in SQL that counts the number of
                // search results.
                log.error(OiyokanMessages.IY2104 + ": " + sql + ", " + ex.toString(), ex);
                throw new ODataApplicationException(OiyokanMessages.IY2104 + ": " + sql, 500, Locale.ENGLISH);
            }
        }

        final long endMillisec = System.currentTimeMillis();
        final long elapsed = endMillisec - startMillisec;
        // [IY1063] INFO: COUNT =
        log.info(OiyokanMessages.IY1063 + countWithWhere //
                + (elapsed >= 10 ? " (elapsed: " + (endMillisec - startMillisec) + ")" : ""));

        // ??????????????????????????????????????????.
        entityCollection.setCount(countWithWhere);
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????
     * 
     * @param uriInfo          instance of
     *                         org.apache.olingo.server.core.uri.UriInfoImpl.
     * @param entitySetName    Name of EntitySet.
     * @param connTargetDb     Connection of db.
     * @param entityCollection result of search.
     * @throws ODataApplicationException OData App Exception occured.
     */
    public void processCollectionQuery(UriInfo uriInfo, String entitySetName, Connection connTargetDb,
            EntityCollection entityCollection) throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, entitySetName);

        OiyoSqlQueryListBuilder basicSqlBuilder = new OiyoSqlQueryListBuilder(oiyoInfo, entitySetName);

        /////////////
        // ????????????
        // EQ???????????????Property?????????????????? WHERE????????????????????????.
        {
            log.trace("TRACE: Check $filter. Find property used EQ and remember.");
            final OiyoSqlInfo sqlInfoDummy = new OiyoSqlInfo(oiyoInfo, entitySet.getName());

            if (uriInfo.getFilterOption() != null) {
                new OiyoSqlQueryListExpr(oiyoInfo, sqlInfoDummy).expand(uriInfo.getFilterOption().getExpression());
            }
            for (OiyoSettingsProperty prop : sqlInfoDummy.getBinaryOperatorEqPropertyList()) {
                // 1??????????????????????????? property ??? 2???????????? OiyoSqlInfo ?????????.
                // ??????????????????????????????????????????.
                boolean isAlreadyAdded = false;
                for (OiyoSettingsProperty look : basicSqlBuilder.getSqlInfo().getBinaryOperatorEqPropertyList()) {
                    if (look.getName().equals(prop.getName())) {
                        isAlreadyAdded = true;
                    }
                }
                if (isAlreadyAdded == false) {
                    log.trace("TRACE: Copy property to main OiyoSqlInfo.: " + prop.getName());
                    basicSqlBuilder.getSqlInfo().getBinaryOperatorEqPropertyList().add(prop);
                }
            }
        }

        // UriInfo ??????????????? SQL??????????????????.
        basicSqlBuilder.buildSelectQuery(uriInfo);
        final String sql = basicSqlBuilder.getSqlInfo().getSqlBuilder().toString();
        final OiyoSqlInfo sqlInfo = basicSqlBuilder.getSqlInfo();

        if (sqlInfo.getSelectColumnNameList().size() == 0) {
            // [IY7105] UNEXPECTED: At least one selected column is required.
            log.error(OiyokanMessages.IY7105);
            throw new ODataApplicationException(OiyokanMessages.IY7105, 500, Locale.ENGLISH);
        }

        // [IY1064] INFO: SQL collect
        log.info(OiyokanMessages.IY1064 + ": " + sql);

        final long startMillisec = System.currentTimeMillis();
        try (var stmt = (basicSqlBuilder.getSqlInfo().getSqlParamList().size() == 0 //
                ? connTargetDb.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
                : connTargetDb.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY))) {
            if (entitySet.getJdbcFetchSize() != null) {
                // [IY1068] DEBUG: JDBC: setFetchSize
                log.debug(OiyokanMessages.IY1068 + ": " + entitySet.getJdbcFetchSize());
                stmt.setFetchSize(entitySet.getJdbcFetchSize());
            }

            final int jdbcStmtTimeout = (entitySet.getJdbcStmtTimeout() == null ? 30 : entitySet.getJdbcStmtTimeout());
            stmt.setQueryTimeout(jdbcStmtTimeout);

            ResultSet prepRset = null;
            if (basicSqlBuilder.getSqlInfo().getSqlParamList().size() == 0) {
                prepRset = stmt.executeQuery(sql);
            } else {
                final PreparedStatement pstmt = (PreparedStatement) stmt;
                // ??????????????????????????????????????? PreparedStatement ????????????.
                int column = 1;
                for (OiyoSqlInfo.SqlParam look : sqlInfo.getSqlParamList()) {
                    OiyoCommonJdbcBindParamUtil.bindPreparedParameter(pstmt, column++, look);
                }
                // ???????????????.
                pstmt.executeQuery();
                prepRset = pstmt.getResultSet();
            }
            // ?????????????????????.
            try (var rset = prepRset) {
                for (; rset.next();) {
                    final Entity ent = new Entity();
                    for (int index = 0; index < sqlInfo.getSelectColumnNameList().size(); index++) {
                        log.trace("TRACE: Bind parameter:" + sqlInfo.getSelectColumnNameList().get(index));
                        // ?????????????????????????????? Property ???????????????.
                        OiyoSettingsProperty oiyoProp = OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySetName,
                                sqlInfo.getSelectColumnNameList().get(index));
                        Property prop = OiyoCommonJdbcUtil.resultSet2Property(oiyoInfo, rset, index + 1, entitySet,
                                oiyoProp);
                        ent.addProperty(prop);
                    }

                    setEntityId(oiyoInfo, entitySet, ent);

                    entityCollection.getEntities().add(ent);
                }
            }

            final long endMillisec = System.currentTimeMillis();
            final long elapsed = endMillisec - startMillisec;
            if (elapsed >= 10) {
                // [IY1065] INFO: SQL collect: elapsed
                log.info(OiyokanMessages.IY1065 + ": " + (endMillisec - startMillisec));
            }
        } catch (SQLTimeoutException ex) {
            // [IY2511] SQL timeout at exec query
            log.error(OiyokanMessages.IY2511 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.IY2511 + ": " + sql, //
                    OiyokanMessages.IY2511_CODE, Locale.ENGLISH);
        } catch (SQLException ex) {
            if (ex.toString().indexOf("timed out") >= 0 /* SQL Server 2008 */) {
                // [IY2512] SQL timeout at exec query
                log.error(OiyokanMessages.IY2512 + ": " + sql + ", " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.IY2512 + ": " + sql, //
                        OiyokanMessages.IY2512_CODE, Locale.ENGLISH);
            } else {
                // [IY2102] Fail to execute SQL
                log.error(OiyokanMessages.IY2102 + ": " + sql + ", " + ex.toString(), ex);
                throw new ODataApplicationException(OiyokanMessages.IY2102 + ": " + sql, //
                        OiyokanMessages.IY2102_CODE, Locale.ENGLISH);
            }
        }
    }

    /**
     * ?????????????????????????????????URI?????????.
     * 
     * @param entitySetName ??????????????????.
     * @param id            ??????????????????????????????Id.
     * @return ?????????????????????????????????????????????????????????Id???????????????????????????????????????URI.
     */
    private static URI createId(OiyoInfo oiyoInfo, String entitySetName, Object id) {
        try {
            // ????????? BasicUrlUtil.encodeUrl4Key() ?????????????????????????????????
            return new URI(oiyoInfo.getRawBaseUri() + "/" + entitySetName + "(" + id + ")");
        } catch (URISyntaxException ex) {
            // [M018] UNEXPECTED: Fail to create ID EntitySet name
            log.fatal(OiyokanMessages.IY2105 + ": " + entitySetName + ": " + ex.toString(), ex);
            throw new ODataRuntimeException(OiyokanMessages.IY2105 + ": " + entitySetName);
        }
    }

    /**
     * ????????????????????????????????? Entity??? ID????????????.
     * 
     * @param oiyoInfo  OiyoInfo instance.
     * @param entitySet EntitySet info.
     * @param ent       output Entity.
     */
    public static void setEntityId(OiyoInfo oiyoInfo, OiyoSettingsEntitySet entitySet, Entity ent) {
        if (entitySet.getEntityType().getKeyName().size() == 0) {
            // ?????????????????????????????? OData ????????????????????????
            // ??????????????????????????????????????????????????????
        } else {
            // ??????????????????????????????????????????????????????ID??????????????????
            if (entitySet.getEntityType().getKeyName().size() == 1) {
                // ???????????????????????????
                final Property prop = ent.getProperty(entitySet.getEntityType().getKeyName().get(0));
                String idVal = prop.getValue().toString();
                if ("Edm.String".equals(prop.getType())) {
                    // TODO v2.x ?????? Property ??????????????????????????????????????????????????????.
                    idVal = "'" + OiyoUrlUtil.encodeUrl4Key(idVal) + "'";
                }
                ent.setId(createId(oiyoInfo, entitySet.getName(), idVal));
            } else {
                // ???????????????????????????
                String keyString = "";
                boolean isFirst = true;
                for (String keyName : entitySet.getEntityType().getKeyName()) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        keyString += ",";
                    }
                    final Property prop = ent.getProperty(keyName);
                    keyString += prop.getName() + "=";
                    String idVal = prop.getValue().toString();
                    if ("Edm.String".equals(prop.getType())) {
                        // TODO v2.x ?????? Property ??????????????????????????????????????????????????????.
                        idVal = "'" + OiyoUrlUtil.encodeUrl4Key(idVal) + "'";
                    }
                    keyString += idVal;
                }
                ent.setId(OiyoBasicJdbcEntityCollectionBuilder.createId(oiyoInfo, entitySet.getName(), keyString));
            }
        }
    }
}
