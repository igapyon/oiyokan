package jp.oiyokan.basic;

import org.junit.jupiter.api.Test;

/**
 * DateTimeに関するテスト。
 */
class OiyoBasicDateTimeUtilTest {

    @Test
    void test() {
        OiyoBasicDateTimeUtil.parseStringDateTime("2021-04-10 19:12:49.082587");

        OiyoBasicDateTimeUtil.parseStringDateTime("1970-01-01T10:12:49Z");
    }

}
