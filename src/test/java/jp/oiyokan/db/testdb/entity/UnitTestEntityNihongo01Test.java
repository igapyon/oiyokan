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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanUnittestUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoUrlUtil;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * Entityの日本語テスト.
 */
class UnitTestEntityNihongo01Test {
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(UnitTestEntityNihongo01Test.class);

    /**
     * CREATE + UPDATE + DELETE
     */
    @Test
    void test02() throws Exception {
        @SuppressWarnings("unused")
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        // INSERT
        ODataResponse resp = OiyokanTestUtil.callPost("/ODataTest3", "{\n" //
                + "  \"Name\":\"名前\"\n" //
                + "  , \"Description\":\"説明\"\n" //
                + "  , \"StringVar255\":\"可変朝文字列\"\n" //
                + "  , \"Clob1\":\"文字列LOB\"\n" //
                + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        final String idString = OiyokanTestUtil.getValueFromResultByKey(result, "ID");
        assertEquals(201, resp.getStatusCode());

        // SELECT
        resp = OiyokanTestUtil.callGet("/ODataTest3",
                OiyoUrlUtil.encodeUrlQuery("$select=Name,Description,StringVar255,Clob1 &$filter=ID eq " + idString));
        result = OiyokanTestUtil.stream2String(resp.getContent());
        assertEquals(
                "{\"@odata.context\":\"$metadata#ODataTest3\",\"value\":[{\"@odata.id\":\"http://localhost:8080/odata4.svc/ODataTest3("
                        + idString + ")\",\"ID\":" + idString
                        + ",\"Name\":\"名前\",\"Description\":\"説明\",\"StringVar255\":\"可変朝文字列\",\"Clob1\":\"文字列LOB\"}]}",
                result);
        assertEquals(200, resp.getStatusCode());

        // UPDATE (PATCH)
        resp = OiyokanTestUtil.callPatch("/ODataTest3(" + idString + ")", "{\n" //
                + "  \"Name\":\"更新後名前\"\n" //
                + "  , \"Description\":\"更新後説明\"\n" //
                + "  , \"StringVar255\":\"更新後可変朝文字列\"\n" //
                + "  , \"Clob1\":\"更新後文字列LOB\"\n" //
                + "}", //
                false, false);
        assertEquals(200, resp.getStatusCode());

        // SELECT
        resp = OiyokanTestUtil.callGet("/ODataTest3",
                OiyoUrlUtil.encodeUrlQuery("$select=Name,Description,StringVar255,Clob1 &$filter=ID eq " + idString));
        result = OiyokanTestUtil.stream2String(resp.getContent());
        assertEquals(
                "{\"@odata.context\":\"$metadata#ODataTest3\",\"value\":[{\"@odata.id\":\"http://localhost:8080/odata4.svc/ODataTest3("
                        + idString + ")\",\"ID\":" + idString
                        + ",\"Name\":\"更新後名前\",\"Description\":\"更新後説明\",\"StringVar255\":\"更新後可変朝文字列\",\"Clob1\":\"更新後文字列LOB\"}]}",
                result);

        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callDelete("/ODataTest3(" + idString + ")");
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callGet("/ODataTest3(" + idString + ")", null);
        assertEquals(404, resp.getStatusCode());
    }
}
