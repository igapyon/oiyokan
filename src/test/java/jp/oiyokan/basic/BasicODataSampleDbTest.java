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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanEdmProvider;
import jp.oiyokan.OiyokanEntityCollectionProcessor;

/**
 * OData サーバについて、おおざっぱな通過によるデグレードを検知.
 */
class BasicODataSampleDbTest {
    @Test
    void testSimpleVersion() throws Exception {
        final ODataHttpHandler handler = getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/simple.svc");
        req.setRawODataPath("/ODataAppInfos");
        req.setRawQueryPath("$top=1");
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

        final ODataResponse resp = handler.process(req);
        assertEquals(200, resp.getStatusCode());
        final String result = stream2String(resp.getContent());
        // System.err.println("result: " + result);
        assertEquals(
                "{\"@odata.context\":\"$metadata#ODataAppInfos\",\"value\":[{\"KeyName\":\"Version\",\"KeyValue\":\""
                        + OiyokanConstants.VERSION + "\"}]}",
                result);
    }

    @Test
    void testSimpleOrderBy() throws Exception {
        final ODataHttpHandler handler = getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/simple.svc");
        req.setRawODataPath("/MyProducts");
        req.setRawQueryPath("$orderby=ID&$top=1&$select=ID,Name,Description");
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

        final ODataResponse resp = handler.process(req);
        assertEquals(200, resp.getStatusCode());
        assertEquals(
                "{\"@odata.context\":\"$metadata#MyProducts\",\"value\":[{\"ID\":1,\"Name\":\"MacBookPro16,2\",\"Description\":\"MacBook Pro (13-inch, 2020, Thunderbolt 3ポートx 4)\"}]}",
                stream2String(resp.getContent()));
    }

    @Test
    void testSimpleAllWithoutSelect() throws Exception {
        final ODataHttpHandler handler = getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/simple.svc");
        req.setRawODataPath("/MyProducts");
        req.setRawQueryPath("$orderby=ID&$top=2");
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

        final ODataResponse resp = handler.process(req);
        assertEquals(200, resp.getStatusCode());
        // コンテンツ内容は確認なし.
    }

    @Test
    void testSimpleFilter() throws Exception {
        final ODataHttpHandler handler = getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/simple.svc");
        req.setRawODataPath("/MyProducts");
        req.setRawQueryPath("$top=2&$filter=ID%20eq%205.0&$count=true&$select=ID,Name");
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

        final ODataResponse resp = handler.process(req);
        assertEquals(200, resp.getStatusCode());
        assertEquals(
                "{\"@odata.context\":\"$metadata#MyProducts\",\"@odata.count\":1,\"value\":[{\"ID\":5,\"Name\":\"PopTablet1\"}]}",
                stream2String(resp.getContent()));
    }

    @Test
    void testSimpleSearch() throws Exception {
        final ODataHttpHandler handler = getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/simple.svc");
        req.setRawODataPath("/MyProducts");
        req.setRawQueryPath("$top=6&$search=macbook&$count=true&$select=ID");
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

        final ODataResponse resp = handler.process(req);
        assertEquals(200, resp.getStatusCode());
        assertEquals("{\"@odata.context\":\"$metadata#MyProducts\",\"value\":[{\"ID\":1},{\"ID\":2}]}",
                stream2String(resp.getContent()));
    }

    ////////////////////////////////////////////////////////
    // 以降は共通コード.

    private ODataHttpHandler getHandler() throws Exception {
        final OData odata = OData.newInstance();

        // EdmProvider を登録.
        final ServiceMetadata edm = odata.createServiceMetadata(new OiyokanEdmProvider(), new ArrayList<>());
        final ODataHttpHandler handler = odata.createHandler(edm);

        // EntityCollectionProcessor を登録.
        handler.register(new OiyokanEntityCollectionProcessor());
        return handler;
    }

    /**
     * InputStream を String に変換.
     * 
     * @param inStream 入力ストリーム.
     * @return 文字列.
     * @throws IOException 入出力例外が発生した場合.
     */
    private static String stream2String(InputStream inStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"))) {
            for (;;) {
                String line = reader.readLine();
                if (line == null)
                    break;
                builder.append(line);
            }
        }
        return builder.toString();
    }
}
