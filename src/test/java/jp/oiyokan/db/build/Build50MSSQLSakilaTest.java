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

import jp.oiyokan.basic.BasicJdbcUtil;
import jp.oiyokan.data.OiyokanResourceSqlUtil;
import jp.oiyokan.dto.OiyokanSettingsDatabase;
import jp.oiyokan.settings.OiyokanSettingsUtil;

/**
 * 内部データベース用のCSDL用内部テーブルのDDLをコマンドライン生成.
 */
class Build50MSSQLSakilaTest {
    // @Test
    void test01() throws Exception {
        OiyokanSettingsDatabase settingsDatabase = OiyokanSettingsUtil.getOiyokanDatabase("mssql1");

        try (Connection connTargetDb = BasicJdbcUtil.getConnection(settingsDatabase)) {
            String[] sqls = OiyokanResourceSqlUtil.loadOiyokanResourceSql("oiyokan/sql/" + "oiyokan-test-db-MSSQL.sql");
            for (String sql : sqls) {
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

    // @Test
    void test02() throws Exception {
        OiyokanSettingsDatabase settingsDatabase = OiyokanSettingsUtil.getOiyokanDatabase("mssql1");

        try (Connection connTargetDb = BasicJdbcUtil.getConnection(settingsDatabase)) {
            String[] sqls = OiyokanResourceSqlUtil
                    .loadOiyokanResourceSql("oiyokan/sql/" + "sample-sakila-db-MSSQL.sql");
            for (String sql : sqls) {
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
}
