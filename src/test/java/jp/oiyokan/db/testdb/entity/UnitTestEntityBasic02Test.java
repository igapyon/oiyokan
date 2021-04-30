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
 * Entityの基本的なテスト.
 */
class UnitTestEntityBasic02Test {
    /**
     * CREATE + UPDATE + DELETE
     */
    @Test
    void test02() throws Exception {
        @SuppressWarnings("unused")
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        // INSERT
        ODataResponse resp = OiyokanTestUtil.callPost("/ODataTest3", "{\n" //
                + "  \"Name\":\"Name\",\n" //
                + "  \"Description\":\"Description\"\n" + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        final String idString = OiyokanTestUtil.getValueFromResultByKey(result, "ID");

        assertEquals(201, resp.getStatusCode());

        // SELECT
        resp = OiyokanTestUtil.callGet("/ODataTest3(" + idString + ")", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        // UPDATE (PATCH)
        resp = OiyokanTestUtil.callPatch("/ODataTest3(" + idString + ")", "{\n" //
                + "  \"Name\":\"Name2\",\n" //
                + "  \"Description\":\"Description2\"\n" + "}", false, false);
        assertEquals(200, resp.getStatusCode());

        resp = OiyokanTestUtil.callGet("/ODataTest3(" + idString + ")", null);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callDelete("/ODataTest3(" + idString + ")");
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callGet("/ODataTest3(" + idString + ")", null);
        assertEquals(404, resp.getStatusCode());
    }
}
