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
package jp.oiyokan.oiyogen;

import java.io.File;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsEntityType;
import jp.oiyokan.dto.OiyoSettingsProperty;

/**
 * 内部データベース用のCSDL用内部テーブルのDDLをコマンドライン生成.
 */
class GenOiyoSettingsJsonTest {
    private static final String TARGET_UNITTEST_DATABASE = "oiyoUnitTestDb";

    /**
     * このテストを実施するかどうか。
     */
    private static final boolean IS_PROCESS = true;

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

        final OiyoInfo oiyoInfo = new OiyoInfo();
        oiyoInfo.setSettings(OiyoInfoUtil.loadOiyokanSettings());

        OiyoSettingsDatabase settingsDatabase = OiyoInfoUtil.getOiyoDatabaseByName(oiyoInfo, TARGET_UNITTEST_DATABASE);
        System.err.println("確認対象データベース: " + settingsDatabase.getName());

        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(settingsDatabase)) {
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

                // for ORCL18
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
            }

            Collections.sort(tableNameList);

            final OiyoSettings oiyoSettings = new OiyoSettings();
            oiyoSettings.setEntitySet(new ArrayList<>());

            if (true) {
                // データベース設定も生成.
                oiyoSettings.setNamespace("Oiyokan");
                oiyoSettings.setContainerName("Container");
                oiyoSettings.setDatabase(new ArrayList<>());

                final String[][] DATABASE_SETTINGS = new String[][] {
                        { "oiyokanKan", "h2", "Oiyokan internal DB. Do not change.", //
                                "org.h2.Driver", //
                                "jdbc:h2:mem:oiyokan;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE;MODE=MSSQLServer", //
                                "sa", "" }, //
                        { "oiyoUnitTestDb", "h2", "Oiyokan internal Target Test DB. Used for build unit test.", //
                                "org.h2.Driver", //
                                "jdbc:h2:file:./src/main/resources/db/oiyokan-internal;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE;MODE=MSSQLServer", //
                                "sa", "" }, //
                        { "postgres1", "postgres",
                                "Sample postgres settings. Change the settings to suit your environment.", //
                                "org.postgresql.Driver", //
                                "jdbc:postgresql://localhost:5432/dvdrental", //
                                "", "" }, //
                        { "mysql1", "MySQL", "Sample MySQL settings. Change the settings to suit your environment.", //
                                "com.mysql.jdbc.Driver", //
                                "jdbc:mysql://localhost/mysql?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&useCursorFetch=true&defaultFetchSize=128&useServerPrepStmts=true&emulateUnsupportedPstmts=false", //
                                "root", "passwd123" }, //
                        { "mysql2", "MySQL",
                                "Sample MySQL settings for Sakila. Change the settings to suit your environment.", //
                                "com.mysql.jdbc.Driver", //
                                "jdbc:mysql://localhost/sakila?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&useCursorFetch=true&defaultFetchSize=128&useServerPrepStmts=true&emulateUnsupportedPstmts=false", //
                                "root", "passwd123" }, //
                        { "mssql1", "SQLSV2008",
                                "Sample MS SQL Server 2008 settings. Change the settings to suit your environment.", //
                                "com.microsoft.sqlserver.jdbc.SQLServerDriver", //
                                "jdbc:sqlserver://localhost\\SQLExpress", //
                                "sa", "passwd123" }, //
                        { "oracle1", "ORCL18",
                                "Sample Oracle XE (18c) settings. Change the settings to suit your environment.", //
                                "oracle.jdbc.driver.OracleDriver", //
                                "jdbc:oracle:thin:@10.0.2.15:1521/xepdb1", //
                                "orauser", "passwd123" }, //
                };

                for (String[] databaseSetting : DATABASE_SETTINGS) {
                    OiyoSettingsDatabase database = new OiyoSettingsDatabase();
                    oiyoSettings.getDatabase().add(database);
                    database.setName(databaseSetting[0]);
                    database.setType(databaseSetting[1]);
                    database.setDescription(databaseSetting[2]);
                    database.setJdbcDriver(databaseSetting[3]);
                    database.setJdbcUrl(databaseSetting[4]);
                    database.setJdbcUser(databaseSetting[5]);
                    database.setJdbcPass(databaseSetting[6]);
                }

            }

            if (true) {
                OiyoSettingsEntitySet entitySet = new OiyoSettingsEntitySet();
                oiyoSettings.getEntitySet().add(entitySet);
                entitySet.setName("Oiyokans");
                entitySet.setDescription("Oiyokan internal info. Do not change.");
                entitySet.setDbSettingName("oiyokanKan");
                entitySet.setCanCreate(false);
                entitySet.setCanRead(true);
                entitySet.setCanUpdate(false);
                entitySet.setCanDelete(false);
                entitySet.setOmitCountAll(true);
                entitySet.setEntityType(new OiyoSettingsEntityType());
                entitySet.getEntityType().setName("Oiyokan");
                entitySet.getEntityType().setDbName("Oiyokan");
                entitySet.getEntityType().setKeyName(new ArrayList<>());
                entitySet.getEntityType().getKeyName().add("KeyName");
                entitySet.getEntityType().setProperty(new ArrayList<>());

                OiyoSettingsProperty prop = new OiyoSettingsProperty();
                entitySet.getEntityType().getProperty().add(prop);
                prop.setName("KeyName");
                prop.setDbName("KeyName");
                prop.setEdmType("Edm.String");
                prop.setJdbcType("Types.VARCHAR");
                prop.setDbType("VARCHAR");
                prop.setJdbcSetMethod("setString");
                prop.setNullable(false);
                prop.setMaxLength(20);
                prop.setLengthFixed(false);
                prop.setPrecision(null);
                prop.setScale(null);

                prop = new OiyoSettingsProperty();
                entitySet.getEntityType().getProperty().add(prop);
                prop.setName("KeyValue");
                prop.setDbName("KeyValue");
                prop.setEdmType("Edm.String");
                prop.setJdbcType("Types.VARCHAR");
                prop.setDbType("VARCHAR");
                prop.setJdbcSetMethod("setString");
                prop.setNullable(true);
                prop.setMaxLength(255);
                prop.setLengthFixed(false);
                prop.setPrecision(null);
                prop.setScale(null);
            }

            for (String tableName : tableNameList) {
                // System.err.println("tabname: "+tableName);
                try {
                    oiyoSettings.getEntitySet().add(OiyokanSettingsGenUtil.generateCreateOiyoJson(connTargetDb,
                            tableName, OiyokanConstants.DatabaseType.valueOf(settingsDatabase.getType())));
                } catch (Exception ex) {
                    System.err.println(ex.toString());
                }
            }

            for (OiyoSettingsEntitySet entitySet : oiyoSettings.getEntitySet()) {
                if ("ODataTest1".equals(entitySet.getEntityType().getDbName())) {
                    entitySet.setName("ODataTests1");
                }
                if ("ODataTest2".equals(entitySet.getEntityType().getDbName())) {
                    entitySet.setName("ODataTests2");
                }
                if ("ODataTest3".equals(entitySet.getEntityType().getDbName())) {
                    entitySet.setName("ODataTests3");
                }
                if ("OData Test4".equals(entitySet.getEntityType().getDbName())) {
                    entitySet.setName("ODataTests4");
                    entitySet.getEntityType().setName("ODataTest4");
                }
                if ("ODataTest5".equals(entitySet.getEntityType().getDbName())) {
                    entitySet.setName("ODataTests5");
                }
                if ("ODataTest6".equals(entitySet.getEntityType().getDbName())) {
                    entitySet.setName("ODataTests6");
                }
                if ("ODataTest7".equals(entitySet.getEntityType().getDbName())) {
                    entitySet.setName("ODataTests7");
                }
            }

            for (OiyoSettingsEntitySet entitySet : oiyoSettings.getEntitySet()) {
                if (entitySet.getName().equals("actors")) {
                    entitySet.setName("SklActors");
                    entitySet.getEntityType().setName("SklActor");
                } else if (entitySet.getName().equals("actor_infos")) {
                    entitySet.setName("SklActorInfos");
                    entitySet.getEntityType().setName("SklActorInfo");
                } else if (entitySet.getName().equals("addresss")) {
                    entitySet.setName("SklAddresses");
                    entitySet.getEntityType().setName("SklAddress");
                } else if (entitySet.getName().equals("categorys")) {
                    entitySet.setName("SklCategories");
                    entitySet.getEntityType().setName("SklCategory");
                } else if (entitySet.getName().equals("citys")) {
                    entitySet.setName("SklCities");
                    entitySet.getEntityType().setName("SklCity");
                } else if (entitySet.getName().equals("countrys")) {
                    entitySet.setName("SklCountries");
                    entitySet.getEntityType().setName("SklCountry");
                } else if (entitySet.getName().equals("customers")) {
                    entitySet.setName("SklCustomers");
                    entitySet.getEntityType().setName("SklCustomer");
                } else if (entitySet.getName().equals("customer_lists")) {
                    entitySet.setName("SklCustomerLists");
                    entitySet.getEntityType().setName("SklCustomerList");
                } else if (entitySet.getName().equals("films")) {
                    entitySet.setName("SklFilms");
                    entitySet.getEntityType().setName("SklFilm");
                } else if (entitySet.getName().equals("film_actors")) {
                    entitySet.setName("SklFilmActors");
                    entitySet.getEntityType().setName("SklFilmActor");
                } else if (entitySet.getName().equals("film_categorys")) {
                    entitySet.setName("SklFilmCategories");
                    entitySet.getEntityType().setName("SklFilmCategory");
                } else if (entitySet.getName().equals("film_lists")) {
                    entitySet.setName("SklFilmLists");
                    entitySet.getEntityType().setName("SklFilmList");
                } else if (entitySet.getName().equals("inventorys")) {
                    entitySet.setName("SklInventories");
                    entitySet.getEntityType().setName("SklInventory");
                } else if (entitySet.getName().equals("languages")) {
                    entitySet.setName("SklLanguages");
                    entitySet.getEntityType().setName("SklLanguage");
                } else if (entitySet.getName().equals("nicer_but_slower_film_lists")) {
                    entitySet.setName("SklNicerButSlowerFilmLists");
                    entitySet.getEntityType().setName("SklNicerButSlowerFilmList");
                } else if (entitySet.getName().equals("payments")) {
                    entitySet.setName("SklPayments");
                    entitySet.getEntityType().setName("SklPayment");
                } else if (entitySet.getName().equals("rentals")) {
                    entitySet.setName("SklRentals");
                    entitySet.getEntityType().setName("SklRental");
                } else if (entitySet.getName().equals("sales_by_film_categorys")) {
                    entitySet.setName("SklSalesByFilmCategories");
                    entitySet.getEntityType().setName("SklSalesByFilmCategory");
                } else if (entitySet.getName().equals("sales_by_stores")) {
                    entitySet.setName("SklSalesByStores");
                    entitySet.getEntityType().setName("SklSalesByStore");
                } else if (entitySet.getName().equals("staffs")) {
                    entitySet.setName("SklStaffs");
                    entitySet.getEntityType().setName("SklStaff");
                } else if (entitySet.getName().equals("staff_lists")) {
                    entitySet.setName("SklStaffLists");
                    entitySet.getEntityType().setName("SklStaffList");
                } else if (entitySet.getName().equals("stores")) {
                    entitySet.setName("SklStores");
                    entitySet.getEntityType().setName("SklStore");
                }
            }

            StringWriter writer = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(writer, oiyoSettings);
            writer.flush();

            new File("./target/generated-oiyokan").mkdirs();
            final File generateFile = new File("./target/generated-oiyokan/auto-generated-oiyokan-settings.json");
            FileUtils.writeStringToFile(generateFile, writer.toString(), "UTF-8");
            System.err.println("sample oiyokan setting file generated: " + generateFile.getCanonicalPath());
        }
    }
}
