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
package jp.oiyokan.db.simplejdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.basic.BasicJdbcUtil;
import jp.oiyokan.data.OiyokanInternalDatabase;
import jp.oiyokan.settings.OiyokanSettingsUtil;

/**
 * そもそも内部 h2 database への接続性を確認
 */
class H2SimpleClientTest {
    @Test
    void test01() throws Exception {
        try (Connection conn = BasicJdbcUtil
                .getConnection(OiyokanSettingsUtil.getOiyokanDatabase(OiyokanConstants.OIYOKAN_INTERNAL_DB))) {
            // 内部データベースのテーブルをセットアップ.
            OiyokanInternalDatabase.setupInternalDatabase();
        }

        try (Connection conn = BasicJdbcUtil
                .getConnection(OiyokanSettingsUtil.getOiyokanDatabase(OiyokanConstants.OIYOKAN_INTERNAL_TARGET_DB))) {

            try (var stmt = conn.prepareStatement(
                    "SELECT address_id FROM address WHERE ((address2 IS NULL) AND (address = ?)) LIMIT 2001")) {
                int column = 1;
                stmt.setString(column++, "47 MySakila Drive");
                stmt.executeQuery();
                var rset = stmt.getResultSet();
                if (rset.next()) {
                    assertEquals("1", rset.getString(1));
                }
            }
        }
    }
}
