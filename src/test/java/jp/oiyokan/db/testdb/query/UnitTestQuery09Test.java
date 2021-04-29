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
class UnitTestQuery09Test {
    @Test
    void testSingle1() throws Exception {
        @SuppressWarnings("unused")
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();
        OiyoSettingsDatabase database = OiyoInfoUtil.getOiyoDatabaseByEntitySetName(oiyoInfo, "ODataTest1");
        OiyokanConstants.DatabaseType databaseType = OiyokanConstants.DatabaseType.valueOf(database.getType());

        final ODataResponse resp = OiyokanTestUtil.callGet("/ODataTest1",
                OiyoUrlUtil.encodeUrlQuery("$top=1 &$filter=Single1 eq 123.45 &$orderby=ID &$count=true &$select=ID"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());

        switch (databaseType) {
        default:
            assertEquals(
                    "{\"@odata.context\":\"$metadata#ODataTest1\",\"@odata.count\":204,\"value\":[{\"ID\":1,\"Single1\":123.45}]}",
                    result, "Single型の確認");
            assertEquals(200, resp.getStatusCode());
            break;
        case PostgreSQL:
            // Postgresでは確認しない。
            break;
        }
    }
}
