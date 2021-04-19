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
package jp.oiyokan.db.testdb.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanTestSettingConstants;
import jp.oiyokan.common.OiyoUrlUtil;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * フィルタの型に着眼したテスト.
 */
class UnitTestQueryMethodTest {
    @Test
    void testStartsWithA() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse( //
                "/ODataTests1", //
                OiyoUrlUtil.encodeUrlQuery("&$filter=startswith(StringVar255, 'ABCDEFG') &$count=true &$select=ID"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":1,\"value\":[{\"ID\":204}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    // ENDSWITH
    @Test
    void testEndsWithA() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                OiyoUrlUtil.encodeUrlQuery("&$filter=endswith(StringVar255, 'VWXYZ') &$count=true &$select=ID"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":1,\"value\":[{\"ID\":204}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testContainsA() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                OiyoUrlUtil.encodeUrlQuery("&$filter=contains(StringVar255, 'HIJK') &$count=true &$select=ID"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":1,\"value\":[{\"ID\":204}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testIndexOfA() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        // indexof は 0 ベース.
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse( //
                "/ODataTests1", //
                OiyoUrlUtil.encodeUrlQuery("&$filter=indexof(StringVar255, 'HIJK') eq 7 &$count=true &$select=ID"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":1,\"value\":[{\"ID\":204}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    // LENGTH
    @Test
    void testLengthA() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                OiyoUrlUtil.encodeUrlQuery("&$filter=length(StringVar255) eq 26 &$count=true &$select=ID"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":1,\"value\":[{\"ID\":204}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    // SUBSTRING
    @Test
    void testSubstringA() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse( //
                "/ODataTests1", //
                OiyoUrlUtil.encodeUrlQuery("&$filter=substring(StringVar255,3,2) eq 'CD' &$count=true &$select=ID"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":1,\"value\":[{\"ID\":204}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    // TOLOWER
    @Test
    void testTolowerA() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse( //
                "/ODataTests1", OiyoUrlUtil.encodeUrlQuery( //
                        "&$filter=tolower(StringVar255) eq 'abcdefghijklmnopqrstuvwxyz' &$count=true &$select=ID"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":1,\"value\":[{\"ID\":204}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    // TOUPPER
    @Test
    void testToupperA() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse( //
                "/ODataTests1", OiyoUrlUtil.encodeUrlQuery( //
                        "&$filter=toupper(tolower(StringVar255)) eq 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' &$count=true &$select=ID"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":1,\"value\":[{\"ID\":204}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    // TRIM
    @Test
    void testTrimA() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse( //
                "/ODataTests1", OiyoUrlUtil.encodeUrlQuery( //
                        "&$filter=trim(concat(' ', StringVar255)) eq 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' &$count=true &$select=ID"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":1,\"value\":[{\"ID\":204}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    // CONCAT
    @Test
    void testConcatA() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse( //
                "/ODataTests1", OiyoUrlUtil.encodeUrlQuery( //
                        "&$filter=concat(' ', StringVar255) eq ' ABCDEFGHIJKLMNOPQRSTUVWXYZ' &$count=true &$select=ID"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":1,\"value\":[{\"ID\":204}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    // SUBSTRINGOF
    @Test
    void testSubstringofA() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                OiyoUrlUtil.encodeUrlQuery("&$filter=substringof(StringVar255, 'EFG') &$count=true &$select=ID"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":1,\"value\":[{\"ID\":204}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    // NOT

    // MINUS
}
