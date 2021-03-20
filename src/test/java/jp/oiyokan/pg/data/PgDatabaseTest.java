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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import jp.oiyokan.data.OiyokanInterDb;
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
            final String sql = "SELECT * FROM " + "actor" + " LIMIT 1";
            try (PreparedStatement stmt = connTargetDb.prepareStatement(sql)) {
                ResultSetMetaData rsmeta = stmt.getMetaData();
                final int columnCount = rsmeta.getColumnCount();
                for (int column = 1; column <= columnCount; column++) {
                    System.err.println("Name: " + rsmeta.getColumnName(column));

                    switch (rsmeta.getColumnType(column)) {
                    case Types.TINYINT:
                        System.err.println("Type: TINYINT");
                        break;
                    case Types.SMALLINT:
                        System.err.println("Type: SMALLINT");
                        break;
                    case Types.INTEGER: /* INT */
                        System.err.println("Type: INTEGER");
                        break;
                    case Types.BIGINT:
                        System.err.println("Type: BIGINT");
                        break;
                    case Types.DECIMAL:
                        System.err.println("Type: DECIMAL");
                        System.err.println("  scale: " + rsmeta.getScale(column));
                        System.err.println("  precision: " + rsmeta.getPrecision(column));
                        break;
                    case Types.BOOLEAN:
                        System.err.println("Type: BOOLEAN");
                        break;
                    case Types.REAL:
                        System.err.println("Type: REAL");
                        break;
                    case Types.DOUBLE:
                        System.err.println("Type: DOUBLE");
                        break;
                    case Types.DATE:
                        System.err.println("Type: DATE");
                        break;
                    case Types.TIMESTAMP:
                        System.err.println("Type: TIMESTAMP");
                        break;
                    case Types.TIME:
                        System.err.println("Type: TIME");
                        break;
                    case Types.CHAR:
                        System.err.println("Type: CHAR");
                        System.err.println("    Size: " + rsmeta.getColumnDisplaySize(column));
                        break;
                    case Types.VARCHAR:
                        System.err.println("Type: VARCHAR");
                        System.err.println("    Size: " + rsmeta.getColumnDisplaySize(column));
                        break;
                    default:
                        System.err.println("Type: ignore");
                        break;
                    }
                }

                // テーブルのキー情報
                final List<CsdlPropertyRef> keyRefList = new ArrayList<>();
                final DatabaseMetaData dbmeta = connTargetDb.getMetaData();
                final ResultSet rsKey = dbmeta.getPrimaryKeys(null, null, "actor");
                for (; rsKey.next();) {
                    String pkName = rsKey.getString("PK_NAME");
                    String colName = rsKey.getString("COLUMN_NAME");
                }
            }
        }
    }

    @Test
    void testo2() throws Exception {
        // TODO このテストを、ODataRequestベースのものに書き換えた版を作成すること。

        final OiyokanSettings settingsOiyokan = OiyokanSettingsUtil.loadOiyokanSettings();
        try (Connection conn = BasicDbUtil
                .getConnection(OiyokanSettingsUtil.getOiyokanInternalDatabase(settingsOiyokan))) {
            // 内部データベースのテーブルをセットアップ.
            OiyokanInterDb.setupTable(conn);

            try (var stmt = conn.prepareStatement("SELECT ID, Name, Description" //
                    + ",Sbyte1,Int16a,Int32a,Int64a,Decimal1,StringChar2,StringVar255,StringVar65535,Boolean1,Single1,Double1,DateTimeOffset1,TimeOfDay1" //
                    + " FROM MyProducts ORDER BY ID LIMIT 1")) {
                stmt.executeQuery();
                var rset = stmt.getResultSet();
                assertEquals(true, rset.next());
                ResultSetMetaData rsmeta = rset.getMetaData();
                for (int column = 1; column <= rsmeta.getColumnCount(); column++) {
                    // System.err.println(rsmeta.getColumnName(column) + ", class=" +
                    // rsmeta.getColumnClassName(column));
                }
            }
        }
    }
}
