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

import jp.app.ctrl.ThSakilaCtrl;
import jp.oiyokan.OiyokanConstants;

/**
 * OData サーバについて、おおざっぱな通過によるデグレードを検知.
 */
class BasicODataSakilaAllDbTest {
    @Test
    void test01() throws Exception {
        final ODataHttpHandler handler = BasicODataSampleTestUtil.getHandler();
        for (String[] entrys : ThSakilaCtrl.ODATA_ENTRY_INFOS) {
            if (entrys[0].indexOf("$search") >= 0) {
                if (!OiyokanConstants.IS_EXPERIMENTAL_SEARCH_ENABLED) {
                    // 実験的 $search について無効化されているためテストから除外.
                    continue;
                }
            }

            String[] entry = entrys[1].split("['?']");

            final ODataRequest req = new ODataRequest();
            req.setMethod(HttpMethod.GET);
            req.setRawBaseUri("http://localhost:8080/odata4.svc");
            req.setRawODataPath(entry[0]);
            req.setRawQueryPath(entry[1]);
            req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

            final ODataResponse resp = handler.process(req);
            final String result = BasicODataSampleTestUtil.stream2String(resp.getContent());
            // System.err.println("[" + entrys[0] + "], [" + entrys[1] + "], result: " +
            // result);
            // 注意：入力の文字列が正しくURLエンコードされていないと以下の asset が失敗する.
            assertEquals(200, resp.getStatusCode());
        }
    }
}
