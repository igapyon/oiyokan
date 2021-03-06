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
 * 実際に返却するデータ本体を組み上げるクラス.
 * 
 * EDM要素セットを入力に実際のデータを組み上げ.
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
     * 指定のEDM要素セットに対応する要素コレクションを作成.
     * 
     * @param edmEntitySet EDM要素セット.
     * @param uriInfo      SQL構築のデータ構造.
     * @return 要素コレクション.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public EntityCollection build(EdmEntitySet edmEntitySet, UriInfo uriInfo) throws ODataApplicationException {
        final EntityCollection entityCollection = new EntityCollection();

        OiyokanEdmProvider provider = new OiyokanEdmProvider(oiyoInfo);
        if (!edmEntitySet.getEntityContainer().getName().equals(provider.getEntityContainer().getName())) {
            // Container 名が不一致. 処理せずに戻します.
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
            // 処理対象外の要素セットです. 処理せずに戻します.
            return entityCollection;
        }

        // [IY1061] DEBUG: QUERY
        log.debug(OiyokanMessages.IY1061 + ": " + edmEntitySet.getName());

        //////////////////////////////////////////////
        // Oiyokan が対応しない処理を拒絶するための記述.
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

        // データベースに接続.
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
                // 件数カウントがONの場合は基本的にカウント処理を実行。
                if (uriInfo.getFilterOption() == null //
                        && oiyoEntitySet.getOmitCountAll() != null && oiyoEntitySet.getOmitCountAll().booleanValue()) {
                    // ただし、条件のない件数カウントの場合、つまり全件カウントについては、、omitCountAll が true の場合には検索をスキップ.
                    // [IY2101] INFO: Skip count all by omitCountAll option.
                    log.info(OiyokanMessages.IY2101);
                } else {
                    // $count.
                    processCountQuery(uriInfo, csdlEntitySet.getName(), connTargetDb, entityCollection);
                }
            }

            // 実際のデータ取得処理を実行。
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

        // 件数をカウントして設定。
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

        // 取得できたレコード件数を設定.
        entityCollection.setCount(countWithWhere);
    }

    /**
     * クエリを実行してエンティティの一覧を取得。直接は利用しないでください。
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
        // 特殊処理
        // EQで結ばれたPropertyを探す目的で WHERE処理を最初に実行.
        {
            log.trace("TRACE: Check $filter. Find property used EQ and remember.");
            final OiyoSqlInfo sqlInfoDummy = new OiyoSqlInfo(oiyoInfo, entitySet.getName());

            if (uriInfo.getFilterOption() != null) {
                new OiyoSqlQueryListExpr(oiyoInfo, sqlInfoDummy).expand(uriInfo.getFilterOption().getExpression());
            }
            for (OiyoSettingsProperty prop : sqlInfoDummy.getBinaryOperatorEqPropertyList()) {
                // 1パス目で見つかった property を 2パス目の OiyoSqlInfo に複写.
                // 同名のものがあれば追加は抑止.
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

        // UriInfo 情報を元に SQL文を組み立て.
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
                // 組み立て後のバインド変数を PreparedStatement にセット.
                int column = 1;
                for (OiyoSqlInfo.SqlParam look : sqlInfo.getSqlParamList()) {
                    OiyoCommonJdbcBindParamUtil.bindPreparedParameter(pstmt, column++, look);
                }
                // 検索を実行.
                pstmt.executeQuery();
                prepRset = pstmt.getResultSet();
            }
            // 検索結果を取得.
            try (var rset = prepRset) {
                for (; rset.next();) {
                    final Entity ent = new Entity();
                    for (int index = 0; index < sqlInfo.getSelectColumnNameList().size(); index++) {
                        log.trace("TRACE: Bind parameter:" + sqlInfo.getSelectColumnNameList().get(index));
                        // 取得された検索結果を Property に組み替え.
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
     * 与えられた情報をもとにURIを作成.
     * 
     * @param entitySetName 要素セット名.
     * @param id            ユニーク性を実現するId.
     * @return 要素セット名およびユニーク性を実現するIdをもとにつくられた部分的なURI.
     */
    private static URI createId(OiyoInfo oiyoInfo, String entitySetName, Object id) {
        try {
            // 事前に BasicUrlUtil.encodeUrl4Key() が実施されていること。
            return new URI(oiyoInfo.getRawBaseUri() + "/" + entitySetName + "(" + id + ")");
        } catch (URISyntaxException ex) {
            // [M018] UNEXPECTED: Fail to create ID EntitySet name
            log.fatal(OiyokanMessages.IY2105 + ": " + entitySetName + ": " + ex.toString(), ex);
            throw new ODataRuntimeException(OiyokanMessages.IY2105 + ": " + entitySetName);
        }
    }

    /**
     * 与えられた情報をもとに Entityに IDをセット.
     * 
     * @param oiyoInfo  OiyoInfo instance.
     * @param entitySet EntitySet info.
     * @param ent       output Entity.
     */
    public static void setEntityId(OiyoInfo oiyoInfo, OiyoSettingsEntitySet entitySet, Entity ent) {
        if (entitySet.getEntityType().getKeyName().size() == 0) {
            // キーが存在しないのは OData としてはまずい。
            // 別の箇所にて標準エラー出力にて報告。
        } else {
            // キーが存在する場合は、キーの値を元にIDとして設定。
            if (entitySet.getEntityType().getKeyName().size() == 1) {
                // 単一項目によるキー
                final Property prop = ent.getProperty(entitySet.getEntityType().getKeyName().get(0));
                String idVal = prop.getValue().toString();
                if ("Edm.String".equals(prop.getType())) {
                    // TODO v2.x にて Property の値を文字列に変換する共通関数を作成.
                    idVal = "'" + OiyoUrlUtil.encodeUrl4Key(idVal) + "'";
                }
                ent.setId(createId(oiyoInfo, entitySet.getName(), idVal));
            } else {
                // 複数項目によるキー
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
                        // TODO v2.x にて Property の値を文字列に変換する共通関数を作成.
                        idVal = "'" + OiyoUrlUtil.encodeUrl4Key(idVal) + "'";
                    }
                    keyString += idVal;
                }
                ent.setId(OiyoBasicJdbcEntityCollectionBuilder.createId(oiyoInfo, entitySet.getName(), keyString));
            }
        }
    }
}
