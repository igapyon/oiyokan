package jp.oiyokan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.junit.jupiter.api.Test;

class OiyoEdmUtilTest {

    @Test
    void test() {
        EdmPrimitiveType edmType = OiyoEdmUtil.string2EdmType("Edm.String");
        assertEquals("Edm.String", OiyoEdmUtil.edmType2String(edmType));
    }
}
