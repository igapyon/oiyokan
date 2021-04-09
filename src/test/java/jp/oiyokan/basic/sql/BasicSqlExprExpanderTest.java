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
package jp.oiyokan.basic.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanCsdlEntityContainer;
import jp.oiyokan.OiyokanCsdlEntitySet;
import jp.oiyokan.OiyokanEdmProvider;
import jp.oiyokan.OiyokanTestConstants;
import jp.oiyokan.basic.BasicUrlUtil;

/**
 * TinyH2SqlExprExpanderのテスト.
 */
class BasicSqlExprExpanderTest {
    final OiyokanCsdlEntityContainer localTemplateEntityContainer = new OiyokanCsdlEntityContainer();

    /**
     * クラス内の共通関数
     * 
     * @param rawODataPath データパス.
     * @param rawQueryPath クエリパス.
     * @return SQL条件文.
     * @throws Exception 例外が発生した場合.
     */
    private String getExprString(String rawODataPath, String rawQueryPath) throws Exception {
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(new OiyokanEdmProvider(), new ArrayList<>());

        localTemplateEntityContainer.ensureBuild();
        // アプリ情報が入っている内部DBをベースに処理。つまり h2 database 前提としての振る舞いをおこなう。
        final OiyokanCsdlEntitySet entitySet = (OiyokanCsdlEntitySet) localTemplateEntityContainer
                .getEntitySet("Oiyokans");

        // EntityType をロード済み状態にする.
        localTemplateEntityContainer.getEntityType(entitySet.getTypeFQN());

        if (entitySet == null || entitySet.getEntityType() == null) {
            final String message = "ERROR: Fail to load Oiyokans EntitySet.";
            System.err.println(message);
            throw new ODataApplicationException(message, 500, Locale.ENGLISH);
        }

        final Parser parser = new Parser(edm.getEdm(), odata);
        final UriInfo uriInfo = parser.parseUri(rawODataPath, rawQueryPath, "", "http://localhost:8080/odata4.svc/");
        BasicSqlInfo sqlInfo = new BasicSqlInfo(entitySet);
        new BasicSqlExprExpander(sqlInfo).expand(uriInfo.getFilterOption().getExpression());
        return sqlInfo.getSqlBuilder().toString();
    }

    /////////////////////
    // Test Body

    @Test
    void test01() throws Exception {
        if (!OiyokanTestConstants.IS_TEST_ODATATEST)
            return;

        assertEquals("(ID = 1.0)", getExprString("/ODataTests1", //
                BasicUrlUtil.encodeUrlQuery("$filter=ID eq 1.0")));
    }

    @Test
    void test02() throws Exception {
        if (!OiyokanTestConstants.IS_TEST_ODATATEST)
            return;

        assertEquals("((Description = ?) AND (ID = 2.0))", getExprString("/ODataTests1", //
                BasicUrlUtil.encodeUrlQuery("$filter=Description eq 'Mac' and ID eq 2.0")));
    }

    @Test
    void test03() throws Exception {
        if (!OiyokanTestConstants.IS_TEST_ODATATEST)
            return;

        assertEquals("((INSTR(Description,?) - 1) <> ?)", getExprString("/ODataTests1", //
                BasicUrlUtil.encodeUrlQuery(
                        "$top=51&$filter= indexof(Description,'増殖タブレット7') ne -1 &$orderby=ID &$count=true &$select=Description,ID,Name")));
    }
}
