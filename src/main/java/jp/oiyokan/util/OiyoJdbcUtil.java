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
import java.util.Locale;

import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.OiyokanMessages;

/**
 * JDBCの超基本ユーティリティ
 */
public class OiyoJdbcUtil {
    /**
     * 与えられた java.sql.Types を文字列に変換.
     * 
     * @param types Value of java.sql.Types.
     * @return String java.sql.Types.
     * @throws ODataApplicationException OData app exception occured.
     */
    public static String types2String(final int types) throws ODataApplicationException {
        switch (types) {
        case Types.BIT: // -7
            return "Types.BIT";
        case Types.TINYINT: // -6
            return "Types.TINYINT";
        case Types.SMALLINT: // 5
            return "Types.SMALLINT";
        case Types.INTEGER: // 4
            return "Types.INT";
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
            // TODO 番号取り直し
            // [M021] NOT SUPPORTED: JDBC Type
            System.err.println(OiyokanMessages.M021 + ": " + types);
            throw new ODataApplicationException(OiyokanMessages.M021 + ": " + types, //
                    500, Locale.ENGLISH);
        }
    }
}
