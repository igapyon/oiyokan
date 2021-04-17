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

import jp.oiyokan.basic.OiyoBasicJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.data.OiyokanKanDatabase;
import jp.oiyokan.dto.OiyoSettingsDatabase;

/**
 * 内部データベース用のCSDL用内部テーブルのDDLをコマンドライン生成.
 */
class ZzzGenTableMSSQLSakilaTest {
    /**
     * SQL Server 接続環境が適切に存在する場合にのみ実行可能。
     * 
     * Oiyoテーブルのスキーマを取得したい場合にのみ JUnit を実行する。
     */
    // @Test
    void test01() throws Exception {
        final OiyoInfo oiyoInfo = new OiyoInfo();
        oiyoInfo.setSettings(OiyoInfoUtil.loadOiyokanSettings());

        OiyoSettingsDatabase settingsDatabase = OiyoInfoUtil.getOiyoDatabaseByName(oiyoInfo, "mssql1");

        try (Connection connTargetDb = OiyoBasicJdbcUtil.getConnection(settingsDatabase)) {
            System.err.println(OiyokanKanDatabase.generateCreateOiyoDdl(connTargetDb, "actor"));

        }
    }
}
