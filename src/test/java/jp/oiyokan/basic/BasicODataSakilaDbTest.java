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

import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanConstants;

/**
 * OData サーバについて、おおざっぱな通過によるデグレードを検知.
 */
class BasicODataSakilaDbTest {
    @Test
    void testSimpleVersion() throws Exception {
        final ODataHttpHandler handler = BasicODataSampleTestUtil.getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/odata4.svc");
        req.setRawODataPath("/ODataAppInfos");
        req.setRawQueryPath("$top=1");
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

        final ODataResponse resp = handler.process(req);
        assertEquals(200, resp.getStatusCode());
        final String result = BasicODataSampleTestUtil.stream2String(resp.getContent());
        // System.err.println("result: " + result);
        assertEquals(
                "{\"@odata.context\":\"$metadata#ODataAppInfos\",\"value\":[{\"KeyName\":\"Version\",\"KeyValue\":\""
                        + OiyokanConstants.VERSION + "\"}]}",
                result);
    }

    @Test
    void testSimpleOrderBy() throws Exception {
        final ODataHttpHandler handler = BasicODataSampleTestUtil.getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/odata4.svc");
        req.setRawODataPath("/MyProducts");
        req.setRawQueryPath("$orderby=ID&$top=1&$select=ID,Name,Description");
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

        final ODataResponse resp = handler.process(req);
        assertEquals(200, resp.getStatusCode());
        assertEquals(
                "{\"@odata.context\":\"$metadata#MyProducts\",\"value\":[{\"ID\":1,\"Name\":\"MacBookPro16,2\",\"Description\":\"MacBook Pro (13-inch, 2020, Thunderbolt 3ポートx 4)\"}]}",
                BasicODataSampleTestUtil.stream2String(resp.getContent()));
    }

    @Test
    void testSimpleAllWithoutSelect() throws Exception {
        final ODataHttpHandler handler = BasicODataSampleTestUtil.getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/odata4.svc");
        req.setRawODataPath("/MyProducts");
        req.setRawQueryPath("$orderby=ID&$top=2");
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

        final ODataResponse resp = handler.process(req);
        assertEquals(200, resp.getStatusCode());
        // コンテンツ内容は確認なし.
        // System.err.println(BasicODataSampleTestUtil.stream2String(resp.getContent()));
    }

    @Test
    void testSimpleFilter() throws Exception {
        final ODataHttpHandler handler = BasicODataSampleTestUtil.getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/odata4.svc");
        req.setRawODataPath("/MyProducts");
        req.setRawQueryPath("$top=2&$filter=ID%20eq%205.0&$count=true&$select=ID,Name");
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

        final ODataResponse resp = handler.process(req);
        assertEquals(200, resp.getStatusCode());
        assertEquals(
                "{\"@odata.context\":\"$metadata#MyProducts\",\"@odata.count\":1,\"value\":[{\"ID\":5,\"Name\":\"PopTablet1\"}]}",
                BasicODataSampleTestUtil.stream2String(resp.getContent()));
    }

    // @Test
    void testSimpleSearch() throws Exception {
        final ODataHttpHandler handler = BasicODataSampleTestUtil.getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/odata4.svc");
        req.setRawODataPath("/MyProducts");
        req.setRawQueryPath("$top=6&$search=macbook&$count=true&$select=ID");
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

        final ODataResponse resp = handler.process(req);
        assertEquals(200, resp.getStatusCode());
        assertEquals("{\"@odata.context\":\"$metadata#MyProducts\",\"value\":[{\"ID\":1},{\"ID\":2}]}",
                BasicODataSampleTestUtil.stream2String(resp.getContent()));
    }
}
