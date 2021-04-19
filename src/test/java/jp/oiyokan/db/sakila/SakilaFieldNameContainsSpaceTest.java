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
package jp.oiyokan.db.sakila;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanTestSettingConstants;
import jp.oiyokan.common.OiyoUrlUtil;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * OData サーバについて、おおざっぱな通過によるデグレードを検知.
 */
class SakilaFieldNameContainsSpaceTest {
    /**
     * zip code 対応
     */
    @Test
    void test02() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_SAKILA)
            return;

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse( //
                "/SklStaffLists", OiyoUrlUtil.encodeUrlQuery( //
                        "$count=true &$top=20 &$select=zip_code &$orderby=zip_code &$filter=zip_code eq '00000'"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("dec: " + OiyokanTestUtil.decodeUrlQuery(
        // "$count=true&$top=20&$select=zip_code&$orderby=zip_code&$filter=zip_code%20eq%20%2700000%27"));

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#SklStaffLists\",\"@odata.count\":0,\"value\":[]}", result,
                "DB上で空白を含む項目名を処理できることの確認。");
        assertEquals(200, resp.getStatusCode());
    }
}
