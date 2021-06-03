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

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanUnittestUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.common.OiyoUrlUtil;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * EntityのNULL多用の基本的なテスト.
 */
class UnitTestEntityNull01Test {
    private static final Log log = LogFactory.getLog(UnitTestEntityNull01Test.class);

    /**
     * CREATE + DELETE
     */
    @Test
    void test01() throws Exception {
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();
        final OiyoSettingsDatabase database = OiyoInfoUtil.getOiyoDatabaseByEntitySetName(oiyoInfo, "ODataTest1");
        final OiyokanConstants.DatabaseType databaseType = OiyokanConstants.DatabaseType.valueOf(database.getType());

        // INSERT + DELETE
        // 自動項目の ID もNULLで引き渡す。
        ODataResponse resp = OiyokanTestUtil.callPost("/ODataTest1", "{\n" //
                + "  \"ID\":null\n" //
                + "  , \"Name\":\"Name\"\n" //
                + "  , \"Description\":\"Null test\"\n" //
                + "  , \"Sbyte1\":null\n" //
                + "  , \"Int16a\":null\n" //
                + "  , \"Single1\":null\n" //
                + "  , \"Double1\":null\n" //
                + "  , \"Decimal1\":null\n" //
                + "}");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        log.trace("TRACE: " + result);
        final String idString = OiyokanTestUtil.getValueFromResultByKey(result, "ID");
        assertEquals(201, resp.getStatusCode(), "");

        resp = OiyokanTestUtil.callGet("/ODataTest1(" + idString + ")", null);
        assertEquals(200, resp.getStatusCode());

        resp = OiyokanTestUtil.callGet("/ODataTest1", OiyoUrlUtil.encodeUrlQuery(
                "$select=Sbyte1,Int16a,Int32a,Int64a,Decimal1,StringChar8,StringVar255,StringLongVar1,Clob1,Boolean1,Single1,Double1 &$filter=ID eq "
                        + idString));
        result = OiyokanTestUtil.stream2String(resp.getContent());
        switch (databaseType) {
        default:
            assertEquals(
                    "{\"@odata.context\":\"$metadata#ODataTest1\",\"value\":[{\"@odata.id\":\"http://localhost:8080/odata4.svc/ODataTest1("
                            + idString + ")\",\"ID\":" + idString
                            + ",\"Sbyte1\":null,\"Int16a\":null,\"Int32a\":2147483647,\"Int64a\":2147483647,\"Decimal1\":null,\"StringChar8\":\"CHAR_VAL\",\"StringVar255\":\"VARCHAR255\",\"StringLongVar1\":\"LONGVARCHAR\",\"Clob1\":\"CLOB\",\"Boolean1\":false,\"Single1\":null,\"Double1\":null}]}",
                    result);
            break;
        case MySQL:
            assertEquals(
                    "{\"@odata.context\":\"$metadata#ODataTest1\",\"value\":[{\"@odata.id\":\"http://localhost:8080/odata4.svc/ODataTest1("
                            + idString + ")\",\"ID\":" + idString
                            + ",\"Sbyte1\":null,\"Int16a\":null,\"Int32a\":2147483647,\"Int64a\":2147483647,\"Decimal1\":null,\"StringChar8\":\"CHAR_VAL\",\"StringVar255\":\"VARCHAR255\",\"StringLongVar1\":\"LONGVARCHAR\",\"Clob1\":null,\"Boolean1\":false,\"Single1\":null,\"Double1\":null}]}",
                    result);
            break;
        }

        // INFO: SQL single: SELECT
        // ID,Name,Description,,Binary1,VarBinary1,LongVarBinary1,Blob1
        // FROM ODataTest1 WHERE ID=?

        // DELETE
        resp = OiyokanTestUtil.callDelete("/ODataTest1(" + idString + ")");
        assertEquals(204, resp.getStatusCode());

        // NOT FOUND after DELETED
        resp = OiyokanTestUtil.callGet("/ODataTest1(" + idString + ")", null);
        assertEquals(404, resp.getStatusCode());
    }
}
