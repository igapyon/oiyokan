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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.basic.BasicJdbcUtil;
import jp.oiyokan.dto.OiyokanSettingsDatabase;
import jp.oiyokan.settings.OiyokanSettingsUtil;

/**
 * Oiyokan (OData v4 server) が動作する際に必要になる内部データベースのバージョン情報および Ocsdl info をセットアップ.
 */
public class OiyokanInternalDatabase {
    /**
     * Oiyokan の設定情報を記述したファイル.
     */
    private static final String[][] OIYOKAN_FILE_SQLS = new String[][] { //
            /*
             * Oiyokan の基本機能を確認およびビルド時の JUnitテストで利用. 変更するとビルドが動作しなくなる場合あり. この内容は
             * BuildInternalDbTest.java により別途生成.
             */
            // { OiyokanConstants.OIYOKAN_INTERNAL_TARGET_DB, "oiyokan-test-db.sql" }, //

            /*
             * Oiyokan の基本機能を確認およびビルド時の JUnitテストで利用. 変更するとビルドが動作しなくなる場合あり.
             */
            { OiyokanConstants.OIYOKAN_INTERNAL_DB, "oiyokan-test-ocsdl.sql" },

            /*
             * Sakila dvdrental サンプルDB の内容そのもの. この内容は BuildInternalDbTest.java により別途生成.
             */
            // { OiyokanConstants.OIYOKAN_INTERNAL_TARGET_DB, "sample-sakila-db.sql" }, //

            /*
             * Sakila dvdrental サンプルDB に接続するためのOCSDL情報.
             */
            { OiyokanConstants.OIYOKAN_INTERNAL_DB, "sample-sakila-ocsdl.sql" },
            /*
             * Oiyokan のターゲットデータベースのOCSDL情報を記述。github上では空白ファイルとする.
             */
            { OiyokanConstants.OIYOKAN_INTERNAL_DB, "oiyokan-ocsdl.sql" }, };

    private OiyokanInternalDatabase() {
    }

    /**
     * 内部データベースの情報一式をセットアップします。
     * 
     * @return true:新規作成, false:既に存在.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static synchronized boolean setupInternalDatabase() throws ODataApplicationException {
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println( //
                    "OData v4: setup internal database (Oiyokan: " + OiyokanConstants.VERSION + ")");

        OiyokanSettingsDatabase settingsInterDatabase = OiyokanSettingsUtil
                .getOiyokanDatabase(OiyokanConstants.OIYOKAN_INTERNAL_DB);

        try (Connection connInterDb = BasicJdbcUtil.getConnection(settingsInterDatabase)) {
            // Internal Database の バージョン情報および Ocsdl テーブルを setup.

            // Oiyokan が動作する上で必要なテーブルのセットアップ.
            try (var stmt = connInterDb.prepareStatement("CREATE TABLE IF NOT EXISTS " //
                    + "ODataAppInfos (" //
                    + "KeyName VARCHAR(20) NOT NULL" //
                    + ",KeyValue VARCHAR(255)" //
                    + ",PRIMARY KEY(KeyName)" //
                    + ")")) {
                stmt.executeUpdate();
            } catch (SQLException ex) {
                // [M027] UNEXPECTED: Fail to create local table: ODataAppInfos
                System.err.println(OiyokanMessages.M027 + ": " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.M027, 500, Locale.ENGLISH);
            }

            // ODataAppInfos が既に存在するかどうか確認. 存在する場合は処理中断.
            try (var stmt = connInterDb.prepareStatement("SELECT COUNT(*) FROM ODataAppInfos")) {
                stmt.executeQuery();
                var rset = stmt.getResultSet();
                rset.next();
                if (rset.getInt(1) > 0) {
                    // すでにテーブルがセットアップ済み。処理中断します。
                    return false;
                }
            } catch (SQLException ex) {
                // [M028] UNEXPECTED: Fail to check local table exists: ODataAppInfos
                System.err.println(OiyokanMessages.M028 + ": " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.M028, 500, Locale.ENGLISH);
            }

            ///////////////////////////////////////////
            // 内部データの作成に突入.
            if (OiyokanConstants.IS_TRACE_ODATA_V4)
                System.err.println( //
                        "OData v4: setup internal data " + " (Oiyokan: " + OiyokanConstants.VERSION + ")");

            ///////////////////////////////////////////
            // ODataAppInfos にバージョン情報などデータの追加
            try (var stmt = connInterDb.prepareStatement("INSERT INTO ODataAppInfos (KeyName, KeyValue) VALUES ("
                    + BasicJdbcUtil.getQueryPlaceholderString(2) + ")")) {
                stmt.setString(1, "Version");
                stmt.setString(2, OiyokanConstants.VERSION);
                stmt.executeUpdate();

                stmt.clearParameters();
                stmt.setString(1, "Provider");
                stmt.setString(2, OiyokanConstants.NAME);
                stmt.executeUpdate();

                connInterDb.commit();
            } catch (SQLException ex) {
                // [M029] UNEXPECTED: Fail to execute SQL for local internal table
                System.err.println(OiyokanMessages.M029 + ": " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.M029, 500, Locale.ENGLISH);
            }

            for (String[] sqlFileDef : OIYOKAN_FILE_SQLS) {
                if (OiyokanConstants.IS_TRACE_ODATA_V4)
                    System.err.println("OData v4: load: db:" + sqlFileDef[0] + ", sql: " + sqlFileDef[1]);

                OiyokanSettingsDatabase lookDatabase = OiyokanSettingsUtil.getOiyokanDatabase(sqlFileDef[0]);

                try (Connection connLoookDatabase = BasicJdbcUtil.getConnection(lookDatabase)) {
                    final String[] sqls = OiyokanResourceSqlUtil.loadOiyokanResourceSql("oiyokan/sql/" + sqlFileDef[1]);
                    for (String sql : sqls) {
                        try (var stmt = connLoookDatabase.prepareStatement(sql.trim())) {
                            // System.err.println("SQL: " + sql);
                            stmt.executeUpdate();
                            connLoookDatabase.commit();
                        } catch (SQLException ex) {
                            // [M030] UNEXPECTED: Fail to execute SQL for local internal table(2)
                            System.err.println(OiyokanMessages.M030 + ": " + ex.toString());
                            throw new ODataApplicationException(OiyokanMessages.M030, 500, Locale.ENGLISH);
                        }
                    }
                } catch (SQLException ex) {
                    // [M031] UNEXPECTED: Fail to execute Dabaase
                    System.err.println(OiyokanMessages.M031 + ": " + ex.toString());
                    throw new ODataApplicationException(OiyokanMessages.M031, 500, Locale.ENGLISH);
                }
            }

            // 新規作成.
            return true;
        } catch (SQLException ex) {
            // [M004] UNEXPECTED: Database error in setup internal database.
            System.err.println(OiyokanMessages.M004 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M004, 500, Locale.ENGLISH);
        }
    }

    /**
     * Ocsdl 用の DDL 文字列を取得.
     * 
     * 注意: このメソッドは内部的に全件検索します。内部用 DDL生成の場合以外このメソッドは呼ばないこと。
     * 
     * @param connTargetDb ターゲットDBへのDB接続.
     * @param tableName    テーブル名.
     * @return 作表のためのDDL.
     * @throws SQLException SQL例外が発生した場合.
     */
    public static String generateCreateOcsdlDdl(Connection connTargetDb, String tableName) throws SQLException {
        final Map<String, String> defaultValueMap = new HashMap<>();
        {
            final ResultSet rsdbmetacolumns = connTargetDb.getMetaData().getColumns(null, null, tableName, "%");
            for (; rsdbmetacolumns.next();) {
                String colName = rsdbmetacolumns.getString("COLUMN_NAME");
                String defValue = rsdbmetacolumns.getString("COLUMN_DEF");
                defaultValueMap.put(colName, defValue);
            }
        }

        final String sql = "SELECT * FROM " + tableName;
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

                String columnName = rsmeta.getColumnName(column);
                if (columnName.indexOf(' ') > 0) {
                    // 内部DB向け設定であるので、接続先DBの種類によらず h2 database 前提のエスケープを実施。
                    columnName = "[" + columnName + "]";
                }
                sqlBuilder.append(columnName + " ");

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
                    if (rsmeta.getPrecision(column) > 0) {
                        sqlBuilder.append("DECIMAL(" //
                                + rsmeta.getPrecision(column) + "," + rsmeta.getScale(column) + ")");
                    } else {
                        sqlBuilder.append("DECIMAL");
                    }
                    break;
                case Types.NUMERIC:
                    // postgres で発生.
                    if (rsmeta.getPrecision(column) > 0) {
                        sqlBuilder.append("NUMERIC(" //
                                + rsmeta.getPrecision(column) + "," + rsmeta.getScale(column) + ")");
                    } else {
                        sqlBuilder.append("NUMERIC");
                    }
                    break;
                case Types.BOOLEAN:
                    sqlBuilder.append("BOOLEAN");
                    break;
                case Types.BIT:
                    // postgres で発生.
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
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        sqlBuilder.append("VARCHAR(" + rsmeta.getColumnDisplaySize(column) + ")");
                    } else {
                        sqlBuilder.append("VARCHAR");
                    }
                    break;
                case Types.LONGVARCHAR:
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        sqlBuilder.append("LONGVARCHAR(" + rsmeta.getColumnDisplaySize(column) + ")");
                    } else {
                        sqlBuilder.append("LONGVARCHAR");
                    }
                    break;
                case Types.LONGNVARCHAR:
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        sqlBuilder.append("LONGVARCHAR(" + rsmeta.getColumnDisplaySize(column) + ")");
                    } else {
                        sqlBuilder.append("LONGVARCHAR");
                    }
                    break;
                case Types.CLOB:
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        sqlBuilder.append("CLOB(" + rsmeta.getColumnDisplaySize(column) + ")");
                    } else {
                        sqlBuilder.append("CLOB");
                    }
                    break;
                case Types.BINARY:
                    if ("UUID".equalsIgnoreCase(rsmeta.getColumnTypeName(column))) {
                        // 型名が UUID の時だけ特殊な挙動をする.
                        sqlBuilder.append("UUID");
                    } else {
                        if (rsmeta.getColumnDisplaySize(column) > 0
                                && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                            sqlBuilder.append("BINARY(" + rsmeta.getColumnDisplaySize(column) + ")");
                        } else {
                            sqlBuilder.append("BINARY");
                        }
                    }
                    break;
                case Types.VARBINARY:
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        sqlBuilder.append("VARBINARY(" + rsmeta.getColumnDisplaySize(column) + ")");
                    } else {
                        sqlBuilder.append("VARBINARY");
                    }
                    break;
                case Types.LONGVARBINARY:
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        sqlBuilder.append("LONGVARBINARY(" + rsmeta.getColumnDisplaySize(column) + ")");
                    } else {
                        sqlBuilder.append("LONGVARBINARY");
                    }
                    break;
                case Types.BLOB:
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        sqlBuilder.append("BLOB(" + rsmeta.getColumnDisplaySize(column) + ")");
                    } else {
                        sqlBuilder.append("BLOB");
                    }
                    break;
                case Types.ARRAY:
                    // postgres で発生. 対応しない.
                    sqlBuilder.append("NOT_SUPPORT_ARRAY");
                    break;
                case Types.OTHER:
                    // postgres で発生. 対応しない.
                    sqlBuilder.append("NOT_SUPPORT_OTHER");
                    break;
                default:
                    // [M021] NOT SUPPORTED: JDBC Type
                    System.err.println(OiyokanMessages.M021 + ": " + rsmeta.getColumnType(column));
                    new ODataApplicationException(OiyokanMessages.M021 + ": " + rsmeta.getColumnType(column), //
                            500, Locale.ENGLISH);
                    break;
                }

                if (defaultValueMap.get(columnName) != null) {
                    sqlBuilder.append(" DEFAULT " + defaultValueMap.get(columnName));
                }

                if (ResultSetMetaData.columnNoNulls == rsmeta.isNullable(column)) {
                    sqlBuilder.append(" NOT NULL");
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
