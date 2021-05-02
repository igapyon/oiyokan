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
 * Binary 型に着眼したテスト.
 * 
 * 通常 $filterも交えて確認.
 */
class UnitTestTypeDecimal02Test {
    @Test
    void test01() throws Exception {
        @SuppressWarnings("unused")
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        ODataResponse resp = OiyokanTestUtil.callPost("/ODataTest2", "{\n" //
                + "  \"Decimal1\":8765.43\n" //
                + "  ,\"StringChar8\":\"ABCDEFGH\"\n" //
                + "  ,\"StringVar255\":\"VARKEY1\"\n" //
                + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        assertEquals("8765.43", OiyokanTestUtil.getValueFromResultByKey(result, "Decimal1"));
        assertEquals(201, resp.getStatusCode());

        final String key = "Decimal1=8765.43,StringChar8='ABCDEFGH',StringVar255='VARKEY1'";

        resp = OiyokanTestUtil.callGet("/ODataTest2(" + key + ")", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        assertEquals(
                "{\"@odata.context\":\"$metadata#ODataTest2\",\"Decimal1\":8765.43,\"StringChar8\":\"ABCDEFGH\",\"StringVar255\":\"VARKEY1\",\"Name\":\"Multi-col UnitTest\",\"Description\":null}",
                result);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callDelete("/ODataTest2(" + key + ")");
        assertEquals(204, resp.getStatusCode(), "作成データを後始末.");
    }
}
