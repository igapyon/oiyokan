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

import jp.oiyokan.OiyokanTestSettingConstants;
import jp.oiyokan.common.OiyoUrlUtil;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * フィルタの型に着眼したテスト.
 */
class UnitTestQueryNotTest {
    @Test
    void testNotA() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1", OiyoUrlUtil
                .encodeUrlQuery("&$filter=not contains(StringVar255,'VARCHAR255') &$count=true &$select=ID"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":1,\"value\":[{\"ID\":204}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }
}
