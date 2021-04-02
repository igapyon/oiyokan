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
package jp.oiyokan.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanConstants;

/**
 * OData サーバについて、おおざっぱな通過によるデグレードを検知.
 */
class BasicODataTestDbTest {
    @Test
    void testSimpleVersion() throws Exception {
        final ODataResponse resp = BasicODataSampleTestUtil.callRequestGetResponse("/ODataAppInfos", "$top=1&$skip=1");
        final String result = BasicODataSampleTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals(
                "{\"@odata.context\":\"$metadata#ODataAppInfos\",\"value\":[{\"KeyName\":\"Version\",\"KeyValue\":\""
                        + OiyokanConstants.VERSION + "\"}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testSimpleOrderBy() throws Exception {
        final ODataResponse resp = BasicODataSampleTestUtil.callRequestGetResponse("/ODataTests1",
                "$orderby=ID&$top=1&$select=ID,Name,Description");
        final String result = BasicODataSampleTestUtil.stream2String(resp.getContent());

        assertEquals(
                "{\"@odata.context\":\"$metadata#ODataTests1\",\"value\":[{\"ID\":1,\"Name\":\"MacBookPro16,2\",\"Description\":\"MacBook Pro (13-inch, 2020, Thunderbolt 3ポートx 4)\"}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testSimpleAllWithoutSelect() throws Exception {
        final ODataResponse resp = BasicODataSampleTestUtil.callRequestGetResponse("/ODataTests1",
                "$orderby=ID&$top=2");
        final String result = BasicODataSampleTestUtil.stream2String(resp.getContent());

        // コンテンツ内容は確認なし.
        // System.err.println(BasicODataSampleTestUtil.stream2String(resp.getContent()));
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testSimpleFilter() throws Exception {
        final ODataResponse resp = BasicODataSampleTestUtil.callRequestGetResponse("/ODataTests1",
                "$top=2&$filter=ID%20eq%205.0&$count=true&$select=ID,Name");
        final String result = BasicODataSampleTestUtil.stream2String(resp.getContent());

        assertEquals(
                "{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":1,\"value\":[{\"ID\":5,\"Name\":\"PopTablet1\"}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testSimpleSearch() throws Exception {
        if (!OiyokanConstants.IS_EXPERIMENTAL_SEARCH_ENABLED) {
            System.err.println("$search はサポート外: テストスキップします.");
            return;
        }

        final ODataResponse resp = BasicODataSampleTestUtil.callRequestGetResponse("/ODataTestFulls1",
                "$top=6&$search=macbook&$count=true&$select=ID");
        final String result = BasicODataSampleTestUtil.stream2String(resp.getContent());

        assertEquals("{\"@odata.context\":\"$metadata#ODataTestFulls1\",\"value\":[{\"ID\":1},{\"ID\":2}]}", result);
        assertEquals(200, resp.getStatusCode());
    }
}
