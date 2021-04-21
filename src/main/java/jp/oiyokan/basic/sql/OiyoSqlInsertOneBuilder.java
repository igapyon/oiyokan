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
 * データベースに1件レコードを追加.
 */
public class OiyoSqlInsertOneBuilder {
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(OiyoSqlInsertOneBuilder.class);

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

    public OiyoSqlInsertOneBuilder(OiyoInfo oiyoInfo, OiyoSqlInfo sqlInfo) {
        this.oiyoInfo = oiyoInfo;
        this.sqlInfo = sqlInfo;
    }

    /**
     * Create DML for INSERT.
     * 
     * @param entitySetName instance of EdmEntitySet.
     * @param keyPredicates (PATCH) key to insert.
     * @param requestEntity entity to insert.
     * @throws ODataApplicationException OData App exception occured.
     */
    public void buildInsertIntoDml(String entitySetName, List<UriParameter> keyPredicates, Entity requestEntity)
            throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, entitySetName);

        sqlInfo.getSqlBuilder().append("INSERT INTO ");
        sqlInfo.getSqlBuilder()
                .append(OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo, entitySet.getEntityType().getDbName()));
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
                sqlInfo.getSqlBuilder().append(OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo,
                        OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySetName, param.getName()).getDbName()));
            }
        }

        for (Property prop : requestEntity.getProperties()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }

            final String colName = OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo,
                    OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySetName, prop.getName()).getDbName());
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

                final OiyoSettingsProperty prop = OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySetName,
                        param.getName());
                OiyoCommonJdbcUtil.expandLiteralOrBindParameter(sqlInfo, prop.getEdmType(), prop, param.getText());
            }
        }

        for (Property prop : requestEntity.getProperties()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }

            final OiyoSettingsProperty oiyoProp = OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySetName,
                    prop.getName());
            OiyoCommonJdbcUtil.expandLiteralOrBindParameter(sqlInfo, prop.getType(), oiyoProp, prop.getValue());
        }

        sqlInfo.getSqlBuilder().append(")");
    }
}
