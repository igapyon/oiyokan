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

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Oiyokan (OData v4 server) を Spring Boot の Servlet として登録.
 * 
 * 特定のパス '/odata4.svc/' に対するリクエストを OData 処理に連携.
 */
@RestController
public class OiyokanOdata4Register {
    /**
     * Oiyokan (OData v4 server) を Spring Boot の Servlet として登録.
     * 
     * @param req  HTTPリクエスト.
     * @param resp HTTPレスポンス.
     * @throws ServletException サーブレット例外.
     */
    @RequestMapping("/odata4.svc/*")
    private void serv(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException {
        String uri = req.getRequestURI();
        if (req.getQueryString() != null) {
            try {
                // Query String を uri に追加.
                uri += "?" + new URLCodec().decode(req.getQueryString());
            } catch (DecoderException ex) {
                // [M001] Can't decode specified decodec url
                System.err.println(OiyokanMessages.M001 + ": " + ex.toString());
                throw new ServletException(OiyokanMessages.M001);
            }
        }

        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: URI: " + uri);

        try {
            OData odata = OData.newInstance();

            // EdmProvider を登録.
            ServiceMetadata edm = odata.createServiceMetadata(new OiyokanEdmProvider(), new ArrayList<>());
            ODataHttpHandler handler = odata.createHandler(edm);

            // EntityCollectionProcessor を登録.
            handler.register(new OiyokanEntityCollectionProcessor());

            // Spring と Servlet の挙動を調整.
            handler.process(new HttpServletRequestWrapper(req) {
                @Override
                public String getServletPath() {
                    return "/odata4.svc";
                }
            }, resp);
        } catch (RuntimeException ex) {
            if (OiyokanConstants.IS_TRACE_ODATA_V4)
                System.err.println("OData v4: OiyokanOdata4Register#serv(): Unexpected Server Error: " + ex.toString());
            ex.printStackTrace();
            throw new ServletException(ex);
        }
    }
}