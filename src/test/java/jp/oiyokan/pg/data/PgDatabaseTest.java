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
package jp.oiyokan.pg.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanSettingsUtil;
import jp.oiyokan.basic.BasicDbUtil;
import jp.oiyokan.dto.OiyokanSettings;
import jp.oiyokan.dto.OiyokanSettingsDatabase;

/**
 * そもそも内部 h2 database への接続性を確認
 */
class PgDatabaseTest {
    @Test
    void test01() throws Exception {
        final OiyokanSettings settingsOiyokan = OiyokanSettingsUtil.loadOiyokanSettings();
        OiyokanSettingsDatabase settingsDatabase = null;
        for (OiyokanSettingsDatabase look : settingsOiyokan.getDatabaseList()) {
            if ("postgres1".equals(look.getName())) {
                settingsDatabase = look;
            }
        }
        try (Connection connTargetDb = BasicDbUtil.getConnection(settingsDatabase)) {
            final String tableName = "actor";
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
                final List<CsdlPropertyRef> keyRefList = new ArrayList<>();
                final DatabaseMetaData dbmeta = connTargetDb.getMetaData();
                final ResultSet rsKey = dbmeta.getPrimaryKeys(null, null, tableName);
                for (; rsKey.next();) {
                    String pkName = rsKey.getString("PK_NAME");
                    String colName = rsKey.getString("COLUMN_NAME");
                }
            }

            sqlBuilder.append("  );\n");

            System.err.println("SQL: ");
            System.err.println(sqlBuilder.toString());
        }
    }
}
