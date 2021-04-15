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
package jp.oiyokan.oiyo.gen;

import java.sql.Connection;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanTestSettingConstants;
import jp.oiyokan.basic.OiyoBasicJdbcUtil;
import jp.oiyokan.data.OiyokanKanDatabase;
import jp.oiyokan.dto.Oiyo13SettingsDatabase;
import jp.oiyokan.settings.OiyoSettingsUtil;

/**
 * 内部データベース用のCSDL用内部テーブルのDDLをコマンドライン生成.
 */
class GenOiyoODataTestFulls1Test {
    /**
     * Oiyoテーブルのスキーマを取得したい場合にのみ JUnit を実行する。
     */
    // @Test
    void test01() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        Oiyo13SettingsDatabase settingsDatabase = OiyoSettingsUtil
                .getOiyokanDatabase(OiyokanConstants.OIYOKAN_UNITTEST_DB);

        try (Connection connTargetDb = OiyoBasicJdbcUtil.getConnection(settingsDatabase)) {
            // 内部データベースのテーブルをセットアップ.
            OiyokanKanDatabase.setupKanDatabase();

            System.err.println(OiyokanKanDatabase.generateCreateOiyoDdl(connTargetDb, "ODataTestFulls1"));

        }
    }
}
