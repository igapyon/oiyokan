package jp.oiyokan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.commons.core.edm.primitivetype.SingletonPrimitiveType;
import org.junit.jupiter.api.Test;

class OiyoEdmUtilTest {

    @Test
    void test() {
        SingletonPrimitiveType edmType = OiyoEdmUtil.string2EdmType("Edm.String");
        assertEquals("Edm.String", OiyoEdmUtil.edmType2String(edmType));
    }
}
