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
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.basic.sql.BasicSqlBuilder;
import jp.oiyokan.h2.data.ExperimentalH2FullTextSearch;

/**
 * 実際に返却するデータ本体を組み上げるクラス.
 * 
 * EDM要素セットを入力に実際のデータを組み上げ.
 */
public class BasicJdbcEntityCollectionBuilder {
    private BasicJdbcEntityCollectionBuilder() {
    }

    /**
     * 指定のEDM要素セットに対応する要素コレクションを作成.
     * 
     * @param edmEntitySet EDM要素セット.
     * @param uriInfo      SQL構築のデータ構造.
     * @return 要素コレクション.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static EntityCollection build(EdmEntitySet edmEntitySet, UriInfo uriInfo) throws ODataApplicationException {
        final EntityCollection eCollection = new EntityCollection();

        OiyokanEdmProvider provider = new OiyokanEdmProvider();
        if (!edmEntitySet.getEntityContainer().getName().equals(provider.getEntityContainer().getName())) {
            // Container 名が不一致. 処理せずに戻します.
            return eCollection;
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
            return eCollection;
        }

        //////////////////////////////////////////////
        // Oiyokan が対応しない処理を拒絶するための記述.
        if (uriInfo.getApplyOption() != null) {
            // [M011] NOT SUPPORTED: URI: $apply
            System.err.println(OiyokanMessages.M011);
            throw new ODataApplicationException(OiyokanMessages.M011, 500, Locale.ENGLISH);
        }
        if (uriInfo.getCustomQueryOptions() != null && uriInfo.getCustomQueryOptions().size() > 0) {
            // [M012] NOT SUPPORTED: URI: customQuery
            System.err.println(OiyokanMessages.M012);
            throw new ODataApplicationException(OiyokanMessages.M012, 500, Locale.ENGLISH);
        }
        if (uriInfo.getDeltaTokenOption() != null) {
            // [M013] NOT SUPPORTED: URI: deltaToken
            System.err.println(OiyokanMessages.M013);
            throw new ODataApplicationException(OiyokanMessages.M013, 500, Locale.ENGLISH);
        }
        if (uriInfo.getExpandOption() != null && uriInfo.getExpandOption().getExpandItems().size() > 0) {
            // [M014] NOT SUPPORTED: URI: $expand
            System.err.println(OiyokanMessages.M014);
            throw new ODataApplicationException(OiyokanMessages.M014, 500, Locale.ENGLISH);
        }

        // データベースに接続.
        try (Connection connTargetDb = BasicJdbcUtil.getConnection(entitySet.getSettingsDatabase())) {
            if (uriInfo.getSearchOption() != null) {
                // $search.
                new ExperimentalH2FullTextSearch().process(connTargetDb, edmEntitySet, uriInfo, eCollection);
                return eCollection;
            }

            // 件数カウントがONの場合はカウント処理を実行。
            if (uriInfo.getCountOption() != null && uriInfo.getCountOption().getValue()) {
                // $count.
                processCountQuery(entitySet, uriInfo, connTargetDb, eCollection);
            }

            // 実際のデータ取得処理を実行。
            processCollectionQuery(entitySet, uriInfo, connTargetDb, eCollection);

            return eCollection;
        } catch (SQLException ex) {
            // [M015] UNEXPECTED: Fail on database connection SQL
            System.err.println(OiyokanMessages.M015 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M015, 500, Locale.ENGLISH, ex);
        }
    }

    private static void processCountQuery(OiyokanCsdlEntitySet entitySet, UriInfo uriInfo, Connection connTargetDb,
            EntityCollection eCollection) throws ODataApplicationException {
        // 件数をカウントして設定。
        BasicSqlBuilder basicSqlBuilder = new BasicSqlBuilder(entitySet);
        basicSqlBuilder.getSelectCountQuery(uriInfo);
        final String sql = basicSqlBuilder.getSqlInfo().getSqlBuilder().toString();

        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: TRACE: SQL: " + sql);

        int countWithWhere = 0;
        try (var stmt = connTargetDb.prepareStatement(sql)) {
            int column = 1;
            for (Object look : basicSqlBuilder.getSqlInfo().getSqlParamList()) {
                BasicJdbcUtil.bindPreparedParameter(stmt, column++, look);
            }

            stmt.executeQuery();
            var rset = stmt.getResultSet();
            rset.next();
            countWithWhere = rset.getInt(1);
        } catch (SQLException ex) {
            // [M015] UNEXPECTED: Fail on database connection SQL
            System.err.println(OiyokanMessages.M015 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M015 + ": " + sql, 500, Locale.ENGLISH, ex);
        }
        // 取得できたレコード件数を設定.
        eCollection.setCount(countWithWhere);
    }

    private static void processCollectionQuery(OiyokanCsdlEntitySet entitySet, UriInfo uriInfo, Connection connTargetDb,
            EntityCollection eCollection) throws ODataApplicationException {
        BasicSqlBuilder basicSqlBuilder = new BasicSqlBuilder(entitySet);

        basicSqlBuilder.getSelectQuery(uriInfo, entitySet.getSettingsDatabase());
        final String sql = basicSqlBuilder.getSqlInfo().getSqlBuilder().toString();

        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: TRACE: SQL: " + sql);

        try (var stmt = connTargetDb.prepareStatement(sql)) {
            int idxColumn = 1;
            for (Object look : basicSqlBuilder.getSqlInfo().getSqlParamList()) {
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

                if (entitySet.getEntityType().getKey().size() == 0) {
                    // キーが存在しないのは OData としてはまずい。
                    // 別の箇所にて標準エラー出力にて報告。
                } else {
                    // キーが存在する場合は、IDとして設定。
                    OiyokanCsdlEntitySet iyoEntitySet = (OiyokanCsdlEntitySet) entitySet;
                    String keyValue = "";
                    for (CsdlPropertyRef look : iyoEntitySet.getEntityType().getKey()) {
                        if (keyValue.length() > 0) {
                            keyValue += "-";
                        }

                        String idVal = rset.getString(look.getName());
                        // 未整理の事項。キーの値をエスケープすべきかどうか.
                        // 現状、スペースとコロンはアンダースコアに置き換え.
                        idVal = idVal.replaceAll("[' '|':']", "_");
                        keyValue += idVal;
                    }
                    ent.setId(createId(entitySet.getName(), keyValue));
                }

                eCollection.getEntities().add(ent);
            }
        } catch (SQLException ex) {
            // [M017] Fail to execute SQL
            System.err.println(OiyokanMessages.M017 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M017 + ": " + sql, 500, Locale.ENGLISH, ex);
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
            return new URI(entitySetName + "(" + id + ")");
        } catch (URISyntaxException ex) {
            // [M018] UNEXPECTED: Fail to create ID EntitySet name
            System.err.println(OiyokanMessages.M018 + ": " + entitySetName + ": " + ex.toString());
            throw new ODataRuntimeException(OiyokanMessages.M018 + ": " + entitySetName, ex);
        }
    }
}
