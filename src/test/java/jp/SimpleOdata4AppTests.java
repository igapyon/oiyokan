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
package jp;

import org.junit.jupiter.api.Test;

// 【現状 Spring Boot のテストはない. ビルド時間短縮のためコメントアウト】 @SpringBootTest
class SimpleOdata4AppTests {
    @Test
    void contextLoads() {

        if (false) {
            final int ZOUSYOKU = 100;

            int idCounter = 4;

            for (int index = 0; index < ZOUSYOKU; index++) {
                System.err.println("INSERT INTO MyProducts (ID, Name, Description) VALUES (");
                System.err.println(
                        "  " + idCounter++ + ", 'PopTablet" + index + "', '増殖タブレット Laptop Intel Core" + index + "');");
            }
        }
    }
}
