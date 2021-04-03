package jp.oiyokan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 一番外側のレベルからの疎通確認
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
        assertEquals(200, resp.getStatus());
        // System.err.println(resp.getContentAsString());
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
        final String url = "https://oiyokan.herokuapp.com" + OiyokanConstants.ODATA_ROOTPATH + "/ODataAppInfos";
        req.setMethod("GET");
        req.setRequestURI(url);
        new OiyokanOdata4Register().serv(req, resp);
        assertEquals(
                "{\"@odata.context\":\"$metadata#ODataAppInfos\",\"value\":[{\"KeyName\":\"Provider\",\"KeyValue\":\"Oiyokan\"},{\"KeyName\":\"Version\",\"KeyValue\":\""
                        + OiyokanConstants.VERSION + "\"}]}",
                resp.getContentAsString());
        assertEquals(200, resp.getStatus());
        // System.err.println(resp.getContentAsString());
    }
}
