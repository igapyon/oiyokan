package jp.oiyokan.basic;

import org.junit.jupiter.api.Test;

class BasicDateTimeUtilTest {

    @Test
    void test() {
        BasicDateTimeUtil.parseStringDateTime("2021-04-10 19:12:49.082587");

        BasicDateTimeUtil.parseStringDateTime("1970-01-01T10:12:49Z");
    }

}
