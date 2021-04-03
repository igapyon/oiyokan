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
package jp.oiyokan.ocsdl.gen;

import java.sql.Connection;

import jp.oiyokan.basic.BasicJdbcUtil;
import jp.oiyokan.data.OiyokanInternalDatabase;
import jp.oiyokan.dto.OiyokanSettingsDatabase;
import jp.oiyokan.settings.OiyokanSettingsUtil;

/**
 * 内部データベース用のCSDL用内部テーブルのDDLをコマンドライン生成.
 */
class GenOcsdlMSSQLSakilaTest {
    /**
     * SQL Server 接続環境が適切に存在する場合にのみ実行可能。
     * 
     * Ocsdlテーブルのスキーマを取得したい場合にのみ JUnit を実行する。
     */
    // @Test
    void test01() throws Exception {
        OiyokanSettingsDatabase settingsDatabase = OiyokanSettingsUtil.getOiyokanDatabase("mssql1");

        try (Connection connTargetDb = BasicJdbcUtil.getConnection(settingsDatabase)) {
            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "actor"));

        }
    }
}
