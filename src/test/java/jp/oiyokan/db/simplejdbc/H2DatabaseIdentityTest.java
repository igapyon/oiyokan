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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanTestSettingConstants;

/**
 * ごく基本的で大雑把な JDBC + h2 database 挙動の確認.
 */
class H2DatabaseIdentityTest {
    @Test
    void test01() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:identest")) {
            try (var stmt = conn
                    .prepareStatement("CREATE TABLE IF NOT EXISTS tab1 (id INT auto_increment, val1 TEXT)")) {
                stmt.executeUpdate();
            }
            try (var stmt = conn.prepareStatement("INSERT INTO tab1 (val1) VALUES ('Hello world.')",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                assertEquals(true, rs.next(), "getGeneratedKeys が機能することを確認.");
            }
        }
    }
}
