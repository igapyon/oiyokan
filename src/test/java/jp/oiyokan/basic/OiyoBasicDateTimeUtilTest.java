package jp.oiyokan.basic;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

/**
 * DateTimeに関するテスト。
 */
class OiyoBasicDateTimeUtilTest {

    @Test
    void test() {
        ZonedDateTime zdt = OiyoBasicDateTimeUtil.parseStringDateTime("2021-04-10T19:12:49.082Z");
        // System.err.println(zdt.toString());
        
        zdt = OiyoBasicDateTimeUtil.parseStringDateTime("2021-04-10 19:12:49.082Z");
        // System.err.println(zdt.toString());

        // TODO FIXME 時間がずれる!?
        zdt = OiyoBasicDateTimeUtil.parseStringDateTime("2021-04-10T19:12:49.082587");
        // System.err.println(zdt.toString());

        zdt = OiyoBasicDateTimeUtil.parseStringDateTime("2021-04-10T19:12:49.082587Z");
        // System.err.println(zdt.toString());

        zdt = OiyoBasicDateTimeUtil.parseStringDateTime("2021-04-10T19:12:49Z");
        // System.err.println(zdt.toString());

        zdt = OiyoBasicDateTimeUtil.parseStringDateTime("2021-04-10");
        // System.err.println(zdt.toString());
    }
}
