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
package jp.oiyokan.db.build;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import jp.oiyokan.basic.OiyoBasicJdbcUtil;
import jp.oiyokan.data.OiyokanResourceSqlUtil;
import jp.oiyokan.dto.OiyokanSettingsDatabase;
import jp.oiyokan.settings.OiyokanSettingsUtil;

/**
 * テスト用の Postgres Test データベースを作成します。
 * 
 * Postgres での単体テスト実施時に利用します。
 */
class Build31PostgresTest {
    @Test
    void test01() throws Exception {
        if (true)
            return;

        OiyokanSettingsDatabase settingsDatabase = OiyokanSettingsUtil.getOiyokanDatabase("postgres1");

        try (Connection connTargetDb = OiyoBasicJdbcUtil.getConnection(settingsDatabase)) {
            String[] sqls = OiyokanResourceSqlUtil
                    .loadOiyokanResourceSql("oiyokan/sql/" + "oiyokan-test-db-postgres.sql");
            for (String sql : sqls) {
                if (sql.trim().length() == 0) {
                    continue;
                }
                try (var stmt = connTargetDb.prepareStatement(sql)) {
                    System.err.println(sql);
                    stmt.executeUpdate();
                } catch (SQLException ex) {
                    System.err.println(ex.toString());
                    throw ex;
                }
            }
        }
    }

    ////////////////////////////////////////////////////
    // Sakila DB については Postgres のものを利用。
}
