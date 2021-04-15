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

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;

import jp.oiyokan.basic.OiyoBasicJdbcUtil;
import jp.oiyokan.settings.OiyoNamingUtil;

/**
 * データベースに1件レコードを追加.
 */
public class OiyoSqlInsertOneBuilder {
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

    public OiyoSqlInsertOneBuilder(OiyoSqlInfo sqlInfo) {
        this.sqlInfo = sqlInfo;
    }

    /**
     * Create DML for INSERT.
     * 
     * @param edmEntitySet  instance of EdmEntitySet.
     * @param keyPredicates (PATCH) key to insert.
     * @param requestEntity entity to insert.
     * @throws ODataApplicationException OData App exception occured.
     */
    public void buildInsertIntoDml(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates, Entity requestEntity)
            throws ODataApplicationException {
        sqlInfo.getSqlBuilder().append("INSERT INTO ");
        sqlInfo.getSqlBuilder().append(
                OiyoBasicJdbcUtil.escapeKakkoFieldName(sqlInfo, sqlInfo.getEntitySet().getDbTableNameTargetIyo()));
        sqlInfo.getSqlBuilder().append(" (");
        boolean isFirst = true;

        // PATCH(INSERT)
        if (keyPredicates != null) {
            for (UriParameter param : keyPredicates) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sqlInfo.getSqlBuilder().append(",");
                }
                sqlInfo.getSqlBuilder().append(
                        OiyoBasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyoNamingUtil.entity2Db(param.getName())));
            }
        }

        for (Property prop : requestEntity.getProperties()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }

            final String colName = OiyoBasicJdbcUtil.escapeKakkoFieldName(sqlInfo,
                    OiyoNamingUtil.entity2Db(prop.getName()));
            sqlInfo.getSqlBuilder().append(colName);
        }

        sqlInfo.getSqlBuilder().append(") VALUES (");
        isFirst = true;

        // PATCH(INSERT)
        if (keyPredicates != null) {
            for (UriParameter param : keyPredicates) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sqlInfo.getSqlBuilder().append(",");
                }

                CsdlProperty csdlProp = sqlInfo.getEntitySet().getEntityType().getProperty(param.getName());
                OiyoBasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, csdlProp.getType(), param.getText());
            }
        }

        for (Property prop : requestEntity.getProperties()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }
            OiyoBasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, prop.getType(), prop.getValue());
        }

        sqlInfo.getSqlBuilder().append(")");
    }
}
