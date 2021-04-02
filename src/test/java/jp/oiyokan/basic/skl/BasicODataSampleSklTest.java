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

import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.basic.BasicODataSampleTestUtil;

/**
 * OData サーバについて、おおざっぱな通過によるデグレードを検知.
 */
class BasicODataSampleSklTest {
    /**
     * zip code 対応
     */
    @Test
    void test02() throws Exception {
        final ODataResponse resp = BasicODataSampleTestUtil.callRequestGetResponse("/SklStaffLists",
                "$count=true&$top=20&$select=zip_code&$orderby=zip_code&$filter=zip_code%20eq%20%2700000%27");
        final String result = BasicODataSampleTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#SklStaffLists\",\"@odata.count\":0,\"value\":[]}", result);
        assertEquals(200, resp.getStatusCode());
    }
}
