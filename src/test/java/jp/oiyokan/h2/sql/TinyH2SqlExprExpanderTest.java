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
package jp.oiyokan.h2.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanEdmProvider;

/**
 * TinyH2SqlExprExpanderのテスト.
 */
class TinyH2SqlExprExpanderTest {

    @Test
    void test01() throws Exception {
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(new OiyokanEdmProvider(), new ArrayList<>());

        final Parser parser = new Parser(edm.getEdm(), odata);
        final UriInfo uriInfo = parser.parseUri("/MyProducts", "$filter=ID eq 1.0", "",
                "https://localhost//simple.svc/");
        TinySqlBuildInfo sqlInfo = new TinySqlBuildInfo();
        new TinyH2SqlExprExpander(sqlInfo).expand(uriInfo.getFilterOption().getExpression());
        assertEquals("([ID] = 1.0)", sqlInfo.getSqlBuilder().toString());
    }

    @Test
    void test02() throws Exception {
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(new OiyokanEdmProvider(), new ArrayList<>());

        final Parser parser = new Parser(edm.getEdm(), odata);
        final UriInfo uriInfo = parser.parseUri("/MyProducts", "$filter=Description eq 'Mac' and ID eq 2.0", "",
                "https://localhost//simple.svc/");
        TinySqlBuildInfo sqlInfo = new TinySqlBuildInfo();
        new TinyH2SqlExprExpander(sqlInfo).expand(uriInfo.getFilterOption().getExpression());
        assertEquals("(([Description] = ?) AND ([ID] = 2.0))", sqlInfo.getSqlBuilder().toString());
    }

    @Test
    void test03() throws Exception {
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(new OiyokanEdmProvider(), new ArrayList<>());

        final Parser parser = new Parser(edm.getEdm(), odata);
        final UriInfo uriInfo = parser.parseUri("/MyProducts",
                "%24top=51&%24filter=%20indexof%28Description%2C%27%E5%A2%97%E6%AE%96%E3%82%BF%E3%83%96%E3%83%AC%E3%83%83%E3%83%887%27%29%20ne%20-1&%24orderby=ID&%24count=true&%24select=Description%2CID%2CName",
                "", "https://localhost//simple.svc/");
        TinySqlBuildInfo sqlInfo = new TinySqlBuildInfo();
        new TinyH2SqlExprExpander(sqlInfo).expand(uriInfo.getFilterOption().getExpression());
        assertEquals("((POSITION(?,[Description]) - 1) <> -1)", sqlInfo.getSqlBuilder().toString());
    }
}
