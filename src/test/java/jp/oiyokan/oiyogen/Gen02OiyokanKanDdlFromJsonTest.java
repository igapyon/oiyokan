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
import java.io.StringReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;

/**
 * Generate oiyokanKan-ddl.sql
 */
class Gen02OiyokanKanDdlFromJsonTest {
    private static final Log log = LogFactory.getLog(Gen02OiyokanKanDdlFromJsonTest.class);

    @Test
    void test01() throws Exception {
        new File("./target/").mkdirs();

        final File existJsonFile = new File("./target/generated-oiyokan/auto-generated-oiyokanKan-settings.json");
        if (!existJsonFile.exists()) {
            return;
        }

        final String existJson = FileUtils.readFileToString(existJsonFile, "UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        final OiyoSettings oiyoSettings = mapper.readValue(new StringReader(existJson), OiyoSettings.class);

        final StringBuilder sql = new StringBuilder();
        for (OiyoSettingsEntitySet entitySet : oiyoSettings.getEntitySet()) {
            OiyoSettingsDatabase database = null;
            for (OiyoSettingsDatabase look : oiyoSettings.getDatabase()) {
                if (entitySet.getDbSettingName().equals(look.getName())) {
                    database = look;
                }
            }
            if (database == null) {
                throw new IllegalArgumentException("database def NOT found. Corrupted json file.");
            }

            OiyokanConstants.DatabaseType databaseType = OiyokanConstants.DatabaseType.valueOf(database.getType());

            OiyokanSettingsGenUtil.generateDdl(databaseType, entitySet, sql);
        }

        final File generateFile = new File("./target/generated-oiyokan/auto-generated-oiyokanKan-ddl.sql");
        FileUtils.writeStringToFile(generateFile, sql.toString(), "UTF-8");
        log.info("oiyokan kan ddl file generated: " + generateFile.getCanonicalPath());
    }
}
