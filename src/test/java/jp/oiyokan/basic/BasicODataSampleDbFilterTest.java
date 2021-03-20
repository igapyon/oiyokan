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

import jp.oiyokan.OiyokanConstants;

/**
 * OData サーバについて、おおざっぱな通過によるデグレードを検知.
 */
class BasicODataSampleDbFilterTest {
    // @Test
    void testSimpleVersion() throws Exception {
        final ODataHttpHandler handler = BasicODataSampleDbTest.getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/odata4.svc");
        req.setRawODataPath("/MyProducts");
        req.setRawQueryPath(
                "$top=51&$filter=DateTimeOffset1 lt 2020-12-31T21:53:00Z&$orderby=ID&$count=true&$select=ID");
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

        final ODataResponse resp = handler.process(req);
        // assertEquals(200, resp.getStatusCode());
        final String result = BasicODataSampleDbTest.stream2String(resp.getContent());
        System.err.println("result: " + result);
        assertEquals(
                "{\"@odata.context\":\"$metadata#ODataAppInfos\",\"value\":[{\"KeyName\":\"Version\",\"KeyValue\":\""
                        + OiyokanConstants.VERSION + "\"}]}",
                result);
    }

}
