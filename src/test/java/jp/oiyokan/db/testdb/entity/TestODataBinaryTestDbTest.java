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

import jp.oiyokan.OiyokanTestConstants;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * Binary 型に着眼したテスト.
 */
class TestODataBinaryTestDbTest {
    /**
     * テストデータが利用する ID 範囲。
     */
    private static final int TEST_ID = 10389;

    @Test
    void test01() throws Exception {
        if (!OiyokanTestConstants.IS_TEST_ODATATEST)
            return;

        ODataResponse resp = OiyokanTestUtil.callRequestPost("/ODataTests6", "{\n" //
                + "  \"ID\":" + TEST_ID + "\n" //
                + "  , \"Binary1\":\"VG9uYXJpIG5vIGt5YWt1Lg==\"\n" //
                + "  , \"VarBinary1\":\"VG9uYXJpIG5vIGt5YWt1Lg==\"\n" //
                + "  , \"LongVarBinary1\":\"VG9uYXJpIG5vIGt5YWt1Lg==\"\n" //
                + "  , \"Blob1\":\"VG9uYXJpIG5vIGt5YWt1Lg==\"\n" //
                + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests6\",\"ID\":" + TEST_ID
                + ",\"Name\":\"Binary UnitTest\",\"Description\":\"Binary UnitTest table.\"" //
                + ",\"Binary1\":\"VG9uYXJpIG5vIGt5YWt1Lg==\",\"VarBinary1\":\"VG9uYXJpIG5vIGt5YWt1Lg==\",\"LongVarBinary1\":\"VG9uYXJpIG5vIGt5YWt1Lg==\",\"Blob1\":\"VG9uYXJpIG5vIGt5YWt1Lg==\"}",
                result, "INSERTできることを確認.");
        assertEquals(201, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests6(" + TEST_ID + ")", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(200, resp.getStatusCode(), "INSERTしたレコードが格納されていることを確認.");

        // UPDATE (PATCH)
        resp = OiyokanTestUtil.callRequestPatch("/ODataTests6(" + TEST_ID + ")", "{\n" //
                + "  \"Binary1\":\"SG91cnl1amku\"\n" //
                + "}");
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(204, resp.getStatusCode(), "UPDATE(PATCH)できることを確認.");

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests6(" + TEST_ID + ")", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests6\",\"ID\":" + TEST_ID
                + ",\"Name\":\"Binary UnitTest\",\"Description\":\"Binary UnitTest table.\"" //
                + ",\"Binary1\":\"SG91cnl1amku\",\"VarBinary1\":\"VG9uYXJpIG5vIGt5YWt1Lg==\",\"LongVarBinary1\":\"VG9uYXJpIG5vIGt5YWt1Lg==\",\"Blob1\":\"VG9uYXJpIG5vIGt5YWt1Lg==\"}",
                result, "UPDATE(PATCH)後の値を確認.");
        assertEquals(200, resp.getStatusCode());

        // UPDATE (PUT)
        resp = OiyokanTestUtil.callRequestPut("/ODataTests6(" + TEST_ID + ")", "{\n" //
                + "  \"Binary1\":\"S2lua2FrdWppLg==\"\n" //
                + "}");
        result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println(result);
        assertEquals(204, resp.getStatusCode(), "UPDATE(PUT)できることを確認.");

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests6(" + TEST_ID + ")", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests6\",\"ID\":" + TEST_ID
                + ",\"Name\":null,\"Description\":null" //
                + ",\"Binary1\":\"S2lua2FrdWppLg==\",\"VarBinary1\":\"\",\"LongVarBinary1\":\"\",\"Blob1\":\"\"}",
                result, "UPDATE(PUT)後の値を確認.");
        assertEquals(200, resp.getStatusCode());

        /// 通常のfilter
        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests6", "$filter=ID eq " + TEST_ID);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println("TRACE: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests6\",\"value\":[{\"ID\":" + TEST_ID
                + ",\"Name\":null,\"Description\":null,\"Binary1\":\"S2lua2FrdWppLg==\",\"VarBinary1\":\"\",\"LongVarBinary1\":\"\",\"Blob1\":\"\"}]}",
                result, "通常のFILTER検索ができることを確認.");
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callRequestDelete("/ODataTests6(" + TEST_ID + ")");
        assertEquals(204, resp.getStatusCode(), "DELETEできることを確認.");

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests6(" + TEST_ID + ")", null);
        assertEquals(404, resp.getStatusCode(), "DELETEされたことを確認.");
    }
}
