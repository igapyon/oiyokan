package jp.oiyokan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Types;

import org.junit.jupiter.api.Test;

class OiyoJdbcUtilTest {

    private static final int[] TYPES = { //
            Types.BIT // -7
            , Types.TINYINT // -6
            , Types.SMALLINT // 5
            , Types.INTEGER // 4
            , Types.BIGINT // -5
            , Types.FLOAT // 6
            , Types.REAL // 7
            , Types.DOUBLE // 8
            , Types.NUMERIC // 2
            , Types.DECIMAL // 3
            , Types.CHAR // 1
            , Types.VARCHAR // 12
            , Types.LONGVARCHAR // -1
            , Types.DATE // 91
            , Types.TIME // 92
            , Types.TIMESTAMP // 93
            , Types.BINARY // -2
            , Types.VARBINARY // -3
            , Types.LONGVARBINARY // -4
            , Types.NULL // 0
            , Types.OTHER // 1111
            , Types.JAVA_OBJECT // 2000
            , Types.DISTINCT // 2001
            , Types.STRUCT // 2002
            , Types.ARRAY // 2003
            , Types.BLOB // 2004
            , Types.CLOB // 2005
            , Types.REF // 2006
            , Types.DATALINK // 70
            , Types.BOOLEAN // 16
            , Types.ROWID // -8
            , Types.NCHAR // -15
            , Types.NVARCHAR // -9
            , Types.LONGNVARCHAR // -16
            , Types.NCLOB // 2011
            , Types.SQLXML // 2009
            , Types.REF_CURSOR // 2012
            , Types.TIME_WITH_TIMEZONE // 2013
            , Types.TIMESTAMP_WITH_TIMEZONE // 2014
    };

    @Test
    void test() {
        for (int origin : TYPES) {
            String val = OiyoJdbcUtil.types2String(origin);
            int result = OiyoJdbcUtil.string2Types(val);
            assertEquals(origin, result);
        }
    }

}
