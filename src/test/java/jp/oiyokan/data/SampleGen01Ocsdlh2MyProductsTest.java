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
package jp.oiyokan.data;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanSettingsUtil;
import jp.oiyokan.basic.BasicDbUtil;
import jp.oiyokan.dto.OiyokanSettings;
import jp.oiyokan.dto.OiyokanSettingsDatabase;

/**
 * 内部データベース用のCSDL用内部テーブルのDDLを生成: h2 版.
 */
class SampleGen01Ocsdlh2MyProductsTest {
    /**
     * postgres 接続環境が適切に存在する場合にのみ JUnit を実行。
     */
    // @Test
    void test01() throws Exception {
        final OiyokanSettings settingsOiyokan = OiyokanSettingsUtil.loadOiyokanSettings();
        OiyokanSettingsDatabase settingsDatabase = null;
        for (OiyokanSettingsDatabase look : settingsOiyokan.getDatabaseList()) {
            if ("oiyokan-internal".equals(look.getName())) {
                settingsDatabase = look;
            }
        }

        try (Connection connTargetDb = BasicDbUtil.getConnection(settingsDatabase)) {
            // 内部データベースのテーブルをセットアップ.
            OiyokanInterDb.setupTable(connTargetDb);

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "MyProducts"));

        }
    }
}
