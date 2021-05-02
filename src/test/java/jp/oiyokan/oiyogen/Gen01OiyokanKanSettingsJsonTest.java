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
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsEntityType;
import jp.oiyokan.dto.OiyoSettingsProperty;

/**
 * Generate oiyokanKan-settings.json
 */
class Gen01OiyokanKanSettingsJsonTest {
    private static final Log log = LogFactory.getLog(Gen01OiyokanKanSettingsJsonTest.class);

    @Test
    void test01() throws Exception {
        final OiyoSettings oiyoSettings = new OiyoSettings();
        oiyoSettings.setEntitySet(new ArrayList<>());

        oiyoSettings.setNamespace("Oiyokan");
        oiyoSettings.setContainerName("Container");
        oiyoSettings.setDatabase(new ArrayList<>());

        final String[][] DATABASE_SETTINGS = new String[][] { //
                { "oiyokanKan", "h2", "Oiyokan kanri DB.", //
                        "org.h2.Driver", //
                        "jdbc:h2:mem:oiyokan;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE;MODE=MSSQLServer", //
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
            database.setJdbcPass(databaseSetting[6]);
        }

        OiyoSettingsEntitySet entitySet = new OiyoSettingsEntitySet();
        oiyoSettings.getEntitySet().add(entitySet);
        entitySet.setName("Oiyokan");
        entitySet.setDescription("Oiyokan internal info. Do not change.");
        entitySet.setDbSettingName("oiyokanKan");
        entitySet.setCanCreate(false);
        entitySet.setCanRead(true);
        entitySet.setCanUpdate(false);
        entitySet.setCanDelete(false);
        entitySet.setOmitCountAll(false);
        entitySet.setJdbcStmtTimeout(30);
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
        prop.setAutoGenKey(false);
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
        prop.setDbDefault("Default Value");
        prop.setFilterTreatNullAsBlank(true);

        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(writer, oiyoSettings);
        writer.flush();

        new File("./target/generated-oiyokan").mkdirs();
        final File generateFile = new File("./target/generated-oiyokan/auto-generated-oiyokanKan-settings.json");
        FileUtils.writeStringToFile(generateFile, writer.toString(), "UTF-8");
        log.info("oiyokan kanri setting file auto-generated: " + generateFile.getCanonicalPath());
    }
}
