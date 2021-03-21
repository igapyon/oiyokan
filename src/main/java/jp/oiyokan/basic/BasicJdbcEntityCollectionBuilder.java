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
import jp.oiyokan.basic.sql.BasicSqlBuilder;
import jp.oiyokan.h2.data.TinyH2TrialFullTextSearch;

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
     */
    public static EntityCollection build(EdmEntitySet edmEntitySet, UriInfo uriInfo) throws ODataApplicationException {
        final EntityCollection eCollection = new EntityCollection();

        OiyokanEdmProvider provider = new OiyokanEdmProvider();
        if (!edmEntitySet.getEntityContainer().getName().equals(provider.getEntityContainer().getName())) {
            // Container 名が不一致. 処理せずに戻します.
            return eCollection;
        }

        OiyokanCsdlEntitySet eSetTarget = null;
        String targetEntityName = null;
        for (CsdlEntitySet eSetProvided : provider.getEntityContainer().getEntitySets()) {
            if (edmEntitySet.getName().equals(eSetProvided.getName())) {
                eSetTarget = (OiyokanCsdlEntitySet) eSetProvided;
                targetEntityName = eSetProvided.getName();
                break;
            }
        }

        if (targetEntityName == null) {
            // 処理対象外の要素セットです. 処理せずに戻します.
            return eCollection;
        }

        if (uriInfo.getApplyOption() != null) {
            System.err.println("NOT SUPPORTED: URI: $apply");
            throw new ODataApplicationException("NOT SUPPORTED: URI: $apply", 500, Locale.ENGLISH);
        }
        if (uriInfo.getCustomQueryOptions() != null && uriInfo.getCustomQueryOptions().size() > 0) {
            System.err.println("NOT SUPPORTED: URI: customQuery");
            throw new ODataApplicationException("NOT SUPPORTED: URI: customQuery", 500, Locale.ENGLISH);
        }
        if (uriInfo.getDeltaTokenOption() != null) {
            System.err.println("NOT SUPPORTED: URI: deltaToken");
            throw new ODataApplicationException("NOT SUPPORTED: URI: deltaToken", 500, Locale.ENGLISH);
        }
        if (uriInfo.getExpandOption() != null && uriInfo.getExpandOption().getExpandItems().size() > 0) {
            System.err.println("NOT SUPPORTED: URI: $expand");
            throw new ODataApplicationException("NOT SUPPORTED: URI: $expand", 500, Locale.ENGLISH);
        }

        // インメモリ作業データベースに接続.
        try (Connection connTargetDb = BasicDbUtil.getConnection(eSetTarget.getSettingsDatabase())) {
            if (uriInfo.getSearchOption() != null) {
                // $search.
                new TinyH2TrialFullTextSearch().process(connTargetDb, edmEntitySet, uriInfo, eCollection);
                return eCollection;
            }

            {
                // 件数をカウントして設定。
                BasicSqlBuilder tinySql = new BasicSqlBuilder();
                tinySql.getSqlInfo().setEntitySet((OiyokanCsdlEntitySet) eSetTarget);
                tinySql.getSelectCountQuery(uriInfo);
                final String sql = tinySql.getSqlInfo().getSqlBuilder().toString();

                if (OiyokanConstants.IS_TRACE_ODATA_V4)
                    System.err.println("OData v4: TRACE: SQL: " + sql);

                int countWithWhere = 0;
                try (var stmt = connTargetDb.prepareStatement(sql)) {
                    int column = 1;
                    for (Object look : tinySql.getSqlInfo().getSqlParamList()) {
                        BasicDbUtil.bindPreparedParameter(stmt, column++, look);
                    }

                    stmt.executeQuery();
                    var rset = stmt.getResultSet();
                    rset.next();
                    countWithWhere = rset.getInt(1);
                } catch (SQLException ex) {
                    System.err.println("Fail to execute count SQL: " + sql + ", " + ex.toString());
                    throw new ODataApplicationException("Fail to execute count SQL: " + sql, 500, Locale.ENGLISH, ex);
                }
                eCollection.setCount(countWithWhere);
            }

            BasicSqlBuilder tinySql = new BasicSqlBuilder();
            tinySql.getSqlInfo().setEntitySet((OiyokanCsdlEntitySet) eSetTarget);

            tinySql.getSelectQuery(uriInfo, eSetTarget.getSettingsDatabase());
            final String sql = tinySql.getSqlInfo().getSqlBuilder().toString();

            if (OiyokanConstants.IS_TRACE_ODATA_V4)
                System.err.println("OData v4: TRACE: SQL: " + sql);

            try (var stmt = connTargetDb.prepareStatement(sql)) {
                int idxColumn = 1;
                for (Object look : tinySql.getSqlInfo().getSqlParamList()) {
                    BasicDbUtil.bindPreparedParameter(stmt, idxColumn++, look);
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
                        Property prop = BasicDbUtil.resultSet2Property(rset, rsmeta, column, eSetTarget);
                        ent.addProperty(prop);
                    }

                    // キーが存在する場合は、IDとして設定。
                    if (eSetTarget.getEntityType().getKey().size() > 0) {
                        OiyokanCsdlEntitySet iyoEntitySet = (OiyokanCsdlEntitySet) eSetTarget;
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
                        ent.setId(createId(eSetTarget.getName(), keyValue));
                    }

                    eCollection.getEntities().add(ent);
                }
            } catch (SQLException ex) {
                System.err.println("Fail to execute SQL: " + sql + ", " + ex.toString());
                throw new ODataApplicationException("Fail to execute SQL: " + sql, 500, Locale.ENGLISH, ex);
            }

            return eCollection;
        } catch (SQLException ex) {
            System.err.println("Fail on database connection SQL: " + ex.toString());
            throw new ODataApplicationException("UNEXPECTED: Fail on database connection ", 500, Locale.ENGLISH, ex);
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
            System.err.println("UNEXPECTED: Fail to create ID EntitySet name: " + entitySetName + ": " + ex.toString());
            throw new ODataRuntimeException("UNEXPECTED: Fail to create ID EntitySet name: " + entitySetName, ex);
        }
    }
}
