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

import jp.oiyokan.OiyokanUnittestUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * Entityアクセスのフル桁に着眼したテスト.
 */
class UnitTestTypeChar01Test {
    @Test
    void test01() throws Exception {
        @SuppressWarnings("unused")
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        // 左右の文字が正しいことを確認
        ODataResponse resp = OiyokanTestUtil.callPost("/ODataTest3", //
                "{\n" //
                        + "  \"Name\": \"左右確認\",\n" //
                        + "  \"Description\": \"CHARの左右の挙動確認\",\n" //
                        + "  \"StringChar8\": \"  C456  \"\n" //
                        + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        final String idString = OiyokanTestUtil.getValueFromResultByKey(result, "ID");
        // System.err.println("TRACE: " + result);
        assertEquals(201, resp.getStatusCode());

        resp = OiyokanTestUtil.callGet("/ODataTest3(" + idString + ")", null);
        assertEquals(200, resp.getStatusCode());
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);

        /// 通常のfilter
        resp = OiyokanTestUtil.callGet("/ODataTest3", "$filter=ID eq " + idString + "&$select=StringChar8");
        result = OiyokanTestUtil.stream2String(resp.getContent());
        assertEquals("{\"@odata.context\":\"$metadata#ODataTest3\",\"value\":[{\"ID\":" + idString
                + ",\"StringChar8\":\"  C456  \"}]}", result, "前後空白付きでFILTER検索できることを確認.");
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callDelete("/ODataTest3(" + idString + ")");
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callGet("/ODataTest3(" + idString + ")", null);
        assertEquals(404, resp.getStatusCode());
    }
}
