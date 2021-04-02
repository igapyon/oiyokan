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

import jp.oiyokan.basic.BasicJdbcUtil;
import jp.oiyokan.dto.OiyokanSettingsDatabase;
import jp.oiyokan.settings.OiyokanSettingsUtil;

/**
 * 内部データベース用のCSDL用内部テーブルのDDLをコマンドライン生成.
 */
class SampleGen11OcsdlPgTest {
    /**
     * postgres 接続環境が適切に存在する場合にのみ実行可能。
     * 
     * Ocsdlテーブルのスキーマを取得したい場合にのみ JUnit を実行する。
     */
    // @Test
    void test01() throws Exception {
        OiyokanSettingsDatabase settingsDatabase = OiyokanSettingsUtil.getOiyokanDatabase("postgres1");

        try (Connection connTargetDb = BasicJdbcUtil.getConnection(settingsDatabase)) {
            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "actor"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "actor_info"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "address"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "category"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "city"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "country"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "customer"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "customer_list"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "film"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "film_actor"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "film_category"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "film_list"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "inventory"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "language"));

            System.err.println(
                    OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "nicer_but_slower_film_list"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "payment"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "rental"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "sales_by_film_category"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "sales_by_store"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "staff"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "staff_list"));

            System.err.println(OiyokanInternalDatabase.generateCreateOcsdlDdl(connTargetDb, "store"));
        }
    }
}
