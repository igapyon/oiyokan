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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

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
    private static final Log logger = LogFactory.getLog(OiyoInfoUtil.class);

    /**
     * resources フォルダから設定ファイルを読み込み.
     * 
     * @return OiyokanSettings 設定情報.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static OiyoSettings loadOiyokanSettings() throws ODataApplicationException {
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: resources: load: settings: oiyokan-settings.json");

        // resources から読み込み。
        final ClassPathResource cpres = new ClassPathResource("oiyokan/oiyokan-settings.json");
        try (InputStream inStream = cpres.getInputStream()) {
            String strOiyokanSettings = StreamUtils.copyToString(inStream, Charset.forName("UTF-8"));

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(strOiyokanSettings, OiyoSettings.class);
        } catch (IOException ex) {
            ex.printStackTrace();
            // [M024] UNEXPECTED: Fail to load Oiyokan settings
            System.err.println(OiyokanMessages.IY7112 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.IY7112, 500, Locale.ENGLISH);
        }
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
            System.err.println(OiyokanMessages.IY7114 + ": " + databaseDefName);
            throw new ODataApplicationException(OiyokanMessages.IY7114 + ": " + databaseDefName, 500, Locale.ENGLISH);
        }

        final OiyoSettings settingsOiyokan = oiyoInfo.getSettings();
        for (OiyoSettingsDatabase look : settingsOiyokan.getDatabase()) {
            if (databaseDefName.equals(look.getName())) {
                return look;
            }
        }

        // [M026] UNEXPECTED: Database settings NOT found
        System.err.println(OiyokanMessages.IY7114 + ": " + databaseDefName);
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
            System.err.println(OiyokanMessages.IY7120 + ": " + entitySetName);
            throw new ODataApplicationException(OiyokanMessages.IY7120 + ": " + entitySetName, 500, Locale.ENGLISH);
        }

        final OiyoSettings settingsOiyokan = oiyoInfo.getSettings();
        for (OiyoSettingsEntitySet entitySet : settingsOiyokan.getEntitySet()) {
            if (entitySetName.equals(entitySet.getName())) {
                return entitySet;
            }
        }

        // [IY7121] UNEXPECTED: EntitySet settings NOT found.
        logger.error(OiyokanMessages.IY7121 + ": " + entitySetName);
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
        System.err.println(OiyokanMessages.IY7122 + ": entitySet:" + entitySetName + ", property:" + propertyName);
        throw new ODataApplicationException(
                OiyokanMessages.IY7122 + ": entitySet:" + entitySetName + ", property:" + propertyName, //
                500, Locale.ENGLISH);
    }
}
