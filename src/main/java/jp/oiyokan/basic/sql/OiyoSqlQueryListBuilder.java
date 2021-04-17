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

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.core.uri.queryoption.FilterOptionImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.MemberImpl;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanCsdlEntitySet;
import jp.oiyokan.basic.OiyoBasicJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.common.OiyoSqlInfo;
import jp.oiyokan.dto.OiyoSettingsEntitySet;

/**
 * SQL文を構築するための簡易クラス.
 */
public class OiyoSqlQueryListBuilder {
    /**
     * Oiyokan Info.
     */
    private OiyoInfo oiyoInfo;

    private String entitySetName;

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

    public OiyoSqlQueryListBuilder(OiyoInfo oiyoInfo, String entitySetName, OiyokanCsdlEntitySet entitySet) {
        this.oiyoInfo = oiyoInfo;
        this.entitySetName = entitySetName;
        this.sqlInfo = new OiyoSqlInfo(oiyoInfo, entitySetName, entitySet);
    }

    /**
     * 件数カウント用のSQLを生成.
     * 
     * @param uriInfo URI情報.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public void buildSelectCountQuery(UriInfo uriInfo) throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, entitySetName);

        sqlInfo.getSqlBuilder().append("SELECT COUNT(*) FROM "
                + OiyoBasicJdbcUtil.escapeKakkoFieldName(sqlInfo, entitySet.getEntityType().getDbName()));
        if (uriInfo.getFilterOption() != null) {
            FilterOptionImpl filterOpt = (FilterOptionImpl) uriInfo.getFilterOption();
            sqlInfo.getSqlBuilder().append(" WHERE ");
            new OiyoSqlQueryListExpr(oiyoInfo, sqlInfo).expand(filterOpt.getExpression());
        }
    }

    /**
     * 検索用のSQLを生成.
     * 
     * @param uriInfo URI情報.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public void buildSelectQuery(UriInfo uriInfo) throws ODataApplicationException {
        sqlInfo.getSqlBuilder().append("SELECT ");

        expandSelect(uriInfo);

        expandFrom(uriInfo);

        // uriInfo.getCountOption は明示的には記載しない.
        // 現状の実装では指定があろうがなかろうが件数はカウントする実装となっている.
        // TODO FIXME 現状でも常にカウントしているかどうか確認すること。

        if (OiyokanConstants.DatabaseType.MSSQL2008 == sqlInfo.getEntitySet().getDatabaseType() //
                || OiyokanConstants.DatabaseType.ORACLE == sqlInfo.getEntitySet().getDatabaseType()) {
            sqlInfo.getSqlBuilder().append(" WHERE ");
            expandRowNumberBetween(uriInfo);
            // SQL Server検索は WHERE絞り込みは既にサブクエリにて適用済み.
        } else {
            if (uriInfo.getFilterOption() != null) {
                FilterOptionImpl filterOpt = (FilterOptionImpl) uriInfo.getFilterOption();
                // WHERE部分についてはパラメータクエリで処理するのを基本とする.
                sqlInfo.getSqlBuilder().append(" WHERE ");
                new OiyoSqlQueryListExpr(oiyoInfo, sqlInfo).expand(filterOpt.getExpression());
            }
        }

        if (OiyokanConstants.DatabaseType.MSSQL2008 == sqlInfo.getEntitySet().getDatabaseType() //
                || OiyokanConstants.DatabaseType.ORACLE == sqlInfo.getEntitySet().getDatabaseType()) {
            // 必ず rownum4between 順でソート.
            sqlInfo.getSqlBuilder().append(" ORDER BY rownum4between");
        } else {
            if (uriInfo.getOrderByOption() != null) {
                sqlInfo.getSqlBuilder().append(" ORDER BY ");
                expandOrderBy(uriInfo);
            } else {
                // 無指定の場合は primary key でソート.
                // これをしないとページング処理で困る.
                sqlInfo.getSqlBuilder().append(" ORDER BY ");
                expandOrderByWithPrimary();
            }
        }

        expandTopSkip(uriInfo);
    }

    private void expandRowNumberBetween(UriInfo uriInfo) throws ODataApplicationException {
        int start = -1;
        int count = -1;
        if (uriInfo.getTopOption() != null) {
            count = uriInfo.getTopOption().getValue();
        } else {
            // とても大きな数.
            count = 1000000;
        }
        if (uriInfo.getSkipOption() != null) {
            start = 1 + uriInfo.getSkipOption().getValue();
        } else {
            start = 1;
        }

        sqlInfo.getSqlBuilder().append("rownum4between BETWEEN " + start + " AND " + (start + count - 1));
    }

    private void expandSelect(UriInfo uriInfo) throws ODataApplicationException {
        if (uriInfo.getSelectOption() == null) {
            expandSelectWild(uriInfo);
        } else {
            expandSelectEach(uriInfo);
        }
    }

    private void expandSelectWild(UriInfo uriInfo) throws ODataApplicationException {
        // アスタリスクは利用せず、項目を指定する。
        OiyokanCsdlEntitySet entitySet = (OiyokanCsdlEntitySet) sqlInfo.getEntitySet();
        CsdlEntityType entityType = entitySet.getEntityType();
        String strColumns = "";
        for (CsdlProperty prop : entityType.getProperties()) {
            if (strColumns.length() > 0) {
                strColumns += ",";
            }

            // もし空白を含む場合はエスケープ。
            strColumns += OiyoBasicJdbcUtil.escapeKakkoFieldName(sqlInfo,
                    OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySet.getName(), prop.getName()).getDbName());
        }
        sqlInfo.getSqlBuilder().append(strColumns);
    }

    private void expandSelectEach(UriInfo uriInfo) throws ODataApplicationException {
        final OiyokanCsdlEntitySet iyoEntitySet = (OiyokanCsdlEntitySet) sqlInfo.getEntitySet();
        final List<String> keyTarget = new ArrayList<>();
        for (CsdlPropertyRef propRef : iyoEntitySet.getEntityType().getKey()) {
            keyTarget.add(OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, iyoEntitySet.getName(), propRef.getName())
                    .getDbName());
        }
        int itemCount = 0;
        for (SelectItem item : uriInfo.getSelectOption().getSelectItems()) {
            for (UriResource res : item.getResourcePath().getUriResourceParts()) {
                sqlInfo.getSqlBuilder().append(itemCount++ == 0 ? "" : ",");
                final String unescapedName = OiyoBasicJdbcUtil.unescapeKakkoFieldName(res.toString());
                sqlInfo.getSqlBuilder().append(OiyoBasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyoInfoUtil
                        .getOiyoEntityProperty(oiyoInfo, sqlInfo.getEntitySet().getName(), unescapedName).getDbName()));
                for (int index = 0; index < keyTarget.size(); index++) {
                    if (keyTarget.get(index).equals(res.toString())) {
                        keyTarget.remove(index);
                        break;
                    }
                }
            }
        }
        for (int index = 0; index < keyTarget.size(); index++) {
            // レコードを一意に表すID項目が必須。検索対象にない場合は追加.
            sqlInfo.getSqlBuilder().append(itemCount++ == 0 ? "" : ",");
            sqlInfo.getSqlBuilder().append(OiyoBasicJdbcUtil.unescapeKakkoFieldName(keyTarget.get(index)));
        }
    }

    private void expandFrom(UriInfo uriInfo) throws ODataApplicationException {
        // 取得元のテーブル.
        switch (sqlInfo.getEntitySet().getDatabaseType()) {
        default:
            sqlInfo.getSqlBuilder().append(" FROM " + OiyoBasicJdbcUtil.escapeKakkoFieldName(sqlInfo,
                    sqlInfo.getEntitySet().getDbTableNameTargetIyo()));
            break;
        case MSSQL2008:
        case ORACLE: {
            ///////////////////////////////////
            // SQL Server / ORACLE 用特殊記述
            // 現在、無条件にサブクエリ展開
            String topfilter = "";
            if (OiyokanConstants.DatabaseType.MSSQL2008 == sqlInfo.getEntitySet().getDatabaseType()) {
                if (uriInfo.getTopOption() != null) {
                    int total = uriInfo.getTopOption().getValue();
                    if (uriInfo.getSkipOption() != null) {
                        total += uriInfo.getSkipOption().getValue();
                    }
                    topfilter = "TOP " + (total + 1) + " ";
                }

            }

            sqlInfo.getSqlBuilder().append(" FROM (SELECT " + topfilter + "ROW_NUMBER()");
            if (uriInfo.getOrderByOption() != null) {
                sqlInfo.getSqlBuilder().append(" OVER (");
                sqlInfo.getSqlBuilder().append("ORDER BY ");
                expandOrderBy(uriInfo);
                sqlInfo.getSqlBuilder().append(") ");
            } else {
                sqlInfo.getSqlBuilder().append(" OVER (ORDER BY ");
                expandOrderByWithPrimary();
                sqlInfo.getSqlBuilder().append(") ");
            }

            sqlInfo.getSqlBuilder().append("AS rownum4between,");
            // 必要な分だけ項目展開.
            expandSelect(uriInfo);
            sqlInfo.getSqlBuilder().append(" FROM " + OiyoBasicJdbcUtil.escapeKakkoFieldName(sqlInfo,
                    sqlInfo.getEntitySet().getDbTableNameTargetIyo()));
            if (uriInfo.getFilterOption() != null) {
                sqlInfo.getSqlBuilder().append(" WHERE ");
                // データ絞り込みはここで実現.
                new OiyoSqlQueryListExpr(oiyoInfo, sqlInfo).expand(uriInfo.getFilterOption().getExpression());
            }
            sqlInfo.getSqlBuilder().append(")");
            if (OiyokanConstants.DatabaseType.MSSQL2008 == sqlInfo.getEntitySet().getDatabaseType()) {
                // 以下記述は SQL2008のみ。ORACLEではエラー。
                sqlInfo.getSqlBuilder().append(" AS rownum4subquery");
            }
            // SQL Server / ORACLE 用特殊記述
            ///////////////////////////////////
        }
            break;
        }
    }

    private void expandOrderBy(UriInfo uriInfo) throws ODataApplicationException {
        List<OrderByItem> orderByItemList = uriInfo.getOrderByOption().getOrders();
        for (int index = 0; index < orderByItemList.size(); index++) {
            OrderByItem orderByItem = orderByItemList.get(index);
            if (index == 0) {
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }

            final String unescapedName = OiyoBasicJdbcUtil
                    .unescapeKakkoFieldName(((MemberImpl) orderByItem.getExpression()).toString());
            sqlInfo.getSqlBuilder().append(OiyoBasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyoInfoUtil
                    .getOiyoEntityProperty(oiyoInfo, sqlInfo.getEntitySet().getName(), unescapedName).getDbName()));

            if (orderByItem.isDescending()) {
                sqlInfo.getSqlBuilder().append(" DESC");
            }
        }
    }

    private void expandOrderByWithPrimary() throws ODataApplicationException {
        // 無指定の場合はプライマリキーにてソート.
        boolean isFirst = true;
        for (CsdlPropertyRef look : sqlInfo.getEntitySet().getEntityType().getKey()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }
            final String unescapedName = OiyoBasicJdbcUtil.unescapeKakkoFieldName(look.getName());
            sqlInfo.getSqlBuilder().append(OiyoBasicJdbcUtil.escapeKakkoFieldName(sqlInfo, OiyoInfoUtil
                    .getOiyoEntityProperty(oiyoInfo, sqlInfo.getEntitySet().getName(), unescapedName).getDbName()));
        }
    }

    private void expandTopSkip(UriInfo uriInfo) {
        if (OiyokanConstants.DatabaseType.MSSQL2008 != sqlInfo.getEntitySet().getDatabaseType() //
                && OiyokanConstants.DatabaseType.ORACLE != sqlInfo.getEntitySet().getDatabaseType()) {
            if (uriInfo.getTopOption() != null) {
                sqlInfo.getSqlBuilder().append(" LIMIT ");
                sqlInfo.getSqlBuilder().append(uriInfo.getTopOption().getValue());
            }

            if (uriInfo.getSkipOption() != null) {
                sqlInfo.getSqlBuilder().append(" OFFSET ");
                sqlInfo.getSqlBuilder().append(uriInfo.getSkipOption().getValue());
            }
        }
    }
}
