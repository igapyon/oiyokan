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

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.basic.OiyoBasicJdbcUtil;
import jp.oiyokan.settings.OiyokanNamingUtil;

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
     * @param requestEntity entity to delete.
     * @throws ODataApplicationException OData App exception occured.
     */
    public void buildInsertIntoDml(EdmEntitySet edmEntitySet, Entity requestEntity) throws ODataApplicationException {
        sqlInfo.getSqlBuilder().append("INSERT INTO ");
        sqlInfo.getSqlBuilder().append(
                OiyoBasicJdbcUtil.escapeKakkoFieldName(sqlInfo, sqlInfo.getEntitySet().getDbTableNameTargetIyo()));
        sqlInfo.getSqlBuilder().append(" (");
        boolean isFirst = true;
        for (Property prop : requestEntity.getProperties()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }

            final String colName = OiyoBasicJdbcUtil.escapeKakkoFieldName(sqlInfo,
                    OiyokanNamingUtil.entity2Db(prop.getName()));
            sqlInfo.getSqlBuilder().append(colName);
        }

        sqlInfo.getSqlBuilder().append(") VALUES (");
        isFirst = true;
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