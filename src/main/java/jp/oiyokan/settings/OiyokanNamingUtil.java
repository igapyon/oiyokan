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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.dto.OiyokanNamingSettings;
import jp.oiyokan.dto.OiyokanNamingSettingsDb2Entity;

/**
 * DBとEntityとの間の命名を調整するユーティリティ.
 */
public class OiyokanNamingUtil {
    private static final boolean IS_DEBUG_NAMING_UTIL = false;

    private static volatile Map<String, OiyokanNamingSettingsDb2Entity> db2entityMap = null;
    private static volatile Map<String, OiyokanNamingSettingsDb2Entity> entity2dbMap = null;

    private static void ensureLoad() throws ODataApplicationException {
        if (db2entityMap != null && entity2dbMap != null) {
            // 読み込み済み。
            return;
        }

        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: resources: load: settings: oiyokan-naming-settings.json");

        // resources から読み込み。
        final ClassPathResource cpres = new ClassPathResource("oiyokan/oiyokan-naming-settings.json");
        try (InputStream inStream = cpres.getInputStream()) {
            String strOiyokanSettings = StreamUtils.copyToString(inStream, Charset.forName("UTF-8"));

            ObjectMapper mapper = new ObjectMapper();
            OiyokanNamingSettings namingSettings = mapper.readValue(strOiyokanSettings, OiyokanNamingSettings.class);

            Map<String, OiyokanNamingSettingsDb2Entity> workDb2entityMap = new HashMap<>();
            Map<String, OiyokanNamingSettingsDb2Entity> workEntity2dbMap = new HashMap<>();

            for (OiyokanNamingSettingsDb2Entity look : namingSettings.getDb2entity()) {
                workDb2entityMap.put(look.getDb(), look);
                workEntity2dbMap.put(look.getEntity(), look);
            }

            // 値をセット。
            db2entityMap = workDb2entityMap;
            entity2dbMap = workEntity2dbMap;
        } catch (IOException ex) {
            // [M023] UNEXPECTED: Fail to load Oiyokan naming settings
            System.err.println(OiyokanMessages.M023 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M023, 500, Locale.ENGLISH);
        }
    }

    /**
     * DB上の名称を Entity上の名称に変換.
     * 
     * @param nameOnDatabase DB上の名称.
     * @return Entity上の名称.
     */
    public static String db2Entity(String nameOnDatabase) throws ODataApplicationException {
        ensureLoad();

        if (db2entityMap.get(nameOnDatabase) != null) {
            // ヒット。登録済みのものを返却。
            if (IS_DEBUG_NAMING_UTIL)
                System.err.println("TRACE: db[" + nameOnDatabase + "], entity["
                        + db2entityMap.get(nameOnDatabase).getEntity() + "]");
            return db2entityMap.get(nameOnDatabase).getEntity();
        } else {
            // 登録無し。そのまま返却。
            return nameOnDatabase;
        }
    }

    /**
     * Entity上の名称を DB上の名称に変換.
     * 
     * @param nameOnEntity Entity上の名称.
     * @return DB上の名称.
     */
    public static String entity2Db(String nameOnEntity) throws ODataApplicationException {
        ensureLoad();

        if (entity2dbMap.get(nameOnEntity) != null) {
            // ヒット。登録済みのものを返却。
            if (IS_DEBUG_NAMING_UTIL)
                System.err.println(
                        "TRACE: entity[" + nameOnEntity + "], db[" + entity2dbMap.get(nameOnEntity).getDb() + "]");
            return entity2dbMap.get(nameOnEntity).getDb();
        } else {
            // 登録無し。そのまま返却。
            return nameOnEntity;
        }
    }
}
