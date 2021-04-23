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
 * ごく基本的な OData の挙動を確認.
 */
class UnitTestQueryOrderby02Test {
    @Test
    void testSimpleAllWithoutSelect() throws Exception {
        @SuppressWarnings("unused")
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1", "$orderby=ID&$top=2");
        @SuppressWarnings("unused")
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // コンテンツ内容は確認なし.
        // System.err.println(result);
        assertEquals(200, resp.getStatusCode());
    }
}
