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
 * 内部データベース用のCSDL用内部テーブルのDDLを生成: postgres 版.
 */
class SampleGen11OcsdlPgTest {
    /**
     * postgres 接続環境が適切に存在する場合にのみ JUnit を実行。
     */
    // @Test
    void test01() throws Exception {
        final OiyokanSettings settingsOiyokan = OiyokanSettingsUtil.loadOiyokanSettings();
        OiyokanSettingsDatabase settingsDatabase = null;
        for (OiyokanSettingsDatabase look : settingsOiyokan.getDatabaseList()) {
            if ("postgres1".equals(look.getName())) {
                settingsDatabase = look;
            }
        }

        try (Connection connTargetDb = BasicDbUtil.getConnection(settingsDatabase)) {
            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "actor"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "actor_info"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "address"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "category"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "city"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "country"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "customer"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "customer_list"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "film"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "film_actor"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "film_category"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "film_list"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "inventory"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "language"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "nicer_but_slower_film_list"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "payment"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "rental"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "sales_by_film_category"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "sales_by_store"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "staff"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "staff_list"));

            System.err.println(OiyokanInterDb.generateCreateOcsdlDdl(connTargetDb, "store"));
        }
    }
}
