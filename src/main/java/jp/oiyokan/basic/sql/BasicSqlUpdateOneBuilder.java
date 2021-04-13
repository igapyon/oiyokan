package jp.oiyokan.basic.sql;

import java.util.List;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;

import jp.oiyokan.basic.BasicJdbcUtil;
import jp.oiyokan.settings.OiyokanNamingUtil;

public class BasicSqlUpdateOneBuilder {
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

    public BasicSqlUpdateOneBuilder(BasicSqlInfo sqlInfo) {
        this.sqlInfo = sqlInfo;
    }

    public void getUpdatePatchDml(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates, Entity requestEntity)
            throws ODataApplicationException {
        sqlInfo.getSqlBuilder().append("UPDATE ");
        sqlInfo.getSqlBuilder()
                .append(BasicJdbcUtil.escapeKakkoFieldName(sqlInfo, sqlInfo.getEntitySet().getDbTableNameTargetIyo()));
        sqlInfo.getSqlBuilder().append(" SET ");
        boolean isFirst = true;
        for (Property prop : requestEntity.getProperties()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }

            sqlInfo.getSqlBuilder()
                    .append(BasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyokanNamingUtil.entity2Db(prop.getName())));
            sqlInfo.getSqlBuilder().append("=");

            BasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, prop.getType(), prop.getValue());
        }

        sqlInfo.getSqlBuilder().append(" WHERE ");

        isFirst = true;
        for (UriParameter param : keyPredicates) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(" AND ");
            }
            sqlInfo.getSqlBuilder()
                    .append(BasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyokanNamingUtil.entity2Db(param.getName())));
            sqlInfo.getSqlBuilder().append("=");

            CsdlProperty csdlProp = sqlInfo.getEntitySet().getEntityType().getProperty(param.getName());
            BasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, csdlProp.getType(), param.getText());
        }
    }

    public void getUpdatePutDml(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates, Entity requestEntity)
            throws ODataApplicationException {
        sqlInfo.getSqlBuilder().append("UPDATE ");
        sqlInfo.getSqlBuilder()
                .append(BasicJdbcUtil.escapeKakkoFieldName(sqlInfo, sqlInfo.getEntitySet().getDbTableNameTargetIyo()));
        sqlInfo.getSqlBuilder().append(" SET ");

        // primary key 以外の全てが対象。指定のないものは null。
        final List<CsdlPropertyRef> keys = sqlInfo.getEntitySet().getEntityType().getKey();
        boolean isFirst = true;
        CSDL_LOOP: for (CsdlProperty csdlProp : sqlInfo.getEntitySet().getEntityType().getProperties()) {
            // KEY以外が対象。
            for (CsdlPropertyRef key : keys) {
                if (key.getName().equals(csdlProp.getName())) {
                    // これはキー項目です。処理対象外.
                    continue CSDL_LOOP;
                }
            }

            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }

            sqlInfo.getSqlBuilder().append(
                    BasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyokanNamingUtil.entity2Db(csdlProp.getName())));

            sqlInfo.getSqlBuilder().append("=");
            Property prop = requestEntity.getProperty(csdlProp.getName());
            if (prop != null) {
                BasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, csdlProp.getType(), prop.getValue());
            } else {
                // 指定のないものには nullをセット.
                BasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, csdlProp.getType(), null);
            }
        }

        sqlInfo.getSqlBuilder().append(" WHERE ");

        isFirst = true;
        for (UriParameter param : keyPredicates) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(" AND ");
            }
            sqlInfo.getSqlBuilder()
                    .append(BasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyokanNamingUtil.entity2Db(param.getName())));
            sqlInfo.getSqlBuilder().append("=");

            CsdlProperty csdlProp = sqlInfo.getEntitySet().getEntityType().getProperty(param.getName());
            BasicJdbcUtil.expandLiteralOrBindParameter(sqlInfo, csdlProp.getType(), param.getText());
        }
    }
}
