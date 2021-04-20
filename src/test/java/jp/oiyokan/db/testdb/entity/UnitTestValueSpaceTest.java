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
package jp.oiyokan.db.testdb.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanTestSettingConstants;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * フィルタの型に着眼したテスト.
 */
class UnitTestValueSpaceTest {
    @Test
    void test01() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final int TEST_ID = OiyokanTestUtil.getNextUniqueId();

        // INSERT + DELETE
        ODataResponse resp = OiyokanTestUtil.callRequestPost("/ODataTests4", "{\n" //
                + "  \"I_D\":" + TEST_ID + ",\n" //
                + "  \"Na_me\":\"Name\"\n" //
                + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(201, resp.getStatusCode(), "ORCL18でエラー(既知の問題)");
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests4\",\"I_D\":" + TEST_ID
                + ",\"Na_me\":\"Name\",\"Va_lue1\":\"VALUEVALUE12345\"}", result);

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests4(I_D=" + TEST_ID + ",Na_me='Name')", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        // UPDATE (PATCH)
        resp = OiyokanTestUtil.callRequestPatch("/ODataTests4(I_D=" + TEST_ID + ",Na_me='Name')", "{\n" //
                + "  \"Na_me\":\"Name2\",\n" //
                + "  \"Va_lue1\":\"Description2\"\n" + "}", false, false);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests4(I_D=" + TEST_ID + ",Na_me='Name2')", null);
        assertEquals(200, resp.getStatusCode());

        /// 通常のfilter
        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests4", "$filter=I_D eq " + TEST_ID);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        // Entity
        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests4(I_D=" + TEST_ID + ",Na_me='Name2')", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callRequestDelete("/ODataTests4(I_D=" + TEST_ID + ",Na_me='Name2')");
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests4(I_D=" + TEST_ID + ",Na_me='Name2')", null);
        assertEquals(404, resp.getStatusCode());
    }
}
