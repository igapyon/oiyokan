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
import jp.oiyokan.dto.Oiyo13SettingsDatabase;
import jp.oiyokan.settings.OiyoSettingsUtil;

/**
 * テスト用の内部データベースを作成します。この内部データベースは動作の上で必要です。
 */
class Build34ORACLETest {
    private static final String[] DROP_TABLE_SQLS = new String[] { //
            "DROP TABLE ODataTest1", //
            "DROP TABLE ODataTest2", //
            "DROP TABLE ODataTest3", //
            "DROP TABLE \"OData Test4\"", //
            "DROP TABLE ODataTest5", //
            "DROP TABLE ODataTest6", //
            "DROP TABLE ODataTest7", //
    };

    @Test
    void test01() throws Exception {
        if (true)
            return;

        Oiyo13SettingsDatabase settingsDatabase = OiyoSettingsUtil.getOiyokanDatabase("oracle1");

        try (Connection connTargetDb = OiyoBasicJdbcUtil.getConnection(settingsDatabase)) {
            for (String sql : DROP_TABLE_SQLS) {
                try (var stmt = connTargetDb.prepareStatement(sql)) {
                    stmt.executeUpdate();
                } catch (SQLException ex) {
                    System.err.println(ex.toString());
                }
            }

            try (var stmt = connTargetDb.prepareStatement("DROP TABLE ODataTest1")) {
                stmt.executeUpdate();
            } catch (SQLException ex) {
                System.err.println(ex.toString());
            }

            String[] sqls = OiyokanResourceSqlUtil
                    .loadOiyokanResourceSql("oiyokan/sql/" + "oiyokan-test-db-ORACLE.sql");
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
    // Sakila DB については 見実装。
}
