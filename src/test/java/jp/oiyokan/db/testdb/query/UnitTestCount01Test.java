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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataResponse;
import org.junit.jupiter.api.Test;

import jp.oiyokan.OiyokanUnittestUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoUrlUtil;
import jp.oiyokan.util.OiyokanTestUtil;

/**
 * テーブルレコード数確認。
 */
class UnitTestCount01Test {
    private static final Log log = LogFactory.getLog(UnitTestCount01Test.class);

    @Test
    void test() throws Exception {
        @SuppressWarnings("unused")
        final OiyoInfo oiyoInfo = OiyokanUnittestUtil.setupUnittestDatabase();

        log.info(getCount("ODataTest1"));
        log.info(getCount("ODataTest2"));
        log.info(getCount("ODataTest3"));
        log.info(getCount("ODataTest4"));
        log.info(getCount("ODataTest5"));
        log.info(getCount("ODataTest6"));
        log.info(getCount("ODataTest7"));
        log.info(getCount("ODataTest8"));
        log.info(getCount("ODataTest8Sub"));
        log.info(getCount("ODataTest8SubSub"));

        final ODataResponse resp = OiyokanTestUtil.callGet("/ODataTest2", OiyoUrlUtil.encodeUrlQuery("$count=true"));
        final String result = OiyokanTestUtil.stream2String(resp.getContent());
        log.info(result);
    }

    static String getCount(String entitySetName) throws Exception {
        ODataResponse resp = OiyokanTestUtil.callGet("/" + entitySetName, "$top=1 &$count=true");
        String result = OiyokanTestUtil.stream2String(resp.getContent());
        return OiyokanTestUtil.getValueFromResultByKey(result, "@odata.count");
    }
}
