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
package jp.oiyokan.util;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

/**
 * DateTimeに関するテスト。
 */
class OiyoDateTimeUtilTest {

    @Test
    void test01() {
        @SuppressWarnings("unused")
        ZonedDateTime zdt = OiyoDateTimeUtil.parseStringDateTime("2021-04-10T19:12:49.082Z");
        // System.err.println(zdt.toString());

        zdt = OiyoDateTimeUtil.parseStringDateTime("2021-04-10 19:12:49.082Z");
        // System.err.println(zdt.toString());

        // TODO FIXME 時間がずれる!?
        zdt = OiyoDateTimeUtil.parseStringDateTime("2021-04-10T19:12:49.082587");
        // System.err.println(zdt.toString());

        zdt = OiyoDateTimeUtil.parseStringDateTime("2021-04-10T19:12:49.082587Z");
        // System.err.println(zdt.toString());

        zdt = OiyoDateTimeUtil.parseStringDateTime("2021-04-10T19:12:49Z");
        // System.err.println(zdt.toString());

        zdt = OiyoDateTimeUtil.parseStringDateTime("2021-04-10");
        // System.err.println(zdt.toString());

    }

    @Test
    void test02() {
        @SuppressWarnings("unused")
        java.sql.Time time = OiyoDateTimeUtil.parseStringTime("21:53:00");
    }
}
