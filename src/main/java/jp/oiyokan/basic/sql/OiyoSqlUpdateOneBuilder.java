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
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;

import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.common.OiyoSqlInfo;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsProperty;

/**
 * データベースの1件レコードを更新.
 */
public class OiyoSqlUpdateOneBuilder {
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

    public OiyoSqlUpdateOneBuilder(OiyoInfo oiyoInfo, OiyoSqlInfo sqlInfo) {
        this.oiyoInfo = oiyoInfo;
        this.sqlInfo = sqlInfo;
    }

    /**
     * Create DML for UPDATE (PATCH).
     * 
     * @param edmEntitySet  instance of EdmEntitySet.
     * @param keyPredicates keys to update.
     * @param requestEntity entity to delete.
     * @throws ODataApplicationException OData App exception occured.
     */
    public void buildUpdatePatchDml(String entitySetName, List<UriParameter> keyPredicates, Entity requestEntity)
            throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, entitySetName);

        sqlInfo.getSqlBuilder().append("UPDATE ");
        sqlInfo.getSqlBuilder()
                .append(OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo, entitySet.getEntityType().getDbName()));
        sqlInfo.getSqlBuilder().append(" SET ");
        boolean isFirst = true;
        for (Property prop : requestEntity.getProperties()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }

            sqlInfo.getSqlBuilder().append(OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo,
                    OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySet.getName(), prop.getName()).getDbName()));
            sqlInfo.getSqlBuilder().append("=");

            OiyoCommonJdbcUtil.expandLiteralOrBindParameter(sqlInfo, prop.getType(), prop.getValue());
        }

        sqlInfo.getSqlBuilder().append(" WHERE ");

        isFirst = true;
        for (UriParameter param : keyPredicates) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(" AND ");
            }
            sqlInfo.getSqlBuilder().append(OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo,
                    OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySet.getName(), param.getName()).getDbName()));
            sqlInfo.getSqlBuilder().append("=");

            final OiyoSettingsProperty prop = OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySetName,
                    param.getName());
            OiyoCommonJdbcUtil.expandLiteralOrBindParameter(sqlInfo, prop.getEdmType(), param.getText());
        }
    }
}
