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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.core.uri.queryoption.FilterOptionImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.MemberImpl;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.common.OiyoSqlInfo;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsProperty;

/**
 * SQL文を構築するための簡易クラス.
 */
public class OiyoSqlQueryListBuilder {
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(OiyoSqlQueryListBuilder.class);

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

    public OiyoSqlQueryListBuilder(OiyoInfo oiyoInfo, String entitySetName) {
        this.oiyoInfo = oiyoInfo;
        this.entitySetName = entitySetName;
        this.sqlInfo = new OiyoSqlInfo(oiyoInfo, entitySetName);
    }

    /**
     * 件数カウント用のSQLを生成.
     * 
     * @param uriInfo URI情報.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public void buildSelectCountQuery(UriInfo uriInfo) throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, entitySetName);

        // SELECTの検索項目名を追加。ここでは Property としては存在しない COUNT というダミーの名称をセット
        sqlInfo.getSelectColumnNameList().add("COUNT");
        sqlInfo.getSqlBuilder().append("SELECT COUNT(*) FROM "
                + OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo, entitySet.getEntityType().getDbName()));
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

        // 初回呼び出しとして SELECT を展開
        expandSelect(uriInfo, false);

        expandFrom(uriInfo);

        // uriInfo.getCountOption は明示的には記載しない.
        // 現状の実装では指定があろうがなかろうが件数はカウントする実装となっている.
        // TODO FIXME 現状でも常にカウントしているかどうか確認すること。

        final OiyokanConstants.DatabaseType databaseType = OiyoInfoUtil
                .getOiyoDatabaseTypeByEntitySetName(sqlInfo.getOiyoInfo(), sqlInfo.getEntitySetName());

        boolean isMssql2008OrOracleSpecial = false;
        if (OiyokanConstants.DatabaseType.MSSQL2008 == databaseType //
                && uriInfo.getSkipOption() == null) {
            // SQL Server 2008 で SKIP指定なしの場合はサブクエリの挙動を抑止してTOP記述のみ.
            // このため、WHERE 部分の展開は h2 などのDBと同じ挙動になる。
            isMssql2008OrOracleSpecial = false;
        } else if (OiyokanConstants.DatabaseType.MSSQL2008 == databaseType //
                || OiyokanConstants.DatabaseType.ORACLE == databaseType) {
            isMssql2008OrOracleSpecial = true;
            // SQL Server / ORACLE 検索は WHERE絞り込みは既にサブクエリにて適用済み.
        } else {
            isMssql2008OrOracleSpecial = false;
        }

        if (isMssql2008OrOracleSpecial) {
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

        if (isMssql2008OrOracleSpecial) {
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

    private void expandSelect(UriInfo uriInfo, boolean isSecondPass) throws ODataApplicationException {
        final OiyokanConstants.DatabaseType databaseType = OiyoInfoUtil
                .getOiyoDatabaseTypeByEntitySetName(sqlInfo.getOiyoInfo(), sqlInfo.getEntitySetName());
        if (OiyokanConstants.DatabaseType.MSSQL2008 == databaseType //
                && uriInfo.getTopOption() != null && uriInfo.getSkipOption() == null) {
            // SQL Server 2008 で SKIP指定なしの場合はサブクエリの挙動を抑止してTOP記述のみ.
            // 一方 TOP については、SQL 2008固有文法であるこの場所への TOP 展開。
            sqlInfo.getSqlBuilder().append("TOP " + uriInfo.getTopOption().getValue() + " ");
        }

        if (uriInfo.getSelectOption() == null) {
            expandSelectWild(uriInfo, isSecondPass);
        } else {
            expandSelectEach(uriInfo, isSecondPass);
        }
    }

    private void expandSelectWild(UriInfo uriInfo, boolean isSecondPass) throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, sqlInfo.getEntitySetName());

        // アスタリスクは利用せず、項目を指定する。
        String strColumns = "";
        for (OiyoSettingsProperty prop : entitySet.getEntityType().getProperty()) {
            if (strColumns.length() > 0) {
                strColumns += ",";
            }

            if (!isSecondPass) {
                // 初回呼び出し時にのみ、SELECTの検索項目名を追加。
                sqlInfo.getSelectColumnNameList().add(prop.getName());
            }
            // 項目名について空白が含まれている場合はカッコなどでエスケープ。
            strColumns += OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo, prop.getDbName());
        }
        sqlInfo.getSqlBuilder().append(strColumns);
    }

    private void expandSelectEach(UriInfo uriInfo, boolean isSecondPass) throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, sqlInfo.getEntitySetName());

        final List<String> keyTarget = new ArrayList<>();
        for (String keyName : entitySet.getEntityType().getKeyName()) {
            keyTarget.add(OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySet.getName(), keyName).getName());
        }
        int itemCount = 0;
        for (SelectItem item : uriInfo.getSelectOption().getSelectItems()) {
            for (UriResource res : item.getResourcePath().getUriResourceParts()) {
                sqlInfo.getSqlBuilder().append(itemCount++ == 0 ? "" : ",");

                final String unescapedName = OiyoCommonJdbcUtil.unescapeKakkoFieldName(res.toString());
                OiyoSettingsProperty prop = OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySet.getName(),
                        unescapedName);

                if (!isSecondPass) {
                    // 初回呼び出し時にのみ、SELECTの検索項目名を追加。
                    sqlInfo.getSelectColumnNameList().add(prop.getName());
                }
                sqlInfo.getSqlBuilder().append(OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo, prop.getDbName()));
                for (int index = 0; index < keyTarget.size(); index++) {
                    if (keyTarget.get(index).equals(prop.getName())) {
                        // 検索項目がキーであれば、すでに検索済みキーとしてリストから除去。
                        // これは、キー項目は選択された検索項目であろうがなかろうが検索する必要があるための一連の処理。
                        keyTarget.remove(index);
                        break;
                    }
                }
            }
        }
        for (int index = 0; index < keyTarget.size(); index++) {
            // レコードを一意に表すID項目が必須。検索対象にない場合は追加.
            sqlInfo.getSqlBuilder().append(itemCount++ == 0 ? "" : ",");
            final String unescapedName = OiyoCommonJdbcUtil.unescapeKakkoFieldName(keyTarget.get(index));
            // SELECTの検索項目名を追加。
            sqlInfo.getSelectColumnNameList().add(unescapedName);
            sqlInfo.getSqlBuilder().append(unescapedName);
        }
    }

    private void expandFrom(UriInfo uriInfo) throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, sqlInfo.getEntitySetName());

        final OiyokanConstants.DatabaseType databaseType = OiyoInfoUtil
                .getOiyoDatabaseTypeByEntitySetName(sqlInfo.getOiyoInfo(), sqlInfo.getEntitySetName());

        // 取得元のテーブル.
        switch (databaseType) {
        default:
            sqlInfo.getSqlBuilder().append(
                    " FROM " + OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo, entitySet.getEntityType().getDbName()));
            break;
        case MSSQL2008:
        case ORACLE: {
            if (OiyokanConstants.DatabaseType.MSSQL2008 == databaseType //
                    // SQL Server 2008 で SKIP指定なしの場合はサブクエリの挙動を抑止してTOP記述のみ.
                    // このため、WHERE 部分の展開は h2 などのDBと同じ挙動になる。
                    && uriInfo.getSkipOption() == null) {
                sqlInfo.getSqlBuilder().append(" FROM "
                        + OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo, entitySet.getEntityType().getDbName()));
                break;
            }

            ///////////////////////////////////
            // SQL Server / ORACLE 用特殊記述
            // 現在、無条件にサブクエリ展開
            String topfilterForSql2000 = "";
            if (OiyokanConstants.DatabaseType.MSSQL2008 == databaseType) {
                // SQL Server の場合 SKIP 指定がある場合のコースとなる。
                int total = 1;
                if (uriInfo.getTopOption() != null) {
                    total += uriInfo.getTopOption().getValue();
                }
                total += uriInfo.getSkipOption().getValue();
                topfilterForSql2000 = "TOP " + total + " ";
            }

            sqlInfo.getSqlBuilder().append(" FROM (SELECT " + topfilterForSql2000 + "ROW_NUMBER()");
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
            // 2回目の呼び出しとして SELECT を展開
            expandSelect(uriInfo, true);
            sqlInfo.getSqlBuilder().append(
                    " FROM " + OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo, entitySet.getEntityType().getDbName()));
            if (uriInfo.getFilterOption() != null) {
                sqlInfo.getSqlBuilder().append(" WHERE ");
                // データ絞り込みはここで実現.
                new OiyoSqlQueryListExpr(oiyoInfo, sqlInfo).expand(uriInfo.getFilterOption().getExpression());
            }
            sqlInfo.getSqlBuilder().append(")");
            if (OiyokanConstants.DatabaseType.MSSQL2008 == databaseType) {
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
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, sqlInfo.getEntitySetName());

        List<OrderByItem> orderByItemList = uriInfo.getOrderByOption().getOrders();
        for (int index = 0; index < orderByItemList.size(); index++) {
            OrderByItem orderByItem = orderByItemList.get(index);
            if (index == 0) {
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }

            final String unescapedName = OiyoCommonJdbcUtil
                    .unescapeKakkoFieldName(((MemberImpl) orderByItem.getExpression()).toString());
            sqlInfo.getSqlBuilder().append(OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo,
                    OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySet.getName(), unescapedName).getDbName()));

            if (orderByItem.isDescending()) {
                sqlInfo.getSqlBuilder().append(" DESC");
            }
        }
    }

    private void expandOrderByWithPrimary() throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, sqlInfo.getEntitySetName());

        // 無指定の場合はプライマリキーにてソート.
        boolean isFirst = true;
        for (String look : entitySet.getEntityType().getKeyName()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sqlInfo.getSqlBuilder().append(",");
            }

            final OiyoSettingsProperty prop = OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySet.getName(), look);

            sqlInfo.getSqlBuilder().append(OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo, prop.getDbName()));
        }
    }

    private void expandTopSkip(UriInfo uriInfo) throws ODataApplicationException {
        final OiyokanConstants.DatabaseType databaseType = OiyoInfoUtil
                .getOiyoDatabaseTypeByEntitySetName(sqlInfo.getOiyoInfo(), sqlInfo.getEntitySetName());

        if (OiyokanConstants.DatabaseType.MSSQL2008 != databaseType //
                && OiyokanConstants.DatabaseType.ORACLE != databaseType) {
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
