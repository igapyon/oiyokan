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
package jp.oiyokan.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsProperty;

/**
 * oiyokan-settings.json ファイルに関する処理。
 */
public class OiyoInfoUtil {
    private static final Log log = LogFactory.getLog(OiyoInfoUtil.class);

    /**
     * resources フォルダから設定ファイルを読み込み.
     * 
     * @return OiyokanSettings 設定情報.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static OiyoSettings loadOiyokanSettings() throws ODataApplicationException {
        // [IY7173] INFO: start to load oiyokan settings
        log.info(OiyokanMessages.IY7173);

        final OiyoSettings mergedOiyoSettings = new OiyoSettings();
        mergedOiyoSettings.setDatabase(new ArrayList<>());
        mergedOiyoSettings.setEntitySet(new ArrayList<>());

        final String[] OIYOKAN_SETTINGS = new String[] { //
                "/oiyokan/oiyokanKan-settings.json", //
                "/oiyokan/oiyokan-settings.json", //
        };

        for (String settings : OIYOKAN_SETTINGS) {
            // [IY7174] INFO: load oiyokan settings
            log.info(OiyokanMessages.IY7174 + ": " + settings);
            // resources から読み込み。
            try {
                final String strOiyokanSettings = IOUtils.resourceToString(settings, StandardCharsets.UTF_8);

                final ObjectMapper mapper = new ObjectMapper();
                final OiyoSettings loadedSettings = mapper.readValue(strOiyokanSettings, OiyoSettings.class);
                if (mergedOiyoSettings.getNamespace() == null) {
                    // [IY6101] INFO: settings: load namespace
                    log.info(OiyokanMessages.IY6101 + ": " + loadedSettings.getNamespace());
                    mergedOiyoSettings.setNamespace(loadedSettings.getNamespace());
                }
                if (mergedOiyoSettings.getContainerName() == null) {
                    // [IY6102] INFO: settings: load containerName
                    log.info(OiyokanMessages.IY6102 + ": " + loadedSettings.getContainerName());
                    mergedOiyoSettings.setContainerName(loadedSettings.getContainerName());
                }
                for (OiyoSettingsDatabase database : loadedSettings.getDatabase()) {
                    // [IY6103] INFO: settings: load database
                    log.info(OiyokanMessages.IY6103 + ": " + database.getName());
                    mergedOiyoSettings.getDatabase().add(database);
                }
                for (OiyoSettingsEntitySet entitySet : loadedSettings.getEntitySet()) {
                    // [IY6104] INFO: settings: load entitySet
                    log.info(OiyokanMessages.IY6104 + ": " + entitySet.getName());
                    mergedOiyoSettings.getEntitySet().add(entitySet);

                    for (OiyoSettingsProperty property : entitySet.getEntityType().getProperty()) {
                        if (property.getAutoGenKey() != null && property.getAutoGenKey()) {
                            if (property.getNullable() != null && property.getNullable() == false) {
                                // [IY6151] WARN: Overwrite nullable with true because autoGenKey for property
                                // is true.
                                log.warn(OiyokanMessages.IY6151 + ": " + "EntitySet:" + entitySet.getName()
                                        + ", Property:" + property.getName());
                                property.setNullable(true);
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                // [M024] WARN: Fail to load Oiyokan settings
                log.warn(OiyokanMessages.IY7112 + ": " + ex.toString());
                // 例外は発生させない。そのまま処理続行する。
            }
        }

        return mergedOiyoSettings;
    }

    /**
     * OiyokanSettingsDatabase 設定情報を取得.
     * 
     * @param databaseDefName Database setting name.
     * @return OiyokanSettingsDatabase setting info.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static OiyoSettingsDatabase getOiyoDatabaseByName(OiyoInfo oiyoInfo, String databaseDefName)
            throws ODataApplicationException {
        if (databaseDefName == null) {
            // [M026] UNEXPECTED: Database settings NOT found
            log.fatal(OiyokanMessages.IY7114 + ": " + databaseDefName);
            throw new ODataApplicationException(OiyokanMessages.IY7114 + ": " + databaseDefName, 500, Locale.ENGLISH);
        }

        final OiyoSettings settingsOiyokan = oiyoInfo.getSettings();
        for (OiyoSettingsDatabase look : settingsOiyokan.getDatabase()) {
            if (databaseDefName.equals(look.getName())) {
                return look;
            }
        }

        // [M026] UNEXPECTED: Database settings NOT found
        log.fatal(OiyokanMessages.IY7114 + ": " + databaseDefName);
        throw new ODataApplicationException(OiyokanMessages.IY7114 + ": " + databaseDefName, 500, Locale.ENGLISH);
    }

    public static OiyoSettingsDatabase getOiyoDatabaseByEntitySetName(OiyoInfo oiyoInfo, String entitySetName)
            throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = getOiyoEntitySet(oiyoInfo, entitySetName);

        return getOiyoDatabaseByName(oiyoInfo, entitySet.getDbSettingName());
    }

    public static OiyokanConstants.DatabaseType getOiyoDatabaseTypeByEntitySetName(OiyoInfo oiyoInfo,
            String entitySetName) throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = getOiyoEntitySet(oiyoInfo, entitySetName);

        OiyoSettingsDatabase database = getOiyoDatabaseByName(oiyoInfo, entitySet.getDbSettingName());

        return OiyokanConstants.DatabaseType.valueOf(database.getType());
    }

    public static OiyoSettingsEntitySet getOiyoEntitySet(OiyoInfo oiyoInfo, String entitySetName)
            throws ODataApplicationException {
        if (entitySetName == null) {
            // [M038] UNEXPECTED: null parameter given as EntitySet.
            log.error(OiyokanMessages.IY7120 + ": " + entitySetName);
            throw new ODataApplicationException(OiyokanMessages.IY7120 + ": " + entitySetName, 500, Locale.ENGLISH);
        }

        final OiyoSettings settingsOiyokan = oiyoInfo.getSettings();
        for (OiyoSettingsEntitySet entitySet : settingsOiyokan.getEntitySet()) {
            if (entitySetName.equals(entitySet.getName())) {
                return entitySet;
            }
        }

        // [IY7121] ERROR: Specified EntitySet settings NOT found.
        log.error(OiyokanMessages.IY7121 + ": " + entitySetName);
        throw new ODataApplicationException(OiyokanMessages.IY7121 + ": " + entitySetName, //
                OiyokanMessages.IY7121_CODE, Locale.ENGLISH);
    }

    public static OiyoSettingsProperty getOiyoEntityProperty(OiyoInfo oiyoInfo, String entitySetName,
            String propertyName) throws ODataApplicationException {
        OiyoSettingsEntitySet entitySet = getOiyoEntitySet(oiyoInfo, entitySetName);

        for (OiyoSettingsProperty prop : entitySet.getEntityType().getProperty()) {
            if (prop.getName().equals(propertyName)) {
                return prop;
            }
        }

        // [M040] UNEXPECTED: EntitySet Property settings NOT found.
        log.error(OiyokanMessages.IY7122 + ": entitySet:" + entitySetName + ", property:" + propertyName);
        throw new ODataApplicationException(
                OiyokanMessages.IY7122 + ": entitySet:" + entitySetName + ", property:" + propertyName, //
                500, Locale.ENGLISH);
    }
}
