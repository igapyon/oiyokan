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
import java.sql.Timestamp;
import java.time.ZonedDateTime;
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

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanCsdlEntitySet;
import jp.oiyokan.OiyokanEdmProvider;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.basic.sql.BasicSqlInfo;
import jp.oiyokan.fromolingo.FromApacheOlingoUtil;
import jp.oiyokan.settings.OiyokanNamingUtil;

public class BasicJdbcEntityProcessor {
    private BasicSqlInfo sqlInfo;

    public Entity readEntityData(UriInfo uriInfo, EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates)
            throws ODataApplicationException {
        OiyokanEdmProvider provider = new OiyokanEdmProvider();
        if (!edmEntitySet.getEntityContainer().getName().equals(provider.getEntityContainer().getName())) {
            // Container 名が不一致. 処理せずに戻します.
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
                    System.err.println("TRACE: param: " + look.toString());
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

        sqlInfo.getSqlBuilder().append(" FROM " + sqlInfo.getEntitySet().getDbTableNameTargetIyo());

        sqlInfo.getSqlBuilder().append(" WHERE ");
        boolean isFirst = true;
        for (UriParameter param : keyPredicates) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append("AND ");
            }
            sqlInfo.getSqlBuilder().append(param.getName());
            sqlInfo.getSqlBuilder().append(" = ");

            // TODO FIXME この記述を EntityCollectionBuilderと共通化すること。
            CsdlProperty csdlProp = sqlInfo.getEntitySet().getEntityType().getProperty(param.getName());
            if ("Edm.SByte".equals(csdlProp.getType())) {
                Byte look = Byte.valueOf(param.getText());
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(look);
                continue;
            } else if ("Edm.Byte".equals(csdlProp.getType())) {
                Short look = Short.valueOf(param.getText());
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(look);
                continue;
            } else if ("Edm.Int16".equals(csdlProp.getType())) {
                sqlInfo.getSqlBuilder().append(param.getText());
                continue;
            } else if ("Edm.Int32".equals(csdlProp.getType())) {
                sqlInfo.getSqlBuilder().append(param.getText());
                continue;
            } else if ("Edm.Int64".equals(csdlProp.getType())) {
                sqlInfo.getSqlBuilder().append(param.getText());
                continue;
            } else if ("Edm.Decimal".equals(csdlProp.getType())) {
                sqlInfo.getSqlBuilder().append(param.getText());
                continue;
            } else if ("Edm.Boolean".equals(csdlProp.getType())) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(Boolean.valueOf("true".equalsIgnoreCase(param.getText())));
                continue;
            } else if ("Edm.Single".equals(csdlProp.getType())) {
                sqlInfo.getSqlBuilder().append(param.getText());
                continue;
            } else if ("Edm.Double".equals(csdlProp.getType())) {
                sqlInfo.getSqlBuilder().append(param.getText());
                continue;
            } else if ("Edm.Date".equals(csdlProp.getType())) {
                ZonedDateTime zdt = FromApacheOlingoUtil.parseDateString(param.getText());
                sqlInfo.getSqlBuilder().append("?");
                Timestamp tstamp = Timestamp.from(zdt.toInstant());
                sqlInfo.getSqlParamList().add(tstamp);
                continue;
            } else if ("Edm.DateTimeOffset".equals(csdlProp.getType())) {
                ZonedDateTime zdt = FromApacheOlingoUtil.parseZonedDateTime(param.getText());
                sqlInfo.getSqlBuilder().append("?");
                Timestamp tstamp = Timestamp.from(zdt.toInstant());
                sqlInfo.getSqlParamList().add(tstamp);
                continue;
            } else if ("Edm.TimeOfDay".equals(csdlProp.getType())) {
            } else if ("Edm.String".equals(csdlProp.getType())) {
                String value = param.getText();
                if (value.startsWith("'") && value.endsWith("'")) {
                    // 文字列リテラルについては前後のクオートを除去して記憶.
                    value = value.substring(1, value.length() - 1);
                }
                // 文字列リテラルとしてパラメータ化クエリで扱う.
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(value);
                continue;
            } else if ("Edm.Binary".equals(csdlProp.getType())) {
            } else if ("Edm.Guid".equals(csdlProp.getType())) {
            } else {
                sqlInfo.getSqlBuilder().append(param.getText());
                continue;
            }

            System.err.println(OiyokanMessages.M999);
            throw new ODataApplicationException(OiyokanMessages.M999, 500, Locale.ENGLISH);
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
}