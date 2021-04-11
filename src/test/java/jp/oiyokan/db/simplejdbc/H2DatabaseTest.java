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
package jp.oiyokan.db.simplejdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanTestConstants;
import jp.oiyokan.basic.BasicJdbcUtil;
import jp.oiyokan.data.OiyokanKanDatabase;
import jp.oiyokan.settings.OiyokanSettingsUtil;

/**
 * ごく基本的で大雑把な JDBC + h2 database 挙動の確認.
 */
class H2DatabaseTest {
    @Test
    void test01() throws Exception {
        if (!OiyokanTestConstants.IS_TEST_ODATATEST)
            return;

        try (Connection conn = BasicJdbcUtil
                .getConnection(OiyokanSettingsUtil.getOiyokanDatabase(OiyokanConstants.OIYOKAN_INTERNAL_TARGET_DB))) {

            try (var stmt = conn.prepareStatement("SELECT ID, Name, Description FROM ODataTest1 ORDER BY ID LIMIT 3")) {
                stmt.executeQuery();
                var rset = stmt.getResultSet();
                assertEquals(true, rset.next());
            }
        }
    }

    @Test
    void testo2() throws Exception {
        if (!OiyokanTestConstants.IS_TEST_ODATATEST)
            return;

        try (Connection conn = BasicJdbcUtil
                .getConnection(OiyokanSettingsUtil.getOiyokanDatabase(OiyokanConstants.OIYOKAN_INTERNAL_TARGET_DB))) {

            try (var stmt = conn.prepareStatement("SELECT ID, Name, Description" //
                    + ",Sbyte1,Int16a,Int32a,Int64a,Decimal1,StringChar8,StringVar255,Boolean1,Single1,Double1,DateTimeOffset1,TimeOfDay1" //
                    + " FROM ODataTest1 ORDER BY ID LIMIT 1")) {
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

    /**
     * h2 database の null に関する挙動の確認.
     * 
     * @throws Exception
     */
    @Test
    void test03() throws Exception {
        if (!OiyokanTestConstants.IS_TEST_ODATATEST)
            return;

        try (Connection conn = BasicJdbcUtil
                .getConnection(OiyokanSettingsUtil.getOiyokanDatabase(OiyokanConstants.OIYOKAN_INTERNAL_DB))) {
            // 内部データベースのテーブルをセットアップ.
            OiyokanKanDatabase.setupKanDatabase();
        }

        try (Connection conn = BasicJdbcUtil
                .getConnection(OiyokanSettingsUtil.getOiyokanDatabase(OiyokanConstants.OIYOKAN_INTERNAL_TARGET_DB))) {

            try (var stmt = conn.prepareStatement(
                    "SELECT address_id FROM address WHERE ((address2 IS NULL) AND (address = ?)) LIMIT 2001")) {
                int column = 1;
                stmt.setString(column++, "47 MySakila Drive");
                stmt.executeQuery();
                var rset = stmt.getResultSet();
                if (rset.next()) {
                    assertEquals("1", rset.getString(1));
                }
            }
        }
    }

    /**
     * 対象データベースのテーブル一覧をゲット.
     */
    // @Test
    void testListTables() throws Exception {
        if (!OiyokanTestConstants.IS_TEST_ODATATEST)
            return;

        try (Connection conn = BasicJdbcUtil.getConnection(OiyokanSettingsUtil.getOiyokanDatabase("mysql1"))) {

            ResultSet rset = conn.getMetaData().getTables(null, "%", "%", new String[] { "TABLE", "VIEW" });
            for (; rset.next();) {
                System.err.println(
                        "tablist: " + rset.getString("TABLE_NAME") + " (" + rset.getString("TABLE_TYPE") + ")");
            }
        }
    }
}
