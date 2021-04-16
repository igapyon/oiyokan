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
package jp.oiyokan.settings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanCsdlEntityContainer;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;

/**
 * oiyokan-settings.json ファイルに関する処理。
 */
public class OiyoSettingsUtil {
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
            System.err.println(OiyokanMessages.M024 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M024, 500, Locale.ENGLISH);
        }
    }

    /**
     * OiyokanSettingsDatabase 設定情報を取得.
     * 
     * @param databaseDefName Database setting name.
     * @return OiyokanSettingsDatabase setting info.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static OiyoSettingsDatabase getOiyokanDatabase(String databaseDefName) throws ODataApplicationException {
        if (databaseDefName == null) {
            // TODO FIXME メッセージ番号採番。
            // [M026] UNEXPECTED: Database settings NOT found
            System.err.println(OiyokanMessages.M026 + ": " + databaseDefName);
            throw new ODataApplicationException(OiyokanMessages.M026 + ": " + databaseDefName, 500, Locale.ENGLISH);
        }

        final OiyoSettings settingsOiyokan = OiyokanCsdlEntityContainer.getSettingsInstance();
        for (OiyoSettingsDatabase look : settingsOiyokan.getDatabase()) {
            if (databaseDefName.equals(look.getName())) {
                return look;
            }
        }

        // [M026] UNEXPECTED: Database settings NOT found
        System.err.println(OiyokanMessages.M026 + ": " + databaseDefName);
        throw new ODataApplicationException(OiyokanMessages.M026 + ": " + databaseDefName, 500, Locale.ENGLISH);
    }

    public static OiyoSettingsEntitySet getOiyokanEntitySet(String entitySetName) throws ODataApplicationException {
        if (entitySetName == null) {
            // TODO FIXME メッセージ番号採番。
            // [M026] UNEXPECTED: Database settings NOT found
            System.err.println(OiyokanMessages.M026 + ": " + entitySetName);
            throw new ODataApplicationException(OiyokanMessages.M026 + ": " + entitySetName, 500, Locale.ENGLISH);
        }

        final OiyoSettings settingsOiyokan = OiyokanCsdlEntityContainer.getSettingsInstance();
        for (OiyoSettingsEntitySet look : settingsOiyokan.getEntitySet()) {
            if (entitySetName.equals(look.getName())) {
                return look;
            }
        }

        // TODO FIXME メッセージ番号採番。
        // [M026] UNEXPECTED: Database settings NOT found
        System.err.println(OiyokanMessages.M026 + ": " + entitySetName);
        throw new ODataApplicationException(OiyokanMessages.M026 + ": " + entitySetName, 500, Locale.ENGLISH);
    }
}
