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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.oiyokan.OiyokanMessages;

/**
 * JDBCの超基本ユーティリティ. 鉄板なメソッドのみここに記載する. Oiyokan の対応有無とは関わらず、数値文字変換を実施。
 */
public class OiyoJdbcUtil {
    private static final Log log = LogFactory.getLog(OiyoJdbcUtil.class);

    /**
     * 与えられた java.sql.Types を文字列に変換.
     * 
     * @param types Value of java.sql.Types.
     * @return String java.sql.Types.
     * @throws IllegalArgumentException Non supported value.
     */
    public static String types2String(final int types) throws IllegalArgumentException {
        switch (types) {
        case Types.BIT: // -7
            return "Types.BIT";
        case Types.TINYINT: // -6
            return "Types.TINYINT";
        case Types.SMALLINT: // 5
            return "Types.SMALLINT";
        case Types.INTEGER: // 4
            return "Types.INTEGER";
        case Types.BIGINT: // -5
            return "Types.BIGINT";
        case Types.FLOAT: // 6
            return "Types.FLOAT";
        case Types.REAL: // 7
            return "Types.REAL";
        case Types.DOUBLE: // 8
            return "Types.DOUBLE";
        case Types.NUMERIC: // 2
            return "Types.NUMERIC";
        case Types.DECIMAL: // 3
            return "Types.DECIMAL";
        case Types.CHAR: // 1
            return "Types.CHAR";
        case Types.VARCHAR: // 12
            return "Types.VARCHAR";
        case Types.LONGVARCHAR: // -1
            return "Types.LONGVARCHAR";
        case Types.DATE: // 91
            return "Types.DATE";
        case Types.TIME: // 92
            return "Types.TIME";
        case Types.TIMESTAMP: // 93
            return "Types.TIMESTAMP";
        case Types.BINARY: // -2
            return "Types.BINARY";
        case Types.VARBINARY: // -3
            return "Types.VARBINARY";
        case Types.LONGVARBINARY: // -4
            return "Types.LONGVARBINARY";
        case Types.NULL: // 0
            return "Types.NULL";
        case Types.OTHER: // 1111
            return "Types.OTHER";
        case Types.JAVA_OBJECT: // 2000
            return "Types.JAVA_OBJECT";
        case Types.DISTINCT: // 2001
            return "Types.DISTINCT";
        case Types.STRUCT: // 2002
            return "Types.STRUCT";
        case Types.ARRAY: // 2003
            return "Types.ARRAY";
        case Types.BLOB: // 2004
            return "Types.BLOB";
        case Types.CLOB: // 2005
            return "Types.CLOB";
        case Types.REF: // 2006
            return "Types.REF";
        case Types.DATALINK: // 70
            return "Types.DATALINK";
        case Types.BOOLEAN: // 16
            return "Types.BOOLEAN";
        case Types.ROWID: // -8
            return "Types.ROWID";
        case Types.NCHAR: // -15
            return "Types.NCHAR";
        case Types.NVARCHAR: // -9
            return "Types.NVARCHAR";
        case Types.LONGNVARCHAR: // -16
            return "Types.LONGNVARCHAR";
        case Types.NCLOB: // 2011
            return "Types.NCLOB";
        case Types.SQLXML: // 2009
            return "Types.SQLXML";
        case Types.REF_CURSOR: // 2012
            return "Types.REF_CURSOR";
        case Types.TIME_WITH_TIMEZONE: // 2013
            return "Types.TIME_WITH_TIMEZONE";
        case Types.TIMESTAMP_WITH_TIMEZONE: // 2014
            return "Types.TIMESTAMP_WITH_TIMEZONE";
        default:
            // [IY7154] NOT SUPPORTED: JDBC Type
            log.error(OiyokanMessages.IY7154 + ": " + types);
            throw new IllegalArgumentException(OiyokanMessages.IY7154 + ": " + types);
        }
    }

    /**
     * 与えられた Types.VARCHAR を java.sql.Types に変換.
     * 
     * @param typesString Value of Types.VARCHAR string.
     * @return java.sql.Types value.
     * @throws IllegalArgumentException Non supported value.
     */
    public static int string2Types(final String typesString) throws IllegalArgumentException {
        if ("Types.BIT".equals(typesString)) {
            return Types.BIT; // -7
        }
        if ("Types.TINYINT".equals(typesString)) {

            return Types.TINYINT; // -6
        }
        if ("Types.SMALLINT".equals(typesString)) {
            return Types.SMALLINT; // 5
        }
        if ("Types.INTEGER".equals(typesString)) {
            return Types.INTEGER; // 4
        }
        if ("Types.BIGINT".equals(typesString)) {
            return Types.BIGINT; // -5
        }
        if ("Types.FLOAT".equals(typesString)) {
            return Types.FLOAT; // 6
        }
        if ("Types.REAL".equals(typesString)) {
            return Types.REAL; // 7
        }
        if ("Types.DOUBLE".equals(typesString)) {
            return Types.DOUBLE; // 8
        }
        if ("Types.NUMERIC".equals(typesString)) {
            return Types.NUMERIC; // 2
        }
        if ("Types.DECIMAL".equals(typesString)) {
            return Types.DECIMAL; // 3
        }
        if ("Types.CHAR".equals(typesString)) {
            return Types.CHAR; // 1
        }
        if ("Types.VARCHAR".equals(typesString)) {
            return Types.VARCHAR; // 12
        }
        if ("Types.LONGVARCHAR".equals(typesString)) {
            return Types.LONGVARCHAR; // -1
        }
        if ("Types.DATE".equals(typesString)) {
            return Types.DATE; // 91
        }
        if ("Types.TIME".equals(typesString)) {
            return Types.TIME; // 92
        }
        if ("Types.TIMESTAMP".equals(typesString)) {
            return Types.TIMESTAMP; // 93
        }
        if ("Types.BINARY".equals(typesString)) {
            return Types.BINARY; // -2
        }
        if ("Types.VARBINARY".equals(typesString)) {
            return Types.VARBINARY; // -3
        }
        if ("Types.LONGVARBINARY".equals(typesString)) {
            return Types.LONGVARBINARY; // -4
        }
        if ("Types.NULL".equals(typesString)) {
            return Types.NULL; // 0
        }
        if ("Types.OTHER".equals(typesString)) {
            return Types.OTHER; // 1111
        }
        if ("Types.JAVA_OBJECT".equals(typesString)) {
            return Types.JAVA_OBJECT; // 2000
        }
        if ("Types.DISTINCT".equals(typesString)) {
            return Types.DISTINCT; // 2001
        }
        if ("Types.STRUCT".equals(typesString)) {
            return Types.STRUCT; // 2002
        }
        if ("Types.ARRAY".equals(typesString)) {
            return Types.ARRAY; // 2003
        }
        if ("Types.BLOB".equals(typesString)) {
            return Types.BLOB; // 2004
        }
        if ("Types.CLOB".equals(typesString)) {
            return Types.CLOB; // 2005
        }
        if ("Types.REF".equals(typesString)) {
            return Types.REF; // 2006
        }
        if ("Types.DATALINK".equals(typesString)) {
            return Types.DATALINK; // 70
        }
        if ("Types.BOOLEAN".equals(typesString)) {
            return Types.BOOLEAN; // 16
        }
        if ("Types.ROWID".equals(typesString)) {
            return Types.ROWID; // -8
        }
        if ("Types.NCHAR".equals(typesString)) {
            return Types.NCHAR; // -15
        }
        if ("Types.NVARCHAR".equals(typesString)) {
            return Types.NVARCHAR; // -9
        }
        if ("Types.LONGNVARCHAR".equals(typesString)) {
            return Types.LONGNVARCHAR; // -16
        }
        if ("Types.NCLOB".equals(typesString)) {
            return Types.NCLOB; // 2011
        }
        if ("Types.SQLXML".equals(typesString)) {
            return Types.SQLXML; // 2009
        }
        if ("Types.REF_CURSOR".equals(typesString)) {
            return Types.REF_CURSOR; // 2012
        }
        if ("Types.TIME_WITH_TIMEZONE".equals(typesString)) {
            return Types.TIME_WITH_TIMEZONE; // 2013
        }
        if ("Types.TIMESTAMP_WITH_TIMEZONE".equals(typesString)) {
            return Types.TIMESTAMP_WITH_TIMEZONE; // 2014
        }

        // [IY7153] NOT SUPPORTED: JDBC Type String.
        log.error(OiyokanMessages.IY7153 + ": " + typesString);
        throw new IllegalArgumentException(OiyokanMessages.IY7153 + ": " + typesString);
    }
}
