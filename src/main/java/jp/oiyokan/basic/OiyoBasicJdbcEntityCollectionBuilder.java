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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Locale;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanCsdlEntitySet;
import jp.oiyokan.OiyokanEdmProvider;
import jp.oiyokan.OiyokanEntityCollectionBuilderInterface;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.basic.sql.OiyoSqlQueryListBuilder;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.h2.data.OiyoExperimentalH2FullTextSearch;
import jp.oiyokan.settings.OiyoSettingsUtil;

/**
 * 実際に返却するデータ本体を組み上げるクラス.
 * 
 * EDM要素セットを入力に実際のデータを組み上げ.
 */
public class OiyoBasicJdbcEntityCollectionBuilder implements OiyokanEntityCollectionBuilderInterface {
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

        OiyokanEdmProvider provider = new OiyokanEdmProvider();
        if (!edmEntitySet.getEntityContainer().getName().equals(provider.getEntityContainer().getName())) {
            // Container 名が不一致. 処理せずに戻します.
            return entityCollection;
        }

        OiyokanCsdlEntitySet entitySet = null;
        for (CsdlEntitySet look : provider.getEntityContainer().getEntitySets()) {
            if (edmEntitySet.getName().equals(look.getName())) {
                entitySet = (OiyokanCsdlEntitySet) look;
                break;
            }
        }

        if (entitySet == null) {
            // 処理対象外の要素セットです. 処理せずに戻します.
            return entityCollection;
        }

        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: TRACE: QUERY: " + edmEntitySet.getName());

        //////////////////////////////////////////////
        // Oiyokan が対応しない処理を拒絶するための記述.
        if (uriInfo.getSearchOption() != null && !OiyokanConstants.IS_EXPERIMENTAL_SEARCH_ENABLED) {
            // [M032] NOT SUPPORTED: URI: $search
            System.err.println(OiyokanMessages.M032);
            throw new ODataApplicationException(OiyokanMessages.M032, OiyokanMessages.M032_CODE, Locale.ENGLISH);
        }
        if (uriInfo.getApplyOption() != null) {
            // [M011] NOT SUPPORTED: URI: $apply
            System.err.println(OiyokanMessages.M011);
            throw new ODataApplicationException(OiyokanMessages.M011, OiyokanMessages.M011_CODE, Locale.ENGLISH);
        }
        if (uriInfo.getCustomQueryOptions() != null && uriInfo.getCustomQueryOptions().size() > 0) {
            // [M012] NOT SUPPORTED: URI: customQuery
            System.err.println(OiyokanMessages.M012);
            throw new ODataApplicationException(OiyokanMessages.M012, OiyokanMessages.M012_CODE, Locale.ENGLISH);
        }
        if (uriInfo.getDeltaTokenOption() != null) {
            // [M013] NOT SUPPORTED: URI: deltaToken
            System.err.println(OiyokanMessages.M013);
            throw new ODataApplicationException(OiyokanMessages.M013, OiyokanMessages.M013_CODE, Locale.ENGLISH);
        }
        if (uriInfo.getExpandOption() != null && uriInfo.getExpandOption().getExpandItems().size() > 0) {
            // [M014] NOT SUPPORTED: URI: $expand
            System.err.println(OiyokanMessages.M014);
            throw new ODataApplicationException(OiyokanMessages.M014, OiyokanMessages.M014_CODE, Locale.ENGLISH);
        }

        // データベースに接続.
        try (Connection connTargetDb = OiyoBasicJdbcUtil.getConnection(entitySet.getSettingsDatabase(oiyoInfo))) {
            if (uriInfo.getSearchOption() != null) {
                // $search.
                new OiyoExperimentalH2FullTextSearch().process(connTargetDb, edmEntitySet, uriInfo, entityCollection);
                return entityCollection;
            }

            final OiyoSettingsEntitySet oiyoEntitySet = OiyoSettingsUtil.getOiyoEntitySet(oiyoInfo,
                    edmEntitySet.getName());

            if (uriInfo.getCountOption() != null && uriInfo.getCountOption().getValue()) {
                // 件数カウントがONの場合は基本的にカウント処理を実行。
                if (uriInfo.getFilterOption() == null //
                        && oiyoEntitySet.getOmitCountAll() != null && oiyoEntitySet.getOmitCountAll().booleanValue()) {
                    // ただし、条件のない件数カウントの場合、つまり全件カウントについては、、omitCountAll が true の場合には検索をスキップ.
                    System.err.println(OiyokanMessages.M042);
                } else {
                    // $count.
                    processCountQuery(entitySet, uriInfo, connTargetDb, entityCollection);
                }
            }

            // 実際のデータ取得処理を実行。
            processCollectionQuery(entitySet, uriInfo, connTargetDb, entityCollection);

            return entityCollection;
        } catch (

        SQLException ex) {
            // [M015] UNEXPECTED: An error occurred in SQL that counts the number of search
            // results.
            System.err.println(OiyokanMessages.M015 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M015, 500, Locale.ENGLISH);
        }
    }

    private void processCountQuery(OiyokanCsdlEntitySet entitySet, UriInfo uriInfo, Connection connTargetDb,
            EntityCollection entityCollection) throws ODataApplicationException {
        // 件数をカウントして設定。
        OiyoSqlQueryListBuilder basicSqlBuilder = new OiyoSqlQueryListBuilder(oiyoInfo, entitySet);
        basicSqlBuilder.buildSelectCountQuery(uriInfo);
        final String sql = basicSqlBuilder.getSqlInfo().getSqlBuilder().toString();

        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: TRACE: COUNT: " + sql);

        int countWithWhere = 0;
        final long startMillisec = System.currentTimeMillis();
        try (var stmt = connTargetDb.prepareStatement(sql)) {
            // set query timeout
            stmt.setQueryTimeout(OiyokanConstants.JDBC_STMT_TIMEOUT);

            int column = 1;
            for (Object look : basicSqlBuilder.getSqlInfo().getSqlParamList()) {
                OiyoBasicJdbcUtil.bindPreparedParameter(stmt, column++, look);
            }

            stmt.executeQuery();
            var rset = stmt.getResultSet();
            rset.next();
            countWithWhere = rset.getInt(1);
        } catch (SQLTimeoutException ex) {
            // [M035] SQL timeout at count
            System.err.println(OiyokanMessages.M035 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M035 + ": " + sql, //
                    OiyokanMessages.M035_CODE, Locale.ENGLISH);
        } catch (SQLException ex) {
            // [M015] UNEXPECTED: An error occurred in SQL that counts the number of search
            // results.
            System.err.println(OiyokanMessages.M015 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M015 + ": " + sql, 500, Locale.ENGLISH);
        }

        final long endMillisec = System.currentTimeMillis();
        if (OiyokanConstants.IS_TRACE_ODATA_V4) {
            final long elapsed = endMillisec - startMillisec;
            System.err.println("OData v4: TRACE: COUNT = " + countWithWhere //
                    + (elapsed >= 10 ? " (elapsed: " + (endMillisec - startMillisec) + ")" : ""));
        }

        // 取得できたレコード件数を設定.
        entityCollection.setCount(countWithWhere);
    }

    /**
     * クエリを実行してエンティティの一覧を取得。直接は利用しないでください。
     * 
     * @param entitySet        instance of OiyokanCsdlEntitySet.
     * @param uriInfo          instance of
     *                         org.apache.olingo.server.core.uri.UriInfoImpl.
     * @param connTargetDb     Connection of db.
     * @param entityCollection result of search.
     * @throws ODataApplicationException OData App Exception occured.
     */
    public void processCollectionQuery(OiyokanCsdlEntitySet entitySet, UriInfo uriInfo, Connection connTargetDb,
            EntityCollection entityCollection) throws ODataApplicationException {
        OiyoSqlQueryListBuilder basicSqlBuilder = new OiyoSqlQueryListBuilder(oiyoInfo, entitySet);

        // UriInfo 情報を元に SQL文を組み立て.
        basicSqlBuilder.buildSelectQuery(uriInfo);
        final String sql = basicSqlBuilder.getSqlInfo().getSqlBuilder().toString();

        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: TRACE: SQL: " + sql);

        final long startMillisec = System.currentTimeMillis();
        try (var stmt = connTargetDb.prepareStatement(sql)) {
            // set query timeout
            stmt.setQueryTimeout(OiyokanConstants.JDBC_STMT_TIMEOUT);

            // 組み立て後のバインド変数を PreparedStatement にセット.
            int idxColumn = 1;
            for (Object look : basicSqlBuilder.getSqlInfo().getSqlParamList()) {
                OiyoBasicJdbcUtil.bindPreparedParameter(stmt, idxColumn++, look);
            }

            // 検索を実行.
            stmt.executeQuery();

            // 検索結果を取得.
            var rset = stmt.getResultSet();
            ResultSetMetaData rsmeta = null;
            for (; rset.next();) {
                if (rsmeta == null) {
                    rsmeta = rset.getMetaData();
                }
                final Entity ent = new Entity();
                for (int column = 1; column <= rsmeta.getColumnCount(); column++) {
                    // 取得された検索結果を Property に組み替え.
                    Property prop = OiyoBasicJdbcUtil.resultSet2Property(oiyoInfo, rset, rsmeta, column, entitySet);
                    ent.addProperty(prop);
                }

                if (entitySet.getEntityType().getKey().size() == 0) {
                    // キーが存在しないのは OData としてはまずい。
                    // 別の箇所にて標準エラー出力にて報告。
                } else {
                    // キーが存在する場合は、キーの値を元にIDとして設定。
                    OiyokanCsdlEntitySet iyoEntitySet = (OiyokanCsdlEntitySet) entitySet;
                    if (iyoEntitySet.getEntityType().getKey().size() == 1) {
                        // 単一項目によるキー
                        final Property prop = ent.getProperty(iyoEntitySet.getEntityType().getKey().get(0).getName());
                        String idVal = prop.getValue().toString();
                        if ("Edm.String".equals(prop.getType())) {
                            // TODO FIXME Property の値を文字列に変換する共通関数を期待したい.
                            idVal = "'" + OiyoBasicUrlUtil.encodeUrl4Key(idVal) + "'";
                        }
                        ent.setId(createId(entitySet.getName(), idVal));
                    } else {
                        // 複数項目によるキー
                        String keyString = "";
                        boolean isFirst = true;
                        for (CsdlPropertyRef propRef : iyoEntitySet.getEntityType().getKey()) {
                            if (isFirst) {
                                isFirst = false;
                            } else {
                                keyString += ",";
                            }
                            final Property prop = ent.getProperty(propRef.getName());
                            keyString += prop.getName() + "=";
                            String idVal = prop.getValue().toString();
                            if ("Edm.String".equals(prop.getType())) {
                                // TODO FIXME Property の値を文字列に変換する共通関数を期待したい.
                                idVal = "'" + OiyoBasicUrlUtil.encodeUrl4Key(idVal) + "'";
                            }
                            keyString += idVal;
                        }
                        ent.setId(createId(entitySet.getName(), keyString));
                    }
                }

                entityCollection.getEntities().add(ent);
            }

            final long endMillisec = System.currentTimeMillis();
            if (OiyokanConstants.IS_TRACE_ODATA_V4) {
                final long elapsed = endMillisec - startMillisec;
                if (elapsed >= 10) {
                    System.err.println("OData v4: TRACE: SQL: elapsed: " + (endMillisec - startMillisec));
                }
            }
        } catch (SQLTimeoutException ex) {
            // [M036] SQL timeout at execute
            System.err.println(OiyokanMessages.M036 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M036 + ": " + sql, //
                    OiyokanMessages.M036_CODE, Locale.ENGLISH);
        } catch (SQLException ex) {
            // ex.printStackTrace();
            // [M017] Fail to execute SQL
            System.err.println(OiyokanMessages.M017 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M017 + ": " + sql, //
                    OiyokanMessages.M017_CODE, Locale.ENGLISH);
        }
    }

    /**
     * 与えられた情報をもとにURIを作成.
     * 
     * @param entitySetName 要素セット名.
     * @param id            ユニーク性を実現するId.
     * @return 要素セット名およびユニーク性を実現するIdをもとにつくられた部分的なURI.
     */
    public static URI createId(String entitySetName, Object id) {
        try {
            // 事前に BasicUrlUtil.encodeUrl4Key() が実施されていること。
            return new URI(entitySetName + "(" + id + ")");
        } catch (URISyntaxException ex) {
            // [M018] UNEXPECTED: Fail to create ID EntitySet name
            System.err.println(OiyokanMessages.M018 + ": " + entitySetName + ": " + ex.toString());
            ex.printStackTrace();
            throw new ODataRuntimeException(OiyokanMessages.M018 + ": " + entitySetName);
        }
    }
}
