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
 * 
 * IS NULL 関連
 */
class SakilaValueNullTest {
    /**
     * リテラルの null 対応
     * 
     * IS NULL で右辺が null の展開の確認。
     */
    @Test
    void test01() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_SAKILA)
            return;

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse( //
                "/SklAddresses", OiyoUrlUtil.encodeUrlQuery( //
                        "$top=1 &$count=true &$filter=address2 eq null &$select=address_id &$orderby=address_id"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // 検索結果が存在するべき。
        assertEquals(
                "{\"@odata.context\":\"$metadata#SklAddresses\",\"@odata.count\":4,\"value\":[{\"address_id\":1}]}",
                result, "eq で右辺が null リテラルの処理");
        assertEquals(200, resp.getStatusCode());
    }

    /**
     * リテラルの null 対応
     * 
     * IS NULL で左辺が null 展開の確認。
     */
    @Test
    void test02() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_SAKILA)
            return;

        // NULLの件数をカウント.
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse( //
                "/SklAddresses", OiyoUrlUtil.encodeUrlQuery( //
                        "$top=1 &$count=true &$filter=null eq address2 &$select=address_id &$orderby=address_id"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // 検索結果が存在するべき。
        assertEquals(
                "{\"@odata.context\":\"$metadata#SklAddresses\",\"@odata.count\":4,\"value\":[{\"address_id\":1}]}",
                result, "eq で左辺が null リテラルの処理");
        assertEquals(200, resp.getStatusCode());
    }

    /**
     * リテラルの null 対応
     * 
     * IS NOT NULL 展開の確認。
     */
    @Test
    void test03() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_SAKILA)
            return;

        // NOT EQUAL NULL の件数をカウント.
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse( //
                "/SklAddresses", OiyoUrlUtil.encodeUrlQuery( //
                        "$top=1 &$count=true &$filter=address2 ne null &$select=address_id &$orderby=address_id"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("dec: " + OiyokanTestUtil.decodeUrlQuery(
        // "$top=1&$count=true&$filter=address2%20ne%20null&$select=address_id&$orderby=address_id"));

        // 検索結果が存在するべき。
        assertEquals(
                "{\"@odata.context\":\"$metadata#SklAddresses\",\"@odata.count\":599,\"value\":[{\"address_id\":5}]}",
                result, "ne で右辺が null リテラルの処理 (ne で左辺は null にはできない)");
        assertEquals(200, resp.getStatusCode());
    }
}
