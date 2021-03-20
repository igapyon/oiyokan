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
package jp.oiyokan.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Locale;

import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.basic.BasicDbUtil;
import jp.oiyokan.h2.data.OiyokanResourceSqlUtil;

/**
 * Oiyokan (OData v4 server) が動作する際に必要になる内部データおよびサンプルデータをセットアップ.
 */
public class OiyokanInterDb {
    private OiyokanInterDb() {
    }

    /**
     * 情報を格納するためのテーブルをセットアップします。
     * 
     * @param connInternalDb データベース接続。
     * @return true:新規作成, false:既に存在.
     */
    public static boolean setupTable(final Connection connInternalDb) throws ODataApplicationException {
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println( //
                    "OData v4: setup internal table: " + " (Oiyokan: " + OiyokanConstants.VERSION + ")");

        // Oiyokan が動作する上で必要なテーブルのセットアップ.
        try (var stmt = connInternalDb.prepareStatement("CREATE TABLE IF NOT EXISTS " //
                + "ODataAppInfos (" //
                + "KeyName VARCHAR(20) NOT NULL" //
                + ",KeyValue VARCHAR(255)" //
                + ",PRIMARY KEY(KeyName)" //
                + ")")) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new ODataApplicationException("テーブル作成に失敗: " + ex.toString(), 500, Locale.ENGLISH);
        }

        // ODataAppInfos が既に存在するかどうか確認. 存在する場合は処理中断.
        try (var stmt = connInternalDb.prepareStatement("SELECT COUNT(*) FROM ODataAppInfos")) {
            stmt.executeQuery();
            var rset = stmt.getResultSet();
            rset.next();
            if (rset.getInt(1) > 0) {
                // すでにテーブルがセットアップ済み。処理中断します。
                return false;
            }
        } catch (SQLException ex) {
            throw new ODataApplicationException("Fail to SQL: " + ex.toString(), 500, Locale.ENGLISH, ex);
        }

        ///////////////////////////////////////////
        // 内部データの作成に突入.
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println( //
                    "OData v4: setup internal data: " + " (Oiyokan: " + OiyokanConstants.VERSION + ")");

        ///////////////////////////////////////////
        // ODataAppInfos にバージョン情報などデータの追加
        try (var stmt = connInternalDb.prepareStatement("INSERT INTO ODataAppInfos (KeyName, KeyValue) VALUES ("
                + BasicDbUtil.getQueryPlaceholderString(2) + ")")) {
            stmt.setString(1, "Version");
            stmt.setString(2, OiyokanConstants.VERSION);
            stmt.executeUpdate();

            stmt.clearParameters();
            stmt.setString(1, "Provider");
            stmt.setString(2, OiyokanConstants.NAME);
            stmt.executeUpdate();

            connInternalDb.commit();
        } catch (SQLException ex) {
            throw new ODataApplicationException("テーブル作成に失敗: " + ex.toString(), 500, Locale.ENGLISH);
        }

        final String[] sqls = OiyokanResourceSqlUtil.loadOiyokanResourceSql("oiyokan-testdb.sql");
        for (String sql : sqls) {
            try (var stmt = connInternalDb.prepareStatement(sql.trim())) {
                // System.err.println("SQL: " + sql);
                stmt.executeUpdate();
                connInternalDb.commit();
            } catch (SQLException ex) {
                throw new ODataApplicationException("SQL実行に失敗: " + ex.toString(), 500, Locale.ENGLISH);
            }
        }

        // 新規作成.
        return true;
    }

    /**
     * Ocsdl 用の DDL 文字列を取得.
     * 
     * @param connTargetDb
     * @param tableName
     * @return
     * @throws SQLException
     */
    public static String generateCreateOcsdlDdl(Connection connTargetDb, String tableName) throws SQLException {
        final String sql = "SELECT * FROM " + tableName + " LIMIT 1";
        final StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("CREATE TABLE IF NOT EXISTS\n");
        sqlBuilder.append("  Ocsdl" + tableName + " (\n");
        try (PreparedStatement stmt = connTargetDb.prepareStatement(sql)) {
            ResultSetMetaData rsmeta = stmt.getMetaData();
            final int columnCount = rsmeta.getColumnCount();
            boolean isFirstColumn = true;
            for (int column = 1; column <= columnCount; column++) {
                sqlBuilder.append("    ");
                if (isFirstColumn) {
                    isFirstColumn = false;
                } else {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append(rsmeta.getColumnName(column) + " ");

                switch (rsmeta.getColumnType(column)) {
                case Types.TINYINT:
                    sqlBuilder.append("TINYINT");
                    break;
                case Types.SMALLINT:
                    sqlBuilder.append("SMALLINT");
                    break;
                case Types.INTEGER: /* INT */
                    sqlBuilder.append("INT");
                    break;
                case Types.BIGINT:
                    sqlBuilder.append("BIGINT");
                    break;
                case Types.DECIMAL:
                    sqlBuilder.append("DECIMAL(" //
                            + rsmeta.getScale(column) + "," + rsmeta.getPrecision(column) + ")");
                    break;
                case Types.BOOLEAN:
                    sqlBuilder.append("BOOLEAN");
                    break;
                case Types.REAL:
                    sqlBuilder.append("REAL");
                    break;
                case Types.DOUBLE:
                    sqlBuilder.append("DOUBLE");
                    break;
                case Types.DATE:
                    sqlBuilder.append("DATE");
                    break;
                case Types.TIMESTAMP:
                    sqlBuilder.append("TIMESTAMP");
                    break;
                case Types.TIME:
                    sqlBuilder.append("TIME");
                    break;
                case Types.CHAR:
                    sqlBuilder.append("CHAR(" + rsmeta.getColumnDisplaySize(column) + ")");
                    break;
                case Types.VARCHAR:
                    sqlBuilder.append("VARCHAR(" + rsmeta.getColumnDisplaySize(column) + ")");
                    break;
                default:
                    System.err.println("Type: ignore");
                    break;
                }
                sqlBuilder.append("\n");
            }

            // テーブルのキー情報
            final DatabaseMetaData dbmeta = connTargetDb.getMetaData();
            final ResultSet rsKey = dbmeta.getPrimaryKeys(null, null, tableName);
            boolean isFirstPkey = true;
            for (; rsKey.next();) {
                String colName = rsKey.getString("COLUMN_NAME");
                if (isFirstPkey) {
                    isFirstPkey = false;
                    sqlBuilder.append("    , PRIMARY KEY(");
                } else {
                    sqlBuilder.append(",");
                }
                sqlBuilder.append(colName);
            }
            if (isFirstPkey == false) {
                sqlBuilder.append(")\n");
            }
        }

        sqlBuilder.append("  );\n");

        return sqlBuilder.toString();
    }
}
