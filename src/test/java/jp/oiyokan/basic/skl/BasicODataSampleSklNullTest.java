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
package jp.oiyokan.basic.skl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.basic.BasicODataSampleTestUtil;

/**
 * OData サーバについて、おおざっぱな通過によるデグレードを検知.
 * 
 * IS NULL 関連
 */
class BasicODataSampleSklNullTest {
    /**
     * リテラルの null 対応
     * 
     * IS NULL 展開の確認。
     */
    @Test
    void test01() throws Exception {
        final ODataHttpHandler handler = BasicODataSampleTestUtil.getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/odata4.svc");
        req.setRawODataPath("/SklAddresss");
        req.setRawQueryPath("$top=1&$count=true&$filter=address2%20eq%20null&$select=address_id&$orderby=address_id"); // NULLの件数をカウント.
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

        final ODataResponse resp = handler.process(req);
        final String result = BasicODataSampleTestUtil.stream2String(resp.getContent());
        // 検索結果が存在するべき。
        assertEquals("{\"@odata.context\":\"$metadata#SklAddresss\",\"@odata.count\":4,\"value\":[{\"address_id\":1}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    /**
     * リテラルの null 対応
     * 
     * IS NULL 展開の確認。
     */
    @Test
    void test02() throws Exception {
        final ODataHttpHandler handler = BasicODataSampleTestUtil.getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/odata4.svc");
        req.setRawODataPath("/SklAddresss");
        req.setRawQueryPath("$top=1&$count=true&$filter=null%20eq%20address2&$select=address_id&$orderby=address_id"); // NULLの件数をカウント.
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

        final ODataResponse resp = handler.process(req);
        final String result = BasicODataSampleTestUtil.stream2String(resp.getContent());
        // 検索結果が存在するべき。
        assertEquals("{\"@odata.context\":\"$metadata#SklAddresss\",\"@odata.count\":4,\"value\":[{\"address_id\":1}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }
}
