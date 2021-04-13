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

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;

import jp.oiyokan.basic.BasicJdbcUtil;
import jp.oiyokan.settings.OiyokanNamingUtil;

/**
 * データベースから1件レコードを削除.
 */
public class BasicSqlDeleteOneBuilder {
    /**
     * SQL構築のデータ構造.
     */
    private BasicSqlInfo sqlInfo;

    /**
     * SQL構築のデータ構造を取得.
     * 
     * @return SQL構築のデータ構造.
     */
    public BasicSqlInfo getSqlInfo() {
        return sqlInfo;
    }

    public BasicSqlDeleteOneBuilder(BasicSqlInfo sqlInfo) {
        this.sqlInfo = sqlInfo;
    }

    public void buildDeleteDml(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates)
            throws ODataApplicationException {
        sqlInfo.getSqlBuilder().append("DELETE FROM ");
        sqlInfo.getSqlBuilder()
                .append(BasicJdbcUtil.escapeKakkoFieldName(sqlInfo, sqlInfo.getEntitySet().getDbTableNameTargetIyo()));
        sqlInfo.getSqlBuilder().append(" WHERE ");
        boolean isFirst = true;

        for (UriParameter param : keyPredicates) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(" AND ");
            }

            CsdlProperty csdlProp = sqlInfo.getEntitySet().getEntityType().getProperty(param.getName());

            sqlInfo.getSqlBuilder().append(
                    BasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyokanNamingUtil.entity2Db(csdlProp.getName())));
            sqlInfo.getSqlBuilder().append("=");
            BasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, csdlProp.getType(), param.getText());
        }
    }
}
