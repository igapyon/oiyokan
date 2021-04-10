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
 * フィルタの型に着眼したテスト.
 */
class TestODataEntityFullTestDbTest {
    /**
     * テストデータが利用する ID 範囲。
     */
    private static final int TEST_ID = 10015;

    @Test
    void test01() throws Exception {
        if (!OiyokanTestConstants.IS_TEST_ODATATEST)
            return;

        // FULL INSERT
        ODataResponse resp = OiyokanTestUtil.callRequestPost("/ODataTests3", //
                "{\n" //
                        + "  \"ID\": " + TEST_ID + ",\n" //
                        + "  \"Name\": \"Name\",\n" //
                        + "  \"Description\": \"Description\",\n" //
                        + "  \"Sbyte1\": 127,\n" //
                        + "  \"Int16a\": 32767,\n" //
                        + "  \"Int32a\": 2147483647,\n" //
                        + "  \"Int64a\": 2147483647,\n" //
                        + "  \"Decimal1\": 1234.56,\n" //
                        + "  \"StringChar2\": \"C2\",\n" //
                        + "  \"StringVar255\": \"VARCHAR255\",\n" //
                        + "  \"StringLongVar1\": \"LONGVARCHAR\",\n" //
                        + "  \"Clob1\": \"CLOB\",\n" //
                        + "  \"Boolean1\": false,\n" //
                        + "  \"Single1\": 123.45679,\n" //
                        + "  \"Double1\": 123.4567890123,\n" //
                        + "  \"Date1\": \"2021-04-10\",\n" //
                        + "  \"DateTimeOffset1\": \"2021-04-10T10:12:49.082587Z\",\n" //
                        + "  \"TimeOfDay1\": \"19:12:49\",\n" //
                        + "  \"Binary1\": \"SGVsbG8gd29ybGQh\",\n" //
                        + "  \"VarBinary1\": \"SGVsbG8gd29ybGQh\",\n" //
                        + "  \"LongVarBinary1\": \"SGVsbG8gd29ybGQh\",\n" //
                        + "  \"Blob1\": \"SGVsbG8gd29ybGQh\",\n" //
                        + "  \"Uuid1\": \"8a98afad-72f0-40ae-a401-e92775ec02dd\"\n" //
                        + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println("TRACE: " + result);
        assertEquals(201, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests3(" + TEST_ID + ")", null);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callRequestDelete("/ODataTests3(" + TEST_ID + ")");
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests3(" + TEST_ID + ")", null);
        assertEquals(404, resp.getStatusCode());
    }
}
