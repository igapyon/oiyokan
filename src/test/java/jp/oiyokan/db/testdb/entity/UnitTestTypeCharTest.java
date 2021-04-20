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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanTestSettingConstants;
import jp.oiyokan.common.OiyoUrlUtil;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * Entityアクセスのフル桁に着眼したテスト.
 */
class UnitTestTypeCharTest {
    @Test
    void test01() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final int TEST_ID = OiyokanTestUtil.getNextUniqueId();

        // 左右の文字が正しいことを確認
        ODataResponse resp = OiyokanTestUtil.callRequestPost("/ODataTests3", //
                "{\n" //
                        + "  \"ID\": " + TEST_ID + ",\n" //
                        + "  \"Name\": \"左右確認\",\n" //
                        + "  \"Description\": \"CHARの左右の挙動確認\",\n" //
                        + "  \"StringChar8\": \"  C456  \"\n" //
                        + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println("TRACE: " + result);
        assertEquals(201, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests3(" + TEST_ID + ")", null);
        assertEquals(200, resp.getStatusCode());
        result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);

        // DELETE
        resp = OiyokanTestUtil.callRequestDelete("/ODataTests3(" + TEST_ID + ")");
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests3(" + TEST_ID + ")", null);
        assertEquals(404, resp.getStatusCode());
    }

    @Test
    void test02() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final String decVal = "1304";

        // キーの文字列
        ODataResponse resp = OiyokanTestUtil.callRequestPost("/ODataTests2", //
                "{\n" //
                        + "  \"Decimal1\": " + decVal + ",\n" //
                        + "  \"StringChar8\": \"12345678\",\n" //
                        + "  \"Name\": \"CHARキー確認\",\n" //
                        + "  \"Description\": \"CHARキーの挙動確認\",\n" //
                        + "  \"StringVar255\": \"ABCXYZ\"\n" //
                        + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println("TRACE: " + result);
        assertEquals(201, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests2", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        String uri = "Decimal1=" + decVal + ",StringChar8='12345678',StringVar255='ABCXYZ'";
        // System.err.println("uri: " + uri);
        resp = OiyokanTestUtil.callRequestGetResponse( //
                "/ODataTests2(" + OiyoUrlUtil.encodeUrlQuery(uri) + ")", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callRequestDelete("/ODataTests2(" + OiyoUrlUtil.encodeUrlQuery(uri) + ")");
        result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse( //
                "/ODataTests2(" + OiyoUrlUtil.encodeUrlQuery(uri) + ")", null);
        assertEquals(404, resp.getStatusCode());
    }

    @Test
    void test03() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final String decVal = "1404";

        // キーの文字列
        ODataResponse resp = OiyokanTestUtil.callRequestPost("/ODataTests2", //
                "{\n" //
                        + "  \"Decimal1\": " + decVal + ",\n" //
                        + "  \"StringChar8\": \"  3456  \",\n" //
                        + "  \"Name\": \"CHARキー確認\",\n" //
                        + "  \"Description\": \"CHARキーの挙動確認\",\n" //
                        + "  \"StringVar255\": \"ABCXYZ\"\n" //
                        + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println("TRACE: " + result);
        assertEquals(201, resp.getStatusCode(), //
                "CHAR項目がINSERTできることの確認. MySQL ではここが失敗する。後方に半角スペースを付与が必要 (既知の問題)");

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests2", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(200, resp.getStatusCode());
        assertTrue(result.indexOf("  3456  ") >= 0, "CHAR型の後方FILLがおこなわれること.");

        final String uri = "Decimal1=" + decVal + ",StringChar8='  3456  ',StringVar255='ABCXYZ'";
        // System.err.println("uri: " + uri);
        resp = OiyokanTestUtil.callRequestGetResponse( //
                "/ODataTests2(" + OiyoUrlUtil.encodeUrlQuery(uri) + ")", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callRequestDelete("/ODataTests2(" + OiyoUrlUtil.encodeUrlQuery(uri) + ")");
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse( //
                "/ODataTests2(" + OiyoUrlUtil.encodeUrlQuery(uri) + ")", null);
        assertEquals(404, resp.getStatusCode());
    }

    @Test
    void test04() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final String decVal = "1404";

        // キーの文字列. 長さの足りない CHAR でも動作すること。
        ODataResponse resp = OiyokanTestUtil.callRequestPost("/ODataTests2", //
                "{\n" //
                        + "  \"Decimal1\": " + decVal + ",\n" //
                        + "  \"StringChar8\": \"  3456\",\n" //
                        + "  \"Name\": \"CHARキー確認\",\n" //
                        + "  \"Description\": \"CHARキーの挙動確認\",\n" //
                        + "  \"StringVar255\": \"ABCXYZ\"\n" //
                        + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println("TRACE: " + result);
        assertEquals(201, resp.getStatusCode(), //
                "CHAR項目がINSERTできることの確認. MySQL ではここが失敗する。後方に半角スペースを付与が必要 (既知の問題)");

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests2", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(200, resp.getStatusCode());
        assertTrue(result.indexOf("  3456  ") >= 0, "CHAR型の後方FILLがおこなわれること.");

        final String uri = "Decimal1=" + decVal + ",StringChar8='  3456  ',StringVar255='ABCXYZ'";
        // System.err.println("uri: " + uri);
        resp = OiyokanTestUtil.callRequestGetResponse( //
                "/ODataTests2(" + OiyoUrlUtil.encodeUrlQuery(uri) + ")", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callRequestDelete("/ODataTests2(" + OiyoUrlUtil.encodeUrlQuery(uri) + ")");
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse( //
                "/ODataTests2(" + OiyoUrlUtil.encodeUrlQuery(uri) + ")", null);
        assertEquals(404, resp.getStatusCode());
    }
}
