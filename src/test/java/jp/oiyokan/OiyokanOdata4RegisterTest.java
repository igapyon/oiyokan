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
package jp.oiyokan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Olingoとして一番上位に該当する OiyokanOdata4Register からの動作確認。
 */
class OiyokanOdata4RegisterTest {
    @Test
    void test01() throws Exception {
        final MockHttpServletRequest req = new MockHttpServletRequest();
        final MockHttpServletResponse resp = new MockHttpServletResponse();
        final String url = "https://oiyokan.herokuapp.com" + OiyokanConstants.ODATA_ROOTPATH + "/";
        req.setMethod("GET");
        req.setRequestURI(url);
        new OiyokanOdata4Register().serv(req, resp);
        // System.err.println(resp.getContentAsString());
        assertEquals(200, resp.getStatus());
    }

    @Test
    void test02() throws Exception {
        final MockHttpServletRequest req = new MockHttpServletRequest();
        final MockHttpServletResponse resp = new MockHttpServletResponse();
        final String url = "https://oiyokan.herokuapp.com" + OiyokanConstants.ODATA_ROOTPATH + "/$metadata";
        req.setMethod("GET");
        req.setRequestURI(url);
        new OiyokanOdata4Register().serv(req, resp);
        assertEquals(200, resp.getStatus());
        // System.err.println(resp.getContentAsString());
    }

    @Test
    void test03() throws Exception {
        final MockHttpServletRequest req = new MockHttpServletRequest();
        final MockHttpServletResponse resp = new MockHttpServletResponse();
        final String url = "https://oiyokan.herokuapp.com" + OiyokanConstants.ODATA_ROOTPATH + "/Oiyokans";
        req.setMethod("GET");
        req.setRequestURI(url);
        new OiyokanOdata4Register().serv(req, resp);
        assertEquals(
                "{\"@odata.context\":\"$metadata#Oiyokans\",\"value\":[{\"KeyName\":\"Provider\",\"KeyValue\":\"Oiyokan\"},{\"KeyName\":\"Version\",\"KeyValue\":\""
                        + OiyokanConstants.VERSION + "\"}]}",
                resp.getContentAsString());
        assertEquals(200, resp.getStatus());
        // System.err.println(resp.getContentAsString());
    }
}
