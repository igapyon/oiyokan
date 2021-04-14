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
package jp.oiyokan.db.oiyokankan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * ODataAppInfos についての Simple な Call Test.
 */
class UnitTestOiyokanKanTest {
    @Test
    void testSimpleVersion() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/Oiyokans", "$top=1&$skip=1");
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#Oiyokans\",\"value\":[{\"KeyName\":\"Version\",\"KeyValue\":\""
                + OiyokanConstants.VERSION + "\"}]}", result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testFilter() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/Oiyokans",
                "$filter=KeyName%20eq%20%27Provider%27");
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals(
                "{\"@odata.context\":\"$metadata#Oiyokans\",\"value\":[{\"KeyName\":\"Provider\",\"KeyValue\":\"Oiyokan\"}]}",
                result);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testEntity() throws Exception {
        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse("/Oiyokans('Provider')", null);
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#Oiyokans\",\"KeyName\":\"Provider\",\"KeyValue\":\"Oiyokan\"}",
                result);
        assertEquals(200, resp.getStatusCode());
    }
}
