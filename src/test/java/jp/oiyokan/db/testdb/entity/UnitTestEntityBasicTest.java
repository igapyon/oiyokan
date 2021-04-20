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
 * Entityの基本的なテスト.
 */
class UnitTestEntityBasicTest {
    /**
     * CREATE + DELETE
     */
    @Test
    void test01() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final int TEST_ID = OiyokanTestUtil.getNextUniqueId();

        // INSERT + DELETE
        ODataResponse resp = OiyokanTestUtil.callRequestPost("/ODataTests3", "{\n" //
                + "  \"ID\":" + TEST_ID + ",\n" //
                + "  \"Name\":\"Name\",\n" //
                + "  \"Description\":\"Description\"\n" + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println("TRACE: " + result);
        assertEquals(201, resp.getStatusCode(), "SQLSV2008でエラー。(既知の問題). varbinaryとtextと混同.");

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests3(" + TEST_ID + ")", null);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callRequestDelete("/ODataTests3(" + TEST_ID + ")");
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests3(" + TEST_ID + ")", null);
        assertEquals(404, resp.getStatusCode());
    }

    /**
     * CREATE + UPDATE + DELETE
     */
    @Test
    void test02() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final int TEST_ID = OiyokanTestUtil.getNextUniqueId();

        // INSERT + DELETE
        ODataResponse resp = OiyokanTestUtil.callRequestPost("/ODataTests3", "{\n" //
                + "  \"ID\":" + TEST_ID + ",\n" //
                + "  \"Name\":\"Name\",\n" //
                + "  \"Description\":\"Description\"\n" + "}");
        @SuppressWarnings("unused")
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(201, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests3(" + TEST_ID + ")", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        // UPDATE (PATCH)
        resp = OiyokanTestUtil.callRequestPatch("/ODataTests3(" + TEST_ID + ")", "{\n" //
                + "  \"Name\":\"Name2\",\n" //
                + "  \"Description\":\"Description2\"\n" + "}", false, false);
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests3(" + TEST_ID + ")", null);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callRequestDelete("/ODataTests3(" + TEST_ID + ")");
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests3(" + TEST_ID + ")", null);
        assertEquals(404, resp.getStatusCode());
    }

    /**
     * NOT FOUND
     */
    @Test
    void test03() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final int uniqueId = OiyokanTestUtil.getNextUniqueId();

        ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests3(" + uniqueId + ")", null);
        assertEquals(404, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestPatch("/ODataTests3(" + uniqueId + ")", "{\n" //
                + "  \"Name\":\"Name2\",\n" //
                + "  \"Description\":\"Description2\"\n" + "}", true, false);
        assertEquals(404, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestDelete("/ODataTests3(" + uniqueId + ")");
        assertEquals(404, resp.getStatusCode(), "追加・更新していないのでヒットもしない.");
    }
}
