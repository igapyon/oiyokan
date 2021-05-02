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
package jp.oiyokan.db.testdb.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanUnittestUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * フィルタの型に着眼したテスト.
 */
class UnitTestQuery01Test {
    @Test
    void testTimestamp() throws Exception {
        @SuppressWarnings("unused")
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        final ODataResponse resp = OiyokanTestUtil.callGet("/ODataTest1",
                "$select=ID &$filter=DateTimeOffset1 lt 2000-12-31T21:53:00Z &$orderby=ID &$top=51 &$count=true");
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        // TODO FIXME 結果が存在するテストパターンが欲しい。
        assertEquals("{\"@odata.context\":\"$metadata#ODataTest1\",\"@odata.count\":0,\"value\":[]}", result);
        assertEquals(200, resp.getStatusCode());
    }
}
