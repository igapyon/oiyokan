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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.oiyokan.dto.OiyokanSettings;
import jp.oiyokan.dto.OiyokanSettingsDatabase;

/**
 * oiyokan-settings.json ファイルに関する処理。
 */
public class OiyokanSettingsUtil {
    /**
     * resources フォルダから設定ファイルを読み込み.
     * 
     * @throws ODataApplicationException
     */
    public static OiyokanSettings loadOiyokanSettings() throws ODataApplicationException {
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: resources: load oiyokan-settings.json");

        // resources から読み込み。
        final ClassPathResource cpres = new ClassPathResource("oiyokan-settings.json");
        try (InputStream inStream = cpres.getInputStream()) {
            String strOiyokanSettings = StreamUtils.copyToString(inStream, Charset.forName("UTF-8"));

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(strOiyokanSettings, OiyokanSettings.class);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new ODataApplicationException("UNEXPECTED: Oiyokan 設定情報読み込み失敗", 500, Locale.ENGLISH);
        }
    }

    public static OiyokanSettingsDatabase getOiyokanInternalDatabase(OiyokanSettings settingsOiyokan)
            throws ODataApplicationException {
        for (OiyokanSettingsDatabase look : settingsOiyokan.getDatabaseList()) {
            if ("oiyokan-internal".equals(look.getName())) {
                return look;
            }
        }
        throw new ODataApplicationException("UNEXPECTED: Database settings [oiyokan-internal] が見つかりません", 500,
                Locale.ENGLISH);
    }
}
