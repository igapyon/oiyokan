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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanUnittestUtil;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.util.OiyoEncryptUtil;

/**
 * Generate oiyokan-unittest-settings.json
 */
class Gen01OiyokanUnittestSettingsJsonTest {
    private static final Log log = LogFactory.getLog(Gen01OiyokanUnittestSettingsJsonTest.class);

    private static final String TARGET_UNITTEST_DATABASE = "oiyoUnitTestDb";

    @Test
    void test01() throws Exception {
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        OiyoSettingsDatabase settingsDatabase = OiyoInfoUtil.getOiyoDatabaseByName(oiyoInfo, TARGET_UNITTEST_DATABASE);
        log.info("Target database: " + settingsDatabase.getName());

        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(settingsDatabase)) {
            final List<String> tableNameList = new ArrayList<>();

            ResultSet rset = connTargetDb.getMetaData().getTables(null, "%", "%", new String[] { "TABLE", "VIEW" });
            for (; rset.next();) {
                final String tableName = rset.getString("TABLE_NAME");
                tableNameList.add(tableName);
            }

            Collections.sort(tableNameList);

            final OiyoSettings oiyoSettings = new OiyoSettings();
            oiyoSettings.setEntitySet(new ArrayList<>());

            // ?????????????????????????????????.
            oiyoSettings.setNamespace("Oiyokan");
            oiyoSettings.setContainerName("Container");
            oiyoSettings.setDatabase(new ArrayList<>());

            final String[][] DATABASE_SETTINGS = new String[][] { //
                    { "oiyoUnitTestDb", "h2", "Oiyokan internal Target Test DB. Used for build unit test.", //
                            "org.h2.Driver", //
                            "jdbc:h2:mem:oiyoUnitTestDb;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE;MODE=MSSQLServer", //
                            "sa", "" }, //
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
                database.setJdbcPassEnc(OiyoEncryptUtil.encrypt(databaseSetting[6], oiyoInfo.getPassphrase()));
            }

            for (String tableName : tableNameList) {
                try {
                    final OiyoSettingsEntitySet entitySet = OiyokanSettingsGenUtil.generateSettingsEntitySet(
                            connTargetDb, tableName, OiyokanConstants.DatabaseType.valueOf(settingsDatabase.getType()));
                    oiyoSettings.getEntitySet().add(entitySet);
                    entitySet.setDbSettingName(TARGET_UNITTEST_DATABASE);
                } catch (Exception ex) {
                    log.warn("Fail to read table: " + tableName);
                }
            }

            // UnitTest???????????????????????????
            for (OiyoSettingsEntitySet entitySet : oiyoSettings.getEntitySet()) {
                if ("ODataTest1".equals(entitySet.getEntityType().getDbName())) {
                    entitySet.setName("ODataTest1");
                }
                if ("ODataTest2".equals(entitySet.getEntityType().getDbName())) {
                    entitySet.setName("ODataTest2");
                }
                if ("ODataTest3".equals(entitySet.getEntityType().getDbName())) {
                    entitySet.setName("ODataTest3");
                }
                if ("OData Test4".equals(entitySet.getEntityType().getDbName())) {
                    entitySet.setName("ODataTest4");
                    entitySet.getEntityType().setName("ODataTest4");
                }
                if ("ODataTest5".equals(entitySet.getEntityType().getDbName())) {
                    entitySet.setName("ODataTest5");
                }
                if ("ODataTest6".equals(entitySet.getEntityType().getDbName())) {
                    entitySet.setName("ODataTest6");
                }
                if ("ODataTest7".equals(entitySet.getEntityType().getDbName())) {
                    entitySet.setName("ODataTest7");
                }
            }

            StringWriter writer = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(writer, oiyoSettings);
            writer.flush();

            new File("./target/generated-oiyokan").mkdirs();
            final File generateFile = new File(
                    "./target/generated-oiyokan/auto-generated-oiyokan-unittest-settings.json");
            FileUtils.writeStringToFile(generateFile, writer.toString(), "UTF-8");
            log.info("oiyokan unittest setting file auto-generated: " + generateFile.getCanonicalPath());
        }
    }
}
