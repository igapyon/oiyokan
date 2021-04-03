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

import jp.oiyokan.util.OiyokanTestUtil;

/**
 * 実際に発生した事象のテストケース.
 */
class TestDbActualFailCaseTest {
    @Test
    void test01() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/SklFilmActors",
                OiyokanTestUtil.encodeUrlQuery(
                        "$top=2001 &$filter=actor_id eq 1 and film_id eq 140 &$count=true &$select=actor_id,film_id,last_update"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals(
                "{\"@odata.context\":\"$metadata#SklFilmActors\",\"@odata.count\":1,\"value\":[{\"actor_id\":1,\"film_id\":140,\"last_update\":\"2006-02-15T01:05:03Z\"}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }
}