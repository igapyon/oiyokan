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
package jp.oiyokan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.data.OiyokanResourceSqlUtil;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;

/**
 * テスト実施用途のユーティリティクラス。
 */
public class OiyokanUnittestUtil {
    private static final Log log = LogFactory.getLog(OiyokanUnittestUtil.class);

    private static final String[][] OIYOKAN_FILE_SQLS = new String[][] { //
            { "oiyoUnitTestDb", "/oiyokan/sql/oiyokan-unittest-db-h2.sql" }, //
    };

    public static synchronized OiyoInfo setupUnittestDatabase() throws ODataApplicationException {
        final OiyoInfo oiyoInfo = new OiyoInfo();
        // シングルトンな OiyoSettings を利用。
        OiyokanEdmProvider.setupOiyoSettingsInstance(oiyoInfo);

        boolean isUnittestDatabaseExists = false;
        for (OiyoSettingsDatabase database : oiyoInfo.getSettings().getDatabase()) {
            if (database.getName().equals("oiyoUnitTestDb")) {
                isUnittestDatabaseExists = true;
            }
        }
        if (isUnittestDatabaseExists) {
            // Already loaded.
            return oiyoInfo;
        }

        if (true) {
            String settings = "/oiyokan/oiyokan-unittest-settings.json";
            log.trace("Unittest: OData v4: resources: load: " + settings);
            // resources から読み込み。
            try {
                final String strOiyokanSettings = IOUtils.resourceToString(settings, StandardCharsets.UTF_8);
                final ObjectMapper mapper = new ObjectMapper();
                final OiyoSettings loadedSettings = mapper.readValue(strOiyokanSettings, OiyoSettings.class);
                for (OiyoSettingsDatabase database : loadedSettings.getDatabase()) {
                    log.trace("load: database: " + database.getName());
                    oiyoInfo.getSettings().getDatabase().add(database);
                }
                for (OiyoSettingsEntitySet entitySet : loadedSettings.getEntitySet()) {
                    log.trace("Unittest: load: entitySet: " + entitySet.getName());
                    oiyoInfo.getSettings().getEntitySet().add(entitySet);
                }
            } catch (IOException ex) {
                // [M024] UNEXPECTED: Fail to load Oiyokan settings
                log.error(OiyokanMessages.IY7112 + ": " + ex.toString());
                // しかし例外は発生させず処理続行。
            }
        }

        try {
            for (String[] sqlFileDef : OIYOKAN_FILE_SQLS) {
                log.trace("Unittest: load: internal db:" + sqlFileDef[0] + ", sql: " + sqlFileDef[1]);

                OiyoSettingsDatabase lookDatabase = OiyoInfoUtil.getOiyoDatabaseByName(oiyoInfo, sqlFileDef[0]);

                try (Connection connLoookDatabase = OiyoCommonJdbcUtil.getConnection(lookDatabase)) {
                    final String[] sqls = OiyokanResourceSqlUtil.loadOiyokanResourceSql(sqlFileDef[1]);
                    for (String sql : sqls) {
                        try (var stmt = connLoookDatabase.prepareStatement(sql.trim())) {
                            stmt.executeUpdate();
                            connLoookDatabase.commit();
                        } catch (SQLException ex) {
                            log.error("UNEXPECTED: Fail to execute SQL for local internal table(2): " + ex.toString());
                            throw new ODataApplicationException(
                                    "UNEXPECTED: Fail to execute SQL for local internal table(2)", 500, Locale.ENGLISH);
                        }
                    }

                    log.trace("OData: load: internal db: end: " + sqlFileDef[0] + ", sql: " + sqlFileDef[1]);
                } catch (SQLException ex) {
                    log.error("UNEXPECTED: Fail to execute Dabaase: " + ex.toString());
                    throw new ODataApplicationException("UNEXPECTED: Fail to execute Dabaase", 500, Locale.ENGLISH);
                }
            }
        } catch (ODataApplicationException ex) {
            // とめる。
        }

        return oiyoInfo;
    }
}
