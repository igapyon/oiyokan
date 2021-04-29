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
 * Entityアクセスのフル設定に着眼したテスト.
 */
class UnitTestEntityPostAll01Test {
    @Test
    void test01() throws Exception {
        @SuppressWarnings("unused")
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        // FULL INSERT
        ODataResponse resp = OiyokanTestUtil.callPost("/ODataTest3", //
                "{\n" //
                        + "  \"Name\": \"Name\",\n" //
                        + "  \"Description\": \"Description\",\n" //
                        + "  \"Sbyte1\": 127,\n" //
                        + "  \"Int16a\": 32767,\n" //
                        + "  \"Int32a\": 2147483647,\n" //
                        + "  \"Int64a\": 2147483647,\n" //
                        + "  \"Decimal1\": 1234.56,\n" //
                        + "  \"StringChar8\": \"C2345678\",\n" //
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
                        + "  \"Blob1\": \"SGVsbG8gd29ybGQh\"\n" //
                        + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        final String idString = OiyokanTestUtil.getValueFromResultByKey(result, "ID");
        assertEquals(201, resp.getStatusCode(), //
                "POSTによるINSERTが成功すること。既知の問題. Postgresにおいて、uuidのエラーで失敗する (既知の問題), MySQLでもUUIDの桁溢れエラー (既知の問題)");

        resp = OiyokanTestUtil.callGet("/ODataTest3(" + idString + ")", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callDelete("/ODataTest3(" + idString + ")");
        assertEquals(204, resp.getStatusCode(), "DELETEが成功すること.");

        // after DELETE
        resp = OiyokanTestUtil.callGet("/ODataTest3(" + idString + ")", null);
        assertEquals(404, resp.getStatusCode(), "DELETEのあとはレコードが存在しない.");
    }
}
