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

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import jp.oiyokan.basic.OiyoBasicJdbcUtil;
import jp.oiyokan.data.OiyokanKanDatabase;
import jp.oiyokan.dto.Oiyo13SettingsDatabase;
import jp.oiyokan.settings.OiyoSettingsUtil;

/**
 * 内部データベース用のCSDL用内部テーブルのDDLをコマンドライン生成.
 */
class GenOiyoGenericAllTest {
    private static final String TARGET_UNITTEST_DATABASE = "oiyoUnitTestDb";

    /**
     * このテストを実施するかどうか。
     */
    private static final boolean IS_PROCESS = false;

    private static final boolean SHOW_JDBCINFO = true;

    /**
     * 全テーブルを取得.
     * 
     * Oiyoテーブルのスキーマを取得したい場合にのみ JUnit を実行する。
     */
    @Test
    void test01() throws Exception {
        if (!IS_PROCESS) {
            return;
        }

        Oiyo13SettingsDatabase settingsDatabase = OiyoSettingsUtil.getOiyokanDatabase(TARGET_UNITTEST_DATABASE);

        try (Connection connTargetDb = OiyoBasicJdbcUtil.getConnection(settingsDatabase)) {
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

                // for ORACLE XE
                if (tableName.contains("$") //
                        || tableName.equals("AV_DUAL") //
                        || tableName.equals("CATALOG") //
                        || tableName.equals("COL") //
                        || tableName.equals("HELP") //
                        || tableName.equals("SYNONYMS") //
                        || tableName.equals("SYSCATALOG") //
                        || tableName.equals("SYSFILES") //
                        || tableName.equals("SYSSEGOBJ") //
                        || tableName.equals("TAB") //
                        || tableName.equals("TABQUOTAS") //
                        || tableName.startsWith("ALL$") //
                        || tableName.startsWith("ALL_") //
                        || tableName.startsWith("AUDITABLE_") //
                        || tableName.startsWith("AUDIT_") //
                        || tableName.startsWith("AW$") //
                        || tableName.startsWith("CDB_") //
                        || tableName.startsWith("COAD$") //
                        || tableName.startsWith("COLUMN_") //
                        || tableName.startsWith("CS_") //
                        || tableName.startsWith("CTX_") //
                        || tableName.startsWith("DATABASE_") //
                        || tableName.startsWith("DATAPUMP_") //
                        || tableName.startsWith("DATA_PUMP_") //
                        || tableName.startsWith("DBA_") //
                        || tableName.startsWith("DBMS_") //
                        || tableName.startsWith("DICTIONARY") //
                        || tableName.startsWith("DICT_") //
                        || tableName.startsWith("DM_") //
                        || tableName.startsWith("DOCUMENT_") //
                        || tableName.startsWith("DR$") //
                        || tableName.startsWith("DRV$") //
                        || tableName.startsWith("EXP") //
                        || tableName.startsWith("EXU") //
                        || tableName.startsWith("FLASHBACK_") //
                        || tableName.startsWith("GEO") //
                        || tableName.startsWith("GLOBAL_") //
                        || tableName.startsWith("HS_") //
                        || tableName.startsWith("IMP") //
                        || tableName.startsWith("INDEX_") //
                        || tableName.startsWith("JAVAS") //
                        || tableName.startsWith("KU_") //
                        || tableName.startsWith("LOADER_") //
                        || tableName.startsWith("LOCAL_") //
                        || tableName.startsWith("MY_SDO") //
                        || tableName.startsWith("NLS_") //
                        || tableName.startsWith("NTV2_") //
                        || tableName.startsWith("OGIS_") //
                        || tableName.startsWith("ORA_") //
                        || tableName.startsWith("ORDDCM_") //
                        || tableName.startsWith("PATH_") //
                        || tableName.startsWith("PRODUCT_") //
                        || tableName.startsWith("PSTU") //
                        || tableName.startsWith("PUBL") //
                        || tableName.startsWith("QUEUE_") //
                        || tableName.startsWith("RDF_") //
                        || tableName.startsWith("REDACTION_") //
                        || tableName.startsWith("REPORT_") //
                        || tableName.startsWith("RESOURCE_") //
                        || tableName.startsWith("ROLE_") //
                        || tableName.startsWith("RULE_") //
                        || tableName.startsWith("SAM_") //
                        || tableName.startsWith("SCHEDULER_") //
                        || tableName.startsWith("SCHEMA_") //
                        || tableName.startsWith("SDO_") //
                        || tableName.startsWith("SESSION_") //
                        || tableName.startsWith("SI_") //
                        || tableName.startsWith("SPD_") //
                        || tableName.startsWith("SQT_") //
                        || tableName.startsWith("SRS") //
                        || tableName.startsWith("STMT_") //
                        || tableName.startsWith("SYSTEM_") //
                        || tableName.startsWith("TABLESPACE_") //
                        || tableName.startsWith("TABLE_") //
                        || tableName.startsWith("TRANSPORTABLE_") //
                        || tableName.startsWith("USABLE_") //
                        || tableName.startsWith("USER_") //
                        || tableName.startsWith("UTL_") //
                        || tableName.startsWith("WM_") //
                        || tableName.startsWith("XDB$") //
                        || tableName.startsWith("XDB_") //
                        || tableName.startsWith("XDS_") //
                        || tableName.startsWith("_") //
                ) {
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

            StringBuilder output = new StringBuilder();
            for (String tableName : tableNameList) {
                // System.err.println("tabname: "+tableName);
                String sql = OiyokanKanDatabase.generateCreateOiyoDdl(connTargetDb, tableName);
                output.append(sql);
                System.err.println(sql);
            }

            new File("./target/").mkdirs();
            FileUtils.writeStringToFile(new File("./target/GenOiyoGenericAllTest-output.sql"), output.toString(),
                    "UTF-8");
        }
    }
}
