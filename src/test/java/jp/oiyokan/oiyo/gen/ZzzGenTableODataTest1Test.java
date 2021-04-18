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

import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanTestSettingConstants;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.data.OiyokanKanDatabase;
import jp.oiyokan.dto.OiyoSettingsDatabase;

/**
 * 内部データベース用のCSDL用内部テーブルのDDLをコマンドライン生成.
 */
class ZzzGenTableODataTest1Test {
    /**
     * ODataTest1 のための Oiyo DDL を生成.
     * 
     * カバレッジ目的で、以下のテストは常に動作させる。
     */
    @Test
    void test01() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final OiyoInfo oiyoInfo = new OiyoInfo();
        oiyoInfo.setSettings(OiyoInfoUtil.loadOiyokanSettings());

        OiyoSettingsDatabase settingsDatabase = OiyoInfoUtil.getOiyoDatabaseByName(oiyoInfo,
                OiyokanConstants.OIYOKAN_UNITTEST_DB);

        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(settingsDatabase)) {
            // 内部データベースのテーブルをセットアップ.
            OiyokanKanDatabase.setupKanDatabase(oiyoInfo);

            System.err.println(OiyokanKanDatabase.generateCreateOiyoDdl(connTargetDb, "ODataTest1"));

        }
    }
}
