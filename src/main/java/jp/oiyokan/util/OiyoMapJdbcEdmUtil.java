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
package jp.oiyokan.util;

import java.sql.Types;

import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.core.edm.primitivetype.EdmBinary;
import org.apache.olingo.commons.core.edm.primitivetype.EdmBoolean;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDate;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDateTimeOffset;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDecimal;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDouble;
import org.apache.olingo.commons.core.edm.primitivetype.EdmInt16;
import org.apache.olingo.commons.core.edm.primitivetype.EdmInt32;
import org.apache.olingo.commons.core.edm.primitivetype.EdmInt64;
import org.apache.olingo.commons.core.edm.primitivetype.EdmSByte;
import org.apache.olingo.commons.core.edm.primitivetype.EdmSingle;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.commons.core.edm.primitivetype.EdmTimeOfDay;

import jp.oiyokan.OiyokanMessages;

/**
 * JDBCの超基本ユーティリティ. 結構正しい範囲のメソッドここに記載する. Oiyokan の対応有無とは関わらず実装。
 */
public class OiyoMapJdbcEdmUtil {
    /**
     * 与えられた java.sql.Types を文字列に変換.
     * 
     * @param types Value of java.sql.Types.
     * @return String java.sql.Types.
     * @throws IllegalArgumentException Non supported value.
     */
    public static EdmPrimitiveType jdbcTypes2Edm(final int types) throws IllegalArgumentException {
        switch (types) {
        case Types.BIT: // -7
            return EdmBoolean.getInstance();
        case Types.TINYINT: // -6
            return EdmSByte.getInstance();
        case Types.SMALLINT: // 5
            return EdmInt16.getInstance();
        case Types.INTEGER: // 4
            return EdmInt32.getInstance();
        case Types.BIGINT: // -5
            return EdmInt64.getInstance();
        case Types.FLOAT: // 6
            // JDBC の FLOAT は DOUBLE に等しい
            return EdmDouble.getInstance();
        case Types.REAL: // 7
            return EdmSingle.getInstance();
        case Types.DOUBLE: // 8
            return EdmDouble.getInstance();
        case Types.NUMERIC: // 2
            return EdmDecimal.getInstance();
        case Types.DECIMAL: // 3
            return EdmDecimal.getInstance();
        case Types.CHAR: // 1
            return EdmString.getInstance();
        case Types.VARCHAR: // 12
            return EdmString.getInstance();
        case Types.LONGVARCHAR: // -1
            return EdmString.getInstance();
        case Types.DATE: // 91
            return EdmDate.getInstance();
        case Types.TIME: // 92
            return EdmTimeOfDay.getInstance();
        case Types.TIMESTAMP: // 93
            return EdmDateTimeOffset.getInstance();
        case Types.BINARY: // -2
            return EdmBinary.getInstance();
        case Types.VARBINARY: // -3
            return EdmBinary.getInstance();
        case Types.LONGVARBINARY: // -4
            return EdmBinary.getInstance();
        case Types.NULL: // 0
            // 特殊。NULLはnull。このため getInstance()は利用できず。
            // 対応不明
            break;
        case Types.OTHER: // 1111
            // 対応不明
            break;
        case Types.JAVA_OBJECT: // 2000
            // 対応不明
            break;
        case Types.DISTINCT: // 2001
            // 対応不明
            break;
        case Types.STRUCT: // 2002
            // 対応不明
            break;
        case Types.ARRAY: // 2003
            // 対応不明
            break;
        case Types.BLOB: // 2004
            return EdmBinary.getInstance();
        case Types.CLOB: // 2005
            return EdmString.getInstance();
        case Types.REF: // 2006
            // 対応不明
            break;
        case Types.DATALINK: // 70
            // 対応不明
            break;
        case Types.BOOLEAN: // 16
            return EdmBoolean.getInstance();
        case Types.ROWID: // -8
            // 対応不明
            break;
        case Types.NCHAR: // -15
            return EdmString.getInstance();
        case Types.NVARCHAR: // -9
            return EdmString.getInstance();
        case Types.LONGNVARCHAR: // -16
            return EdmString.getInstance();
        case Types.NCLOB: // 2011
            return EdmString.getInstance();
        case Types.SQLXML: // 2009
            // Stringでよいのかどうか知見なし.
            // 対応不明
            break;
        case Types.REF_CURSOR: // 2012
            // 対応不明
            break;
        case Types.TIME_WITH_TIMEZONE: // 2013
            // 対応不明
            break;
        case Types.TIMESTAMP_WITH_TIMEZONE: // 2014
            return EdmDateTimeOffset.getInstance();
        default:
            break;
        }

        // TODO 番号取り直し
        // [M021] NOT SUPPORTED: JDBC Type
        System.err.println(OiyokanMessages.IY7125 + ": " + types);
        throw new IllegalArgumentException(OiyokanMessages.IY7125 + ": " + types);
    }
}
