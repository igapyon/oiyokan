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
 * IDENに関する挙動確認。
 */
class UnitTestTypeIdentity01Test {
    @Test
    void test01() throws Exception {
        @SuppressWarnings("unused")
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        ODataResponse resp = OiyokanTestUtil.callPost("/ODataTest5", "{\n" //
                + "  \"Name\":\"Name\"\n" //
                + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        final String iden1String = OiyokanTestUtil.getValueFromResultByKey(result, "Iden1");

        // System.err.println(result);
        assertEquals(201, resp.getStatusCode(), //
                "Iden1が引き当てられないとエラーになる.");

        /// 通常のfilter
        resp = OiyokanTestUtil.callGet("/ODataTest5", "$filter=Iden1 eq " + iden1String);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        resp = OiyokanTestUtil.callGet("/ODataTest5(" + iden1String + ")", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callDelete("/ODataTest5(" + iden1String + ")");
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callGet("/ODataTest5(" + iden1String + ")", null);
        assertEquals(404, resp.getStatusCode());
    }
}
