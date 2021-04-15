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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.basic.OiyoBasicJdbcUtil;
import jp.oiyokan.dto.Oiyo13SettingsDatabase;
import jp.oiyokan.dto.Oiyo13SettingsEntitySet;
import jp.oiyokan.dto.Oiyo13SettingsEntityType;
import jp.oiyokan.dto.Oiyo13SettingsProperty;
import jp.oiyokan.settings.OiyoSettingsUtil;

/**
 * Oiyokan (OData v4 server) が動作する際に必要になる内部管理データベースのバージョン情報および Oiyo情報 をセットアップ.
 */
public class OiyokanKanDatabase {
    /**
     * Oiyokan の設定情報を記述したファイル.
     */
    private static final String[][] OIYOKAN_FILE_SQLS = new String[][] { //
            /*
             * Oiyokan の基本機能を確認およびビルド時の JUnitテストで利用. 変更するとビルドが動作しなくなる場合あり.
             */
            { OiyokanConstants.OIYOKAN_KAN_DB, "oiyokan-test-oiyo.sql" },

            /*
             * Sakila dvdrental サンプルDB に接続するための Oiyo 情報.
             */
            { OiyokanConstants.OIYOKAN_KAN_DB, "sample-sakila-oiyo.sql" },

            /*
             * Oiyokan のターゲットデータベースの Oiyo情報を記述。github上では空白ファイルとする.
             */
            { OiyokanConstants.OIYOKAN_KAN_DB, "oiyokan-oiyo.sql" }, };

    private OiyokanKanDatabase() {
    }

    /**
     * 内部データベースの情報一式をセットアップします。
     * 
     * @return true:新規作成, false:既に存在.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static synchronized boolean setupKanDatabase() throws ODataApplicationException {
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println( //
                    "OData v4: setup oiyokanKan database (Oiyokan: " + OiyokanConstants.VERSION + ")");

        Oiyo13SettingsDatabase settingsInterDatabase = OiyoSettingsUtil
                .getOiyokanDatabase(OiyokanConstants.OIYOKAN_KAN_DB);

        try (Connection connInterDb = OiyoBasicJdbcUtil.getConnection(settingsInterDatabase)) {
            // Internal Database の バージョン情報および Oiyokanテーブルを setup.

            // Oiyokan が動作する上で必要なテーブルのセットアップ.
            try (var stmt = connInterDb.prepareStatement("CREATE TABLE IF NOT EXISTS " //
                    + "Oiyokan (" //
                    + "KeyName VARCHAR(20) NOT NULL" //
                    + ",KeyValue VARCHAR(255)" //
                    + ",PRIMARY KEY(KeyName)" //
                    + ")")) {
                stmt.executeUpdate();
            } catch (SQLException ex) {
                // [M027] UNEXPECTED: Fail to create local table: Oiyokan
                System.err.println(OiyokanMessages.M027 + ": " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.M027, 500, Locale.ENGLISH);
            }

            // ODataAppInfos が既に存在するかどうか確認. 存在する場合は処理中断.
            try (var stmt = connInterDb.prepareStatement("SELECT COUNT(*) FROM Oiyokan")) {
                stmt.executeQuery();
                var rset = stmt.getResultSet();
                rset.next();
                if (rset.getInt(1) > 0) {
                    // すでにテーブルがセットアップ済み。処理中断します。
                    return false;
                }
            } catch (SQLException ex) {
                // [M028] UNEXPECTED: Fail to check local table exists: Oiyokan
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
            try (var stmt = connInterDb.prepareStatement("INSERT INTO Oiyokan (KeyName, KeyValue) VALUES ("
                    + OiyoBasicJdbcUtil.getQueryPlaceholderString(2) + ")")) {
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

                Oiyo13SettingsDatabase lookDatabase = OiyoSettingsUtil.getOiyokanDatabase(sqlFileDef[0]);

                try (Connection connLoookDatabase = OiyoBasicJdbcUtil.getConnection(lookDatabase)) {
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
     * Oiyo 用の DDL 文字列を取得.
     * 
     * 注意: このメソッドは内部的に全件検索します。内部用 DDL生成の場合以外このメソッドは呼ばないこと。
     * 
     * @param connTargetDb ターゲットDBへのDB接続.
     * @param tableName    テーブル名.
     * @return 作表のためのDDL.
     * @throws SQLException SQL例外が発生した場合.
     */
    public static String generateCreateOiyoDdl(Connection connTargetDb, String tableName) throws SQLException {
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
        sqlBuilder.append("  Oiyo_" + tableName + " (\n");
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

    public static Oiyo13SettingsEntitySet generateCreateOiyoJson(Connection connTargetDb, String tableName)
            throws SQLException {
        final Oiyo13SettingsEntitySet entitySet = new Oiyo13SettingsEntitySet();
        entitySet.setEntityType(new Oiyo13SettingsEntityType());

        final Map<String, String> defaultValueMap = new HashMap<>();
        {
            final ResultSet rsdbmetacolumns = connTargetDb.getMetaData().getColumns(null, null, tableName, "%");
            for (; rsdbmetacolumns.next();) {
                String colName = rsdbmetacolumns.getString("COLUMN_NAME");
                String defValue = rsdbmetacolumns.getString("COLUMN_DEF");
                defaultValueMap.put(colName, defValue);
            }
        }

        entitySet.setName(tableName + "s");
        entitySet.setDbSettingName("localdb");
        entitySet.setDescription("Desc");
        entitySet.setCanCreate(true);
        entitySet.setCanRead(true);
        entitySet.setCanUpdate(true);
        entitySet.setCanDelete(true);
        entitySet.setDescription("Desc");
        entitySet.getEntityType().setName(tableName);
        entitySet.getEntityType().setProperty(new ArrayList<Oiyo13SettingsProperty>());
        entitySet.getEntityType().setKeyName(new ArrayList<String>());

        // TODO FIXME テーブル名エスケープが暫定対処。
        try (PreparedStatement stmt = connTargetDb.prepareStatement("SELECT * FROM " + "[" + tableName + "]")) {
            ResultSetMetaData rsmeta = stmt.getMetaData();
            final int columnCount = rsmeta.getColumnCount();
            for (int column = 1; column <= columnCount; column++) {
                final Oiyo13SettingsProperty property = new Oiyo13SettingsProperty();
                entitySet.getEntityType().getProperty().add(property);

                String columnName = rsmeta.getColumnName(column);
                property.setDbType(columnName);
                property.setDbType(rsmeta.getColumnTypeName(column));

                switch (rsmeta.getColumnType(column)) {
                case Types.TINYINT:
                    property.setJdbcType("Types.TINYINT");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.SByte.getFullQualifiedName().getFullQualifiedNameAsString());
                    break;
                case Types.SMALLINT:
                    property.setJdbcType("Types.SMALLINT");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.Int16.getFullQualifiedName().getFullQualifiedNameAsString());
                    break;
                case Types.INTEGER: /* INT */
                    property.setJdbcType("Types.INTEGER");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.Int32.getFullQualifiedName().getFullQualifiedNameAsString());
                    break;
                case Types.BIGINT:
                    property.setJdbcType("Types.BIGINT");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.Int64.getFullQualifiedName().getFullQualifiedNameAsString());
                    break;
                case Types.DECIMAL:
                    property.setJdbcType("Types.DECIMAL");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.Decimal.getFullQualifiedName().getFullQualifiedNameAsString());
                    if (rsmeta.getPrecision(column) > 0) {
                        property.setPrecision(rsmeta.getPrecision(column));
                        property.setScale(rsmeta.getScale(column));
                    }
                    break;
                case Types.NUMERIC:
                    // postgres で発生.
                    property.setJdbcType("Types.NUMERIC");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.Decimal.getFullQualifiedName().getFullQualifiedNameAsString());
                    if (rsmeta.getPrecision(column) > 0) {
                        property.setPrecision(rsmeta.getPrecision(column));
                        property.setScale(rsmeta.getScale(column));
                    }
                    break;
                case Types.BOOLEAN:
                    property.setJdbcType("Types.BOOLEAN");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.Boolean.getFullQualifiedName().getFullQualifiedNameAsString());
                    break;
                case Types.BIT:
                    // postgres で発生.
                    property.setJdbcType("Types.BIT");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.Boolean.getFullQualifiedName().getFullQualifiedNameAsString());
                    break;
                case Types.REAL:
                    property.setJdbcType("Types.REAL");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.Single.getFullQualifiedName().getFullQualifiedNameAsString());
                    break;
                case Types.DOUBLE:
                    property.setJdbcType("Types.DOUBLE");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.Double.getFullQualifiedName().getFullQualifiedNameAsString());
                    break;
                case Types.DATE:
                    property.setJdbcType("Types.DATE");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.Date.getFullQualifiedName().getFullQualifiedNameAsString());
                    break;
                case Types.TIMESTAMP:
                    property.setJdbcType("Types.TIMESTAMP");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName().getFullQualifiedNameAsString());
                    break;
                case Types.TIME:
                    property.setJdbcType("Types.TIME");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.TimeOfDay.getFullQualifiedName().getFullQualifiedNameAsString());
                    break;
                case Types.CHAR:
                    property.setJdbcType("Types.CHAR");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString());
                    property.setLengthFixed(true);
                    property.setLength(rsmeta.getColumnDisplaySize(column));
                    break;
                case Types.VARCHAR:
                    property.setJdbcType("Types.VARCHAR");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString());
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        property.setLength(rsmeta.getColumnDisplaySize(column));
                    }
                    break;
                case Types.LONGVARCHAR:
                    property.setJdbcType("Types.LONGVARCHAR");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString());
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        property.setLength(rsmeta.getColumnDisplaySize(column));
                    }
                    break;
                case Types.LONGNVARCHAR:
                    property.setJdbcType("Types.LONGNVARCHAR");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString());
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        property.setLength(rsmeta.getColumnDisplaySize(column));
                    }
                    break;
                case Types.CLOB:
                    property.setJdbcType("Types.CLOB");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString());
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        property.setLength(rsmeta.getColumnDisplaySize(column));
                    }
                    break;
                case Types.BINARY:
                    property.setJdbcType("Types.BINARY");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.Binary.getFullQualifiedName().getFullQualifiedNameAsString());
                    if ("UUID".equalsIgnoreCase(rsmeta.getColumnTypeName(column))) {
                        // 型名が UUID の時だけ特殊な挙動をする.
                        property.setEdmType(
                                EdmPrimitiveTypeKind.Guid.getFullQualifiedName().getFullQualifiedNameAsString());
                    } else {
                        if (rsmeta.getColumnDisplaySize(column) > 0
                                && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                            property.setLength(rsmeta.getColumnDisplaySize(column));
                        }
                    }
                    break;
                case Types.VARBINARY:
                    property.setJdbcType("Types.VARBINARY");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.Binary.getFullQualifiedName().getFullQualifiedNameAsString());
                    if ("UUID".equalsIgnoreCase(rsmeta.getColumnTypeName(column))) {
                        // 型名が UUID の時だけ特殊な挙動をする.
                        property.setEdmType(
                                EdmPrimitiveTypeKind.Guid.getFullQualifiedName().getFullQualifiedNameAsString());
                    } else {
                        if (rsmeta.getColumnDisplaySize(column) > 0
                                && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                            property.setLength(rsmeta.getColumnDisplaySize(column));
                        }
                    }
                    break;
                case Types.LONGVARBINARY:
                    property.setJdbcType("Types.LONGVARBINARY");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.Binary.getFullQualifiedName().getFullQualifiedNameAsString());
                    if ("UUID".equalsIgnoreCase(rsmeta.getColumnTypeName(column))) {
                        // 型名が UUID の時だけ特殊な挙動をする.
                        property.setEdmType(
                                EdmPrimitiveTypeKind.Guid.getFullQualifiedName().getFullQualifiedNameAsString());
                    } else {
                        if (rsmeta.getColumnDisplaySize(column) > 0
                                && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                            property.setLength(rsmeta.getColumnDisplaySize(column));
                        }
                    }
                    break;
                case Types.BLOB:
                    property.setJdbcType("Types.BLOB");
                    property.setEdmType(
                            EdmPrimitiveTypeKind.Binary.getFullQualifiedName().getFullQualifiedNameAsString());
                    if ("UUID".equalsIgnoreCase(rsmeta.getColumnTypeName(column))) {
                        // 型名が UUID の時だけ特殊な挙動をする.
                        property.setEdmType(
                                EdmPrimitiveTypeKind.Guid.getFullQualifiedName().getFullQualifiedNameAsString());
                    } else {
                        if (rsmeta.getColumnDisplaySize(column) > 0
                                && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                            property.setLength(rsmeta.getColumnDisplaySize(column));
                        }
                    }
                    break;
                case Types.ARRAY:
                    // postgres で発生. 対応しない.
                    property.setJdbcType("Types.ARRAY");
                    property.setEdmType("NOT_SUPPORT_ARRAY");
                    break;
                case Types.OTHER:
                    // postgres で発生. 対応しない.
                    property.setJdbcType("Types.ARRAY");
                    property.setEdmType("NOT_SUPPORT_OTHER");
                    break;
                default:
                    // [M021] NOT SUPPORTED: JDBC Type
                    System.err.println(OiyokanMessages.M021 + ": " + rsmeta.getColumnType(column));
                    new ODataApplicationException(OiyokanMessages.M021 + ": " + rsmeta.getColumnType(column), //
                            500, Locale.ENGLISH);
                    break;
                }

                // if (defaultValueMap.get(columnName) != null) {
                // sqlBuilder.append(" DEFAULT " + defaultValueMap.get(columnName));
                // }

                if (ResultSetMetaData.columnNoNulls == rsmeta.isNullable(column)) {
                    property.setNullable(false);
                }
            }

            // テーブルのキー情報
            final DatabaseMetaData dbmeta = connTargetDb.getMetaData();
            final ResultSet rsKey = dbmeta.getPrimaryKeys(null, null, tableName);
            for (; rsKey.next();) {
                String colName = rsKey.getString("COLUMN_NAME");
                entitySet.getEntityType().getKeyName().add(colName);
            }
        }

        return entitySet;
    }
}
