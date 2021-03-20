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
package jp.oiyokan.h2.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.ResultSetMetaData;

import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanSettingsUtil;
import jp.oiyokan.basic.BasicDbUtil;
import jp.oiyokan.dto.OiyokanSettings;

/**
 * そもそも内部 h2 database への接続性を確認
 */
class H2DatabaseTest {
    @Test
    void test01() throws Exception {
        final OiyokanSettings settingsOiyokan = OiyokanSettingsUtil.loadOiyokanSettings();
        try (Connection conn = BasicDbUtil
                .getConnection(OiyokanSettingsUtil.getOiyokanInternalDatabase(settingsOiyokan))) {

            // テーブルをセットアップ.
            TinyH2DbSample.createTable(conn);

            // テーブルデータをセットアップ.
            TinyH2DbSample.setupTableData(conn);

            try (var stmt = conn.prepareStatement("SELECT ID, Name, Description FROM MyProducts ORDER BY ID LIMIT 3")) {
                stmt.executeQuery();
                var rset = stmt.getResultSet();
                assertEquals(true, rset.next());
            }
        }
    }

    @Test
    void testo2() throws Exception {
        // TODO このテストを、ODataRequestベースのものに書き換えた版を作成すること。

        final OiyokanSettings settingsOiyokan = OiyokanSettingsUtil.loadOiyokanSettings();
        try (Connection conn = BasicDbUtil
                .getConnection(OiyokanSettingsUtil.getOiyokanInternalDatabase(settingsOiyokan))) {
            // テーブルをセットアップ.
            TinyH2DbSample.createTable(conn);

            // テーブルデータをセットアップ.
            TinyH2DbSample.setupTableData(conn);

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
