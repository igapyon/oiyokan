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
package jp.oiyokan.db.testdb.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanUnittestUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * EntityのNULL多用の基本的なテスト.
 */
class UnitTestEntityNull01Test {

    /**
     * CREATE + DELETE
     */
    @Test
    void test01() throws Exception {
        @SuppressWarnings("unused")
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        // INSERT + DELETE
        // 自動項目の ID もNULLで引き渡す。
        ODataResponse resp = OiyokanTestUtil.callRequestPost("/ODataTest1", "{\n" //
                + "  \"ID\":null,\n" //
                + "  \"Name\":\"Name\",\n" //
                + "  \"Description\":\"Description\"\n" + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        System.err.println("TRACE: " + result);
        final String idString = OiyokanTestUtil.getValueFromResultByKey(result, "ID");
        assertEquals(201, resp.getStatusCode(), "");

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTest1(" + idString + ")", null);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callRequestDelete("/ODataTest1(" + idString + ")");
        assertEquals(204, resp.getStatusCode());

        // NOT FOUND after DELETED
        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTest1(" + idString + ")", null);
        assertEquals(404, resp.getStatusCode());
    }
}
