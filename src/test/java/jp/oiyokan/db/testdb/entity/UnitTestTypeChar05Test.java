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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanUnittestUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * NULLが設定された CHARの挙動確認。
 */
class UnitTestTypeChar05Test {
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(UnitTestTypeChar05Test.class);

    @Test
    void test04() throws Exception {
        @SuppressWarnings("unused")
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        // 全項目をデフォルト値でセット。
        // StringChar8 だけは null にセット.
        ODataResponse resp = OiyokanTestUtil.callRequestPost("/ODataTests1", //
                "{\n" //
                        + "  \"StringChar8\": null\n" //
                        + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        // log.debug(result);
        final String idString = OiyokanTestUtil.getValueFromResultByKey(result, "ID");
        assertEquals(201, resp.getStatusCode(), "");

        /// 通常のfilter
        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests1",
                "$select=ID,StringChar8&$filter=ID eq " + idString + " and StringChar8 eq null");
        result = OiyokanTestUtil.stream2String(resp.getContent());
        assertEquals("null", OiyokanTestUtil.getValueFromResultByKey(result, "StringChar8"), result);
        // log.debug("TRACE: " + result);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callRequestDelete("/ODataTests1(" + idString + ")");
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(204, resp.getStatusCode());
    }
}
