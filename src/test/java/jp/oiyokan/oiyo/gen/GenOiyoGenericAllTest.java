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
package jp.oiyokan.oiyo.gen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import jp.oiyokan.basic.BasicJdbcUtil;
import jp.oiyokan.data.OiyokanInternalDatabase;
import jp.oiyokan.dto.OiyokanSettingsDatabase;
import jp.oiyokan.settings.OiyokanSettingsUtil;

/**
 * 内部データベース用のCSDL用内部テーブルのDDLをコマンドライン生成.
 */
class GenOiyoGenericAllTest {
    private static final String TARGET_SETTINGS_DATABASE = "oiyokanInternalTarget";

    private static final boolean SHOW_JDBCINFO = true;

    /**
     * 全テーブルを取得.
     * 
     * Oiyoテーブルのスキーマを取得したい場合にのみ JUnit を実行する。
     */
    // @Test
    void test01() throws Exception {
        OiyokanSettingsDatabase settingsDatabase = OiyokanSettingsUtil.getOiyokanDatabase(TARGET_SETTINGS_DATABASE);

        try (Connection connTargetDb = BasicJdbcUtil.getConnection(settingsDatabase)) {
            final List<String> tableNameList = new ArrayList<>();

            ResultSet rset = connTargetDb.getMetaData().getTables(null, "%", "%", new String[] { "TABLE", "VIEW" });
            for (; rset.next();) {
                final String tableName = rset.getString("TABLE_NAME");
                final String tableCat = rset.getString("TABLE_CAT");

                if (SHOW_JDBCINFO) {
                    System.err.println(
                            "table: " + rset.getString("TABLE_NAME") + " (" + rset.getString("TABLE_TYPE") + ")");
                    System.err.println("    TABLE_CAT   : " + rset.getString("TABLE_CAT"));
                    // System.err.println(" TABLE_SCHEM : " + rset.getString("TABLE_SCHEM"));
                }

                // for h2 database
                if ("IGNORELIST".equals(tableName) //
                        || "INDEXES".equals(tableName)//
                        || "MAP".equals(tableName)//
                        || "ROWS".equals(tableName)//
                        || "SETTINGS".equals(tableName)//
                        || "WORDS".equals(tableName)) {
                    System.err.println("    skip.");
                    continue;
                }

                // for MySQL.
                if ("sys".equals(tableCat)) {
                    if (SHOW_JDBCINFO) {
                        System.err.println("    skip.");
                    }
                    continue;
                }

                tableNameList.add(tableName);

                if (SHOW_JDBCINFO) {
                    System.err.println("");
                }

            }

            Collections.sort(tableNameList);

            System.err.println("Oiyo候補: " + settingsDatabase.getName());
            System.err.println("");

            for (String tableName : tableNameList) {
                System.err.println(OiyokanInternalDatabase.generateCreateOiyoDdl(connTargetDb, tableName));
            }
        }
    }
}
