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
package jp.oiyokan.common;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.ZonedDateTime;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.core.edm.primitivetype.EdmBinary;
import org.apache.olingo.commons.core.edm.primitivetype.EdmBoolean;
import org.apache.olingo.commons.core.edm.primitivetype.EdmByte;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDate;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDateTimeOffset;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDecimal;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDouble;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGuid;
import org.apache.olingo.commons.core.edm.primitivetype.EdmInt16;
import org.apache.olingo.commons.core.edm.primitivetype.EdmInt32;
import org.apache.olingo.commons.core.edm.primitivetype.EdmInt64;
import org.apache.olingo.commons.core.edm.primitivetype.EdmSByte;
import org.apache.olingo.commons.core.edm.primitivetype.EdmSingle;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.commons.core.edm.primitivetype.EdmTimeOfDay;
import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.dto.OiyoSettingsProperty;
import jp.oiyokan.util.OiyoDateTimeUtil;
import jp.oiyokan.util.OiyoEdmUtil;
import jp.oiyokan.util.OiyoJdbcUtil;

/**
 * Oiyokan 関連の JDBC パラメータバインドのユーティリティクラス. ログの制御をこのクラス単位で与えたたいため分離。
 */
public class OiyoCommonJdbcBindParamUtil {
    private static final Log log = LogFactory.getLog(OiyoCommonJdbcBindParamUtil.class);

    private OiyoCommonJdbcBindParamUtil() {
    }

    ////////////////////////////////////////
    // このクラスは、当面メソッドは1つの予定

    /**
     * PreparedStatement のバインドパラメータを設定.
     * 
     * @param stmt   PreparedStatement のインスタンス.
     * @param column 項目番号. 1オリジン.
     * @param param  セットしたい値.
     * @throws SQLException              SQL例外が発生した場合.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static void bindPreparedParameter(PreparedStatement stmt, int column, OiyoSqlInfo.SqlParam param)
            throws ODataApplicationException, SQLException {
        if (param == null) {
            // [IY7162] WARN: bind NULL value to an SQL statement, set Types.NULL because
            // there is no type information.
            log.warn(OiyokanMessages.IY7162);
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column
                    + ") : PreparedStatement#setNull: Types.NULL");
            stmt.setNull(column, Types.NULL);
            return;
        }
        if (null == param.getValue()) {
            log.trace("TRACE: PreparedStatement#setNull: null with Type");
            if (null == param.getProperty()) {
                // [IY7163] WARN: bind NULL value to an SQL statement, set Types.NULL because
                // there is no property object information.
                log.warn(OiyokanMessages.IY7163);
                log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column
                        + ") : PreparedStatement#setNull: property==null");
                stmt.setNull(column, Types.NULL);
                return;
            }
            if (null == param.getProperty().getDbType()) {
                // [IY7164] WARN: bind NULL value to an SQL statement, set Types.NULL because
                // there is no JDBC Type info in property information.
                log.warn(OiyokanMessages.IY7164);
                log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column
                        + ") : PreparedStatement#setNull: property.dbType==null");
                stmt.setNull(column, Types.NULL);
                return;
            } else {
                final int jdbcType = OiyoJdbcUtil.string2Types(param.getProperty().getJdbcType());
                // oiyokan-settings.json に設定された JDBC型をもちいてNULL設定する。
                log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column
                        + ") : PreparedStatement#setNull: " + param.getProperty().getJdbcType());
                stmt.setNull(column, jdbcType);
                return;
            }
        }

        if (param.getProperty() != null) {
            final OiyoSettingsProperty property = param.getProperty();

            // property 指定ありの場合.
            log.trace("TRACE: OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                    + property.getEdmType() + ")");
            final EdmPrimitiveType edmType = OiyoEdmUtil.string2EdmType(property.getEdmType());
            if (EdmSByte.getInstance() == edmType) {
                if (param.getValue() instanceof Byte) {
                    log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                            + property.getEdmType() + ") : setByte");
                    stmt.setByte(column, (Byte) param.getValue());
                    return;
                }
            }
            if (EdmByte.getInstance() == edmType /* Edm.Byteに相当する型がJavaにないので Shortで代替と想定 */ //
                    || EdmInt16.getInstance() == edmType //
                    || EdmInt32.getInstance() == edmType //
                    || EdmInt64.getInstance() == edmType) {
                if (param.getValue() instanceof Short) {
                    log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                            + property.getEdmType() + ") : setShort");
                    stmt.setShort(column, (Short) param.getValue());
                    return;
                }
                if (param.getValue() instanceof Integer) {
                    log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                            + property.getEdmType() + ") : setInt");
                    stmt.setInt(column, (Integer) param.getValue());
                    return;
                }
                if (param.getValue() instanceof Long) {
                    log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                            + property.getEdmType() + ") : setLong");
                    stmt.setLong(column, (Long) param.getValue());
                    return;
                }
            }
            if (EdmDecimal.getInstance() == edmType) {
                if (param.getValue() instanceof BigDecimal) {
                    log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                            + property.getEdmType() + ") : setBigDecimal");
                    stmt.setBigDecimal(column, (BigDecimal) param.getValue());
                    return;
                }
            }
            if (EdmBoolean.getInstance() == edmType) {
                if (param.getValue() instanceof Boolean) {
                    log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                            + property.getEdmType() + ") : setBoolean");
                    stmt.setBoolean(column, (Boolean) param.getValue());
                    return;
                }
                // TODO v1.x BIT型の対応状況確認
            }
            if (EdmSingle.getInstance() == edmType) {
                if (param.getValue() instanceof Float) {
                    log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                            + property.getEdmType() + ") : setFloat");
                    stmt.setFloat(column, (Float) param.getValue());
                    return;
                }
            }
            if (EdmDouble.getInstance() == edmType) {
                if (param.getValue() instanceof Double) {
                    log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                            + property.getEdmType() + ") : setDouble");
                    stmt.setDouble(column, (Double) param.getValue());
                    return;
                }
            }
            if (EdmDate.getInstance() == edmType) {
                if (param.getValue() instanceof java.util.Calendar) {
                    java.util.Calendar cal = (java.util.Calendar) param.getValue();
                    log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                            + property.getEdmType() + ") : setDate");
                    stmt.setDate(column, OiyoJdbcUtil.toSqlDate(cal.getTime()));
                    return;
                }
                if (param.getValue() instanceof java.time.ZonedDateTime) {
                    final ZonedDateTime zdt = (java.time.ZonedDateTime) param.getValue();
                    java.util.Date look = OiyoDateTimeUtil.zonedDateTime2Date(zdt);
                    log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                            + property.getEdmType() + ") : setDate");
                    stmt.setDate(column, OiyoJdbcUtil.toSqlDate(look));
                    return;
                }
            }
            if (EdmDateTimeOffset.getInstance() == edmType) {
                if (param.getValue() instanceof java.sql.Timestamp) {
                    log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                            + property.getEdmType() + ") : setTimestamp");
                    stmt.setTimestamp(column, (java.sql.Timestamp) param.getValue());
                    return;
                }
                if (param.getValue() instanceof java.time.ZonedDateTime) {
                    final ZonedDateTime zdt = (java.time.ZonedDateTime) param.getValue();
                    java.util.Date look = OiyoDateTimeUtil.zonedDateTime2Date(zdt);
                    log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                            + property.getEdmType() + ") : setTimestamp");
                    stmt.setTimestamp(column, OiyoJdbcUtil.toSqlTimestamp(look));
                    return;
                }
            }
            if (EdmTimeOfDay.getInstance() == edmType) {
                if (param.getValue() instanceof java.sql.Time) {
                    log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                            + property.getEdmType() + ") : setTime");
                    stmt.setTime(column, (java.sql.Time) param.getValue());
                    return;
                }
            }
            if (EdmString.getInstance() == edmType) {
                if (param.getValue() instanceof String) {
                    final String value = (String) param.getValue();
                    if (property.getJdbcStream() != null && property.getJdbcStream()) {
                        final StringReader reader = new StringReader(value);
                        log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                                + property.getEdmType() + ") : setCharacterStream");
                        stmt.setCharacterStream(column, reader);
                        return;
                    } else {
                        log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                                + property.getEdmType() + ") : setString");
                        stmt.setString(column, value);
                        return;
                    }
                }
            }
            if (EdmBinary.getInstance() == edmType) {
                if (param.getValue() instanceof byte[]) {
                    final byte[] value = (byte[]) param.getValue();
                    if (property.getJdbcStream() != null && property.getJdbcStream()) {
                        final ByteArrayInputStream inStream = new ByteArrayInputStream(value);
                        log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                                + property.getEdmType() + ") : setBinaryStream");
                        stmt.setBinaryStream(column, inStream);
                        return;
                    } else {
                        log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ", "
                                + property.getEdmType() + ") : setBytes");
                        stmt.setBytes(column, value);
                        return;
                    }
                }
            }
            if (EdmGuid.getInstance() == edmType) {
                // TODO GUID は v2.xで対応
            }

            // [IY1111] WARN: A literal associated with property was given but could not be
            // processed.
            log.warn(OiyokanMessages.IY1111 + ": Edm:" + property.getEdmType() + ", class:"
                    + param.getValue().getClass().getName());
        }

        ////////////////////////////////////
        // ここからは property に依存しない処理.

        if (param.getValue() instanceof Byte) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ") : setByte");
            stmt.setByte(column, (Byte) param.getValue());
        } else if (param.getValue() instanceof Short) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ") : setShort");
            stmt.setShort(column, (Short) param.getValue());
        } else if (param.getValue() instanceof Integer) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ") : setInt");
            stmt.setInt(column, (Integer) param.getValue());
        } else if (param.getValue() instanceof Long) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ") : setLong");
            stmt.setLong(column, (Long) param.getValue());
        } else if (param.getValue() instanceof BigDecimal) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ") : setBigDecimal");
            // Oiyokan では 小数点は基本的にリテラルのまま残すため、このコードは通過しない.
            stmt.setBigDecimal(column, (BigDecimal) param.getValue());
        } else if (param.getValue() instanceof Boolean) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ") : setBoolean");
            stmt.setBoolean(column, (Boolean) param.getValue());
        } else if (param.getValue() instanceof Float) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ") : setFloat");
            // Oiyokan では 小数点は基本的にリテラルのまま残すため、このコードは通過しない.
            stmt.setFloat(column, (Float) param.getValue());
        } else if (param.getValue() instanceof Double) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ") : setDouble");
            // Oiyokan では 小数点は基本的にリテラルのまま残すため、このコードは通過しない.
            stmt.setDouble(column, (Double) param.getValue());
        } else if (param.getValue() instanceof java.sql.Time) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ") : setTime");
            // java.util.Dateより先に記載が必要
            java.sql.Time look = (java.sql.Time) param.getValue();
            stmt.setTime(column, look);
        } else if (param.getValue() instanceof java.sql.Timestamp) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ") : setTimestamp");
            // java.util.Dateより先に記載が必要
            java.sql.Timestamp look = (java.sql.Timestamp) param.getValue();
            stmt.setTimestamp(column, look);
        } else if (param.getValue() instanceof java.sql.Date) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ") : setDate");
            // java.util.Dateより先に記載が必要
            java.sql.Date look = (java.sql.Date) param.getValue();
            stmt.setDate(column, look);
        } else if (param.getValue() instanceof java.util.Date) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column
                    + ") : setTimestamp : java.util.Date");
            java.util.Date udate = (java.util.Date) param.getValue();
            // Date か Timestamp 判別できないため、情報量の多い Date を利用.
            stmt.setTimestamp(column, OiyoJdbcUtil.toSqlTimestamp(udate));
        } else if (param.getValue() instanceof java.util.Calendar) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column
                    + ") : setTimestamp : java.util.Calendar");
            java.util.Calendar cal = (java.util.Calendar) param.getValue();
            // Date か Timestamp 判別できないため、情報量の多い Date を利用.
            stmt.setTimestamp(column, OiyoJdbcUtil.toSqlTimestamp(cal.getTime()));
        } else if (param.getValue() instanceof ZonedDateTime) {
            // ex: $filter で lt 2020-12-31T21:53:00Z により発生。
            log.debug(
                    "OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ") : setTimestamp : ZonedDateTime");
            ZonedDateTime zdt = (ZonedDateTime) param.getValue();
            java.util.Date look = OiyoDateTimeUtil.zonedDateTime2Date(zdt);
            stmt.setTimestamp(column, OiyoJdbcUtil.toSqlTimestamp(look));
        } else if (param.getValue() instanceof String) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ") : setString");
            // property 情報がないため、setString のみ選択可能
            stmt.setString(column, (String) param.getValue());
        } else if (param.getValue() instanceof byte[]) {
            log.debug("OiyoCommonJdbcBindParamUtil#bindPreparedParameter(" + column + ") : setBytes");
            byte[] look = (byte[]) param.getValue();
            stmt.setBytes(column, look);
        } else {
            // [IY1101] NOT SUPPORTED: Parameter Type
            log.fatal(OiyokanMessages.IY1101 + ": " + param.getClass().getCanonicalName());
            throw new ODataApplicationException(OiyokanMessages.IY1101 + ": " + param.getClass().getCanonicalName(), //
                    OiyokanMessages.IY1101_CODE, Locale.ENGLISH);
        }
    }

    ////////////////////////////////////////
    // このクラスは、当面メソッドは1つの予定
}
