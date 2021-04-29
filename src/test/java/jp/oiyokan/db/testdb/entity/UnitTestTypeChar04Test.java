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

import jp.oiyokan.OiyokanUnittestUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoUrlUtil;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * 後方空白がTRIMされた状態のCHARでの挙動確認。
 */
class UnitTestTypeChar04Test {
    @Test
    void test04() throws Exception {
        @SuppressWarnings("unused")
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        final String decVal = "1404";

        // キーの文字列. 長さの足りない CHAR でも動作すること。
        ODataResponse resp = OiyokanTestUtil.callPost("/ODataTest2", //
                "{\n" //
                        + "  \"Decimal1\": " + decVal + ",\n" //
                        + "  \"StringChar8\": \"  3456\",\n" //
                        + "  \"Name\": \"CHARキー確認\",\n" //
                        + "  \"Description\": \"CHARキーの挙動確認\",\n" //
                        + "  \"StringVar255\": \"ABCXYZ\"\n" //
                        + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println("TRACE: " + result);
        assertEquals(201, resp.getStatusCode(), //
                "CHAR項目がINSERTできることの確認. MySQL ではここが失敗する。後方に半角スペースを付与が必要 (既知の問題)");

        resp = OiyokanTestUtil.callGet("/ODataTest2", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(200, resp.getStatusCode());
        assertTrue(result.indexOf("  3456  ") >= 0, "CHAR型の後方FILLがおこなわれること.");

        final String uri = "Decimal1=" + decVal + ",StringChar8='  3456',StringVar255='ABCXYZ'";
        // System.err.println("uri: " + uri);
        resp = OiyokanTestUtil.callGet( //
                "/ODataTest2(" + OiyoUrlUtil.encodeUrlQuery(uri) + ")", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callDelete("/ODataTest2(" + OiyoUrlUtil.encodeUrlQuery(uri) + ")");
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callGet( //
                "/ODataTest2(" + OiyoUrlUtil.encodeUrlQuery(uri) + ")", null);
        assertEquals(404, resp.getStatusCode());
    }
}
