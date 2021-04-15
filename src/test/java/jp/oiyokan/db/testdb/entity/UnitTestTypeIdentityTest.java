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

import jp.oiyokan.OiyokanTestSettingConstants;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * フィルタの型に着眼したテスト.
 */
class UnitTestTypeIdentityTest {
    @Test
    void test01() throws Exception {
        if (!OiyokanTestSettingConstants.IS_TEST_ODATATEST)
            return;

        ODataResponse resp = OiyokanTestUtil.callRequestPost("/ODataTests5", "{\n" //
                + "  \"Name\":\"Name\"\n" //
                + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(201, resp.getStatusCode(), //
                "Iden1が引き当てられないとエラーになる.");

        int indexOf = result.indexOf("\",\"Iden1\":");
        String subString = result.substring(indexOf + "\",\"Iden1\":".length());
        String[] subStrParts = subString.split(",");
        // System.err.println(subStrParts[0]);
        final String nowID = subStrParts[0];

        /// 通常のfilter
        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests5", "$filter=Iden1 eq " + nowID);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests5(" + nowID + ")", null);
        result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(200, resp.getStatusCode());

        // DELETE
        resp = OiyokanTestUtil.callRequestDelete("/ODataTests5(" + nowID + ")");
        assertEquals(204, resp.getStatusCode());

        resp = OiyokanTestUtil.callRequestGetResponse("/ODataTests5(" + nowID + ")", null);
        assertEquals(404, resp.getStatusCode());
    }
}
