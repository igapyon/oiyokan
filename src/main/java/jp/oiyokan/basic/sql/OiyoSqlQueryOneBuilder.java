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
package jp.oiyokan.basic.sql;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.common.OiyoSqlInfo;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsProperty;

/**
 * データベースから1件レコードを検索.
 */
public class OiyoSqlQueryOneBuilder {
    private static final Log log = LogFactory.getLog(OiyoSqlQueryOneBuilder.class);

    /**
     * Oiyokan Info.
     */
    private OiyoInfo oiyoInfo;

    /**
     * SQL構築のデータ構造.
     */
    private OiyoSqlInfo sqlInfo;

    /**
     * SQL構築のデータ構造を取得.
     * 
     * @return SQL構築のデータ構造.
     */
    public OiyoSqlInfo getSqlInfo() {
        return sqlInfo;
    }

    public OiyoSqlQueryOneBuilder(OiyoInfo oiyoInfo, OiyoSqlInfo sqlInfo) {
        this.oiyoInfo = oiyoInfo;
        this.sqlInfo = sqlInfo;
    }

    /**
     * 1件の検索用のSQLを生成.
     * 
     * @param entitySetName instance of EdmEntitySet.
     * @param keyPredicates Keys to select.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public void buildSelectOneQuery(String entitySetName, List<UriParameter> keyPredicates)
            throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, entitySetName);

        final OiyokanConstants.DatabaseType databaseType = OiyoInfoUtil
                .getOiyoDatabaseTypeByEntitySetName(sqlInfo.getOiyoInfo(), sqlInfo.getEntitySetName());

        sqlInfo.getSqlBuilder().append("SELECT ");

        expandSelectKey(entitySetName);

        sqlInfo.getSqlBuilder().append(
                " FROM " + OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo, entitySet.getEntityType().getDbName()));

        sqlInfo.getSqlBuilder().append(" WHERE ");
        boolean isFirst = true;
        for (UriParameter param : keyPredicates) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(" AND ");
            }
            if ("ROWID".equalsIgnoreCase(param.getName()) //
                    && OiyokanConstants.DatabaseType.ORACLE == databaseType) {
                // ORACLE ROWID special.
                sqlInfo.getSqlBuilder().append(param.getName());
            } else {
                sqlInfo.getSqlBuilder().append(OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyoInfoUtil
                        .getOiyoEntityProperty(oiyoInfo, entitySet.getName(), param.getName()).getDbName()));
            }
            sqlInfo.getSqlBuilder().append("=");

            try {
                final OiyoSettingsProperty prop = OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySetName,
                        param.getName());
                OiyoCommonJdbcUtil.expandLiteralOrBindParameter(sqlInfo, prop.getEdmType(), param.getText());
            } catch (ODataApplicationException ex) {
                // ORACLEのROWIDを利用する場合、この処理で例外.
                // 例外の場合は Edm.String 決め打ちで処理する。
                OiyoCommonJdbcUtil.expandLiteralOrBindParameter(sqlInfo, "Edm.String", param.getText());
            }
        }
    }

    private void expandSelectKey(String entitySetName) throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, entitySetName);

        int itemCount = 0;
        for (OiyoSettingsProperty prop : entitySet.getEntityType().getProperty()) {
            sqlInfo.getSqlBuilder().append(itemCount++ == 0 ? "" : ",");
            sqlInfo.getColumnNameList().add(prop.getName());
            sqlInfo.getSqlBuilder().append(OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo,
                    OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySetName, prop.getName()).getDbName()));
        }
    }
}
