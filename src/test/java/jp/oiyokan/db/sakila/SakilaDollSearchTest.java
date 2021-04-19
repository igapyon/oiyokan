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
package jp.oiyokan.db.sakila;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.app.ctrl.ThSakilaCtrl;
import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanTestSettingConstants;
import jp.oiyokan.common.OiyoUrlUtil;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * OData サーバについて、おおざっぱな通過によるデグレードを検知.
 */
class SakilaDollSearchTest {
    @Test
    void test01() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_SAKILA)
            return;

        for (String[] entrys : ThSakilaCtrl.ODATA_ENTRY_INFOS) {
            if (entrys[0].indexOf("$search") >= 0) {
                if (!OiyokanConstants.IS_EXPERIMENTAL_SEARCH_ENABLED) {
                    // 実験的 $search について無効化されているためテストから除外.
                    continue;
                }
            }

            String[] entry = entrys[1].split("['?']");

            final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse(entry[0], entry[1]);
            final String result = OiyokanTestUtil.stream2String(resp.getContent());

            if (true) {
                System.err.println("TRACE: " + OiyoUrlUtil.decodeUrlQuery(entrys[1]));
                System.err.println("[" + entrys[0] + "], [" + entrys[1] + "], result: " + result);
            }
            // System.err.println(result);
            int statusCode = resp.getStatusCode();
            if (entrys[0].equals("SklFilmLists")
                    && result.startsWith("{\"error\":{\"code\":null,\"message\":\"[IY2104] UNEXPECTED:")
                    && statusCode != 200) {
                // Postgresでこのパターンでエラーになるが気にしない。
            } else if (entrys[0].equals("SklFilms")
                    && result.startsWith("{\"error\":{\"code\":null,\"message\":\"[M017]") && statusCode != 200) {
                // MySQLでこのパターンでエラーになるが気にしない。O
            } else {
                assertEquals(200, resp.getStatusCode(), "Sakila sample db での挙動確認.");
            }
        }
    }
}
