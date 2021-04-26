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

import jp.oiyokan.OiyokanUnittestUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * Entityの基本的なテスト.
 */
class UnitTestEntityPatchInsert02Test {
    /**
     * UPDATE対象がなく、PATCH(INSERT)しようとするけれど autoGenKeyしばりでエラー終了.
     */
    @Test
    void test01() throws Exception {
        @SuppressWarnings("unused")
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        final int NOT_EXISTS_ID = OiyokanTestUtil.getNextUniqueId();

        // INSERT (PATCH)
        // 存在しないのでINSERTになるケース.
        ODataResponse resp = OiyokanTestUtil.callRequestPatch("/ODataTests3(" + NOT_EXISTS_ID + ")", "{\n" //
                + "  \"Name\":\"Name2\",\n" //
                + "  \"Description\":\"Description2\"\n" + "}", false, true);
        @SuppressWarnings("unused")
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        // System.err.println(result);
        assertEquals(304, resp.getStatusCode());
    }
}
