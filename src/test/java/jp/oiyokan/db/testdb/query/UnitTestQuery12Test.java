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
package jp.oiyokan.db.testdb.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
 * フィルタの型に着眼したテスト.
 */
class UnitTestQuery12Test {
    @Test
    void testStringVar255WithAndOr() throws Exception {
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        OiyoSettingsDatabase database = OiyoInfoUtil.getOiyoDatabaseByEntitySetName(oiyoInfo, "ODataTests1");
        OiyokanConstants.DatabaseType databaseType = OiyokanConstants.DatabaseType.valueOf(database.getType());
        switch (databaseType) {
        case SQLSV2008:
        case ORCL18:
            return;
        default:
            // テスト処理します。
            break;
        }

        final ODataResponse resp = OiyokanTestUtil.callRequestGetResponse( //
                "/ODataTests1", OiyoUrlUtil.encodeUrlQuery(
                        "&$filter=StringVar255 eq 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' or StringVar255 eq 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' and StringLongVar1 eq 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' or Clob1 eq 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' &$count=true &$select=ID"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        // System.err.println("result: " + result);
        assertEquals("{\"@odata.context\":\"$metadata#ODataTests1\",\"@odata.count\":1,\"value\":[{\"ID\":204}]}",
                result, "SQLSV2008でエラー(既知の問題), ORCL18でエラー(既知の問題)");
        assertEquals(200, resp.getStatusCode());
    }
}
