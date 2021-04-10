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
package jp.oiyokan.db.testdb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanTestConstants;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * フィルタの型に着眼したテスト.
 */
class TestODataDmlTestDbTest {
    /**
     * テストデータが利用する ID 範囲。
     */
    private static final int TEST_ID = 10022;

    /**
     * CREATE + DELETE
     */
    @Test
    void test01() throws Exception {
        if (!OiyokanTestConstants.IS_TEST_ODATATEST)
            return;

        // INSERT + DELETE
        ODataResponse resp = OiyokanTestUtil.callRequestPost("/ODataTests3", "{\n" //
                + "  \"ID\":" + TEST_ID + ",\n" //
                + "  \"Name\":\"Name\",\n" //
                + "  \"Description\":\"Description\"\n" + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(201, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callRequestDelete("/ODataTests3(" + TEST_ID + ")");
        assertEquals(204, resp.getStatusCode());
    }

    /**
     * CREATE + UPDATE + DELETE
     */
    @Test
    void test02() throws Exception {
        if (!OiyokanTestConstants.IS_TEST_ODATATEST)
            return;

        // INSERT + DELETE
        ODataResponse resp = OiyokanTestUtil.callRequestPost("/ODataTests3", "{\n" //
                + "  \"ID\":" + TEST_ID + ",\n" //
                + "  \"Name\":\"Name\",\n" //
                + "  \"Description\":\"Description\"\n" + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(201, resp.getStatusCode());

        // UPDATE (PATCH)
        resp = OiyokanTestUtil.callRequestPatch("/ODataTests3(" + TEST_ID + ")", "{\n" //
                + "  \"Name\":\"Name2\",\n" //
                + "  \"Description\":\"Description2\"\n" + "}");
        assertEquals(204, resp.getStatusCode());

        // UPDATE (PUT)
        resp = OiyokanTestUtil.callRequestPut("/ODataTests3(" + TEST_ID + ")", "{\n" //
                + "  \"Name\":\"Name2\",\n" //
                + "  \"Description\":\"Description2\"\n" + "}");
        assertEquals(204, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callRequestDelete("/ODataTests3(" + TEST_ID + ")");
        assertEquals(204, resp.getStatusCode());
    }
}
