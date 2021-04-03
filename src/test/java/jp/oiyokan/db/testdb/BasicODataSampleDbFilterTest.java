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
package jp.oiyokan.db.testdb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.util.OiyokanTestUtil;

/**
 * OData サーバについて、おおざっぱな通過によるデグレードを検知.
 */
class BasicODataSampleDbFilterTest {

    @Test
    void testTimestamp() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                "$top=51&$filter=DateTimeOffset1 lt 2020-12-31T21:53:00Z&$orderby=ID&$count=true&$select=ID");
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":0,\"value\":[]}", result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testDate() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                "$top=51&$filter=Date1 lt 2021-01-01&$orderby=ID&$count=true&$select=ID");
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":0,\"value\":[]}", result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testBoolean() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                "$filter=Boolean1 eq false&$orderby=ID&$select=ID&$top=1");
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"value\":[{\"ID\":1}]}", result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testString() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataAppInfos",
                "$filter=KeyName%20eq%20%27Provider%27");
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals(
                "{\"@odata.context\":\"$metadata#ODataAppInfos\",\"value\":[{\"KeyName\":\"Provider\",\"KeyValue\":\"Oiyokan\"}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testInt16a() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                "$top=1&$filter=Int16a eq 32767&$orderby=ID&$select=ID");
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"value\":[{\"ID\":1}]}", result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testInt32a() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                "$top=2&$filter=Int32a eq 2147483647&$orderby=ID&$count=true&$select=ID");
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals(
                "{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":203,\"value\":[{\"ID\":1},{\"ID\":2}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testInt64a() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                "$top=2 &$skip=2 &$filter=Int64a eq 2147483647 &$orderby=ID &$count=true &$select=ID");
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals(
                "{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":203,\"value\":[{\"ID\":3},{\"ID\":4}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testIntBigDecimal() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                "$top=2 &$skip=2 &$filter=Decimal1 eq 1234.56 &$orderby=ID &$count=true &$select=ID");
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals(
                "{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":203,\"value\":[{\"ID\":3},{\"ID\":4}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testSbyte1() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                "$top=1&$filter=Sbyte1 eq 127&$orderby=ID&$count=true&$select=ID");
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":203,\"value\":[{\"ID\":1}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testSingle1() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                "$top=1&$filter=Single1 eq 123.45678711&$orderby=ID&$count=true&$select=ID");
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":203,\"value\":[{\"ID\":1}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testDouble1() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                "$top=51&$filter=Double1 lt 123.456789&$orderby=ID&$count=true&$select=ID");
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":0,\"value\":[]}", result);
        assertEquals(200, resp.getStatusCode());
    }
}
