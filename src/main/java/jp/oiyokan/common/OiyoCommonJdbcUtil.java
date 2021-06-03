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
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
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

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsProperty;
import jp.oiyokan.util.OiyoDateTimeUtil;
import jp.oiyokan.util.OiyoEdmUtil;
import jp.oiyokan.util.OiyoJdbcUtil;

/**
 * Oiyokan 関連の JDBC まわりユーティリティクラス.
 */
public class OiyoCommonJdbcUtil {
    private static final Log log = LogFactory.getLog(OiyoCommonJdbcUtil.class);

    private OiyoCommonJdbcUtil() {
    }

    /**
     * 指定データベースへのDB接続を取得します。
     * 
     * @param settingsDatabase データベース設定情報.
     * @return データベース接続.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static Connection getConnection(OiyoSettingsDatabase settingsDatabase) throws ODataApplicationException {
        Connection conn;
        // OData server 起動シーケンスにてドライバ存在チェックは既に実施済み.
        // Class.forName(settingsDatabase.getJdbcDriver());

        // [IY7171] DEBUG: DB connect
        log.debug(OiyokanMessages.IY7171 + ": " + settingsDatabase.getName() //
                + " (" + settingsDatabase.getDescription() + ")");

        try {
            if (settingsDatabase.getJdbcUser() == null || settingsDatabase.getJdbcUser().trim().length() == 0) {
                // User が指定ない場合はURLで接続.
                conn = DriverManager.getConnection(settingsDatabase.getJdbcUrl());
            } else {
                // User が指定ある場合は user と pass を利用.
                conn = DriverManager.getConnection(settingsDatabase.getJdbcUrl(), settingsDatabase.getJdbcUser(),
                        settingsDatabase.getJdbcPassPlain());
            }

            if (settingsDatabase.getAutoCommit() != null) {
                if (log.isDebugEnabled()) {
                    // [IY7181] DEBUG: JDBC: call setAutoCommit
                    log.debug(OiyokanMessages.IY7181 + ": " + settingsDatabase.getAutoCommit());
                }
                conn.setAutoCommit(settingsDatabase.getAutoCommit());
            }

            if (settingsDatabase.getTransactionIsolation() != null
                    && settingsDatabase.getTransactionIsolation().length() > 0) {
                // [IY7175] DEBUG: DB set connection transaction isolation.
                log.debug(OiyokanMessages.IY7175 + ": " + settingsDatabase.getTransactionIsolation());

                final int transactionIsolation = OiyoJdbcUtil
                        .string2TransactionIsolation(settingsDatabase.getTransactionIsolation());
                conn.setTransactionIsolation(transactionIsolation);
            }

            if (settingsDatabase.getInitSqlExec() != null && settingsDatabase.getInitSqlExec().trim().length() > 0) {
                // [IY7176] DEBUG: DB init sql exec.
                log.debug(OiyokanMessages.IY7176 + ": " + settingsDatabase.getInitSqlExec());

                try (PreparedStatement stmt = conn.prepareStatement(settingsDatabase.getInitSqlExec())) {
                    final boolean hasResultSet = stmt.execute();
                    if (hasResultSet) {
                        try (ResultSet rs = stmt.getResultSet()) {
                            ResultSetMetaData rsmeta = rs.getMetaData();
                            int columnCount = rsmeta.getColumnCount();
                            for (; rs.next();) {
                                log.trace("  row:");
                                for (int column = 1; column <= columnCount; column++) {
                                    log.trace("    " + rsmeta.getColumnName(column) + ": " + rs.getString(column));
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            // [M005] UNEXPECTED: データベースの接続に失敗:
            // しばらく待って再度トライしてください。しばらく経っても改善しない場合はIT部門に連絡してください
            log.error(OiyokanMessages.IY1501 + ": " + settingsDatabase.getName() + ": " + ex.toString(), ex);
            throw new ODataApplicationException(OiyokanMessages.IY1501 + ": " + settingsDatabase.getName(), //
                    500, Locale.ENGLISH);
        }

        return conn;
    }

    /**
     * SQL検索プレースホルダの文字列を生成します。
     * 
     * @param count プレースホルダ数。
     * @return プレースホルダ文字列。
     */
    public static String getQueryPlaceholderString(int count) {
        String queryPlaceholder = "";
        for (int col = 0; col < count; col++) {
            if (col != 0) {
                queryPlaceholder += ",";
            }
            queryPlaceholder += "?";
        }

        return queryPlaceholder;
    }

    /**
     * oiyokan-settings.json の Property 情報から CSDL Property 情報を取得.
     * 
     * CsdlEntityType 生成時にテーブル情報からプロパティを生成.
     * 
     * see:
     * https://olingo.apache.org/javadoc/odata4/org/apache/olingo/commons/api/edm/EdmPrimitiveType.html
     * 
     * @param oiyoProp Property setting info.
     * @return CsdlProperty 情報.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static CsdlProperty settingsProperty2CsdlProperty(OiyoSettingsProperty oiyoProp)
            throws ODataApplicationException {
        // 型名の正しさをチェック。
        // 正しくなければ例外。
        // チェック実施のみなので左辺がない。
        OiyoEdmUtil.string2EdmType(oiyoProp.getEdmType());

        final CsdlProperty csdlProperty = new CsdlProperty();
        csdlProperty.setName(oiyoProp.getName());
        csdlProperty.setType(oiyoProp.getEdmType());
        if (oiyoProp.getMaxLength() != null) {
            csdlProperty.setMaxLength(oiyoProp.getMaxLength());
        }
        if (oiyoProp.getNullable() == null) {
            // 指定なしは NULL許容.
            csdlProperty.setNullable(true);
        } else {
            // 指定ありは 指定の通りに.
            csdlProperty.setNullable(oiyoProp.getNullable());
        }
        if (oiyoProp.getPrecision() != null) {
            csdlProperty.setPrecision(oiyoProp.getPrecision());
        }
        if (oiyoProp.getScale() != null) {
            csdlProperty.setScale(oiyoProp.getScale());
        }
        if (oiyoProp.getDbDefault() != null) {
            csdlProperty.setDefaultValue(oiyoProp.getDbDefault());
        }

        return csdlProperty;
    }

    /**
     * 実際の EntityCollection 生成時に、ResultSet から Property を作成.
     * 
     * @param oiyoInfo  Oiyokan Information.
     * @param rset      ResultSet to process.
     * @param column    Column Number. 1 origin.
     * @param entitySet EntitySet information.
     * @param property  Property information.
     * @return 作成された Property.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     * @throws SQLException              SQL例外が発生した場合.
     */
    public static Property resultSet2Property(OiyoInfo oiyoInfo, ResultSet rset, int column,
            OiyoSettingsEntitySet entitySet, OiyoSettingsProperty property)
            throws ODataApplicationException, SQLException {
        OiyoSettingsProperty oiyoProp = null;
        for (OiyoSettingsProperty look : OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, entitySet.getName()).getEntityType()
                .getProperty()) {
            if (property.getName().equals(look.getName())) {
                oiyoProp = look;
                break;
            }
        }
        if (oiyoProp == null) {
            // [M041] Fail to find Property from DB name.
            log.error(OiyokanMessages.IY7123 + "EntitySet:" + entitySet.getName() + " Prop:" + property.getName());
            throw new ODataApplicationException(OiyokanMessages.IY7123 + "EntitySet:" + entitySet.getName() + " Prop:" //
                    + property.getName(), 500, Locale.ENGLISH);
        }

        final String propName = oiyoProp.getName();
        final String edmTypeName = oiyoProp.getEdmType();
        final EdmPrimitiveType edmType = OiyoEdmUtil.string2EdmType(edmTypeName);
        if (EdmSByte.getInstance() == edmType) {
            final byte value = rset.getByte(column);
            if (rset.wasNull()) {
                return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
            }
            return new Property(edmTypeName, propName, ValueType.PRIMITIVE, value);
        } else if (EdmByte.getInstance() == edmType) {
            // 符号なしのバイト. h2 database には該当なし.
            // Edm.Byteに相当する型がJavaにないので Shortで代替.
            final short value = rset.getShort(column);
            if (rset.wasNull()) {
                return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
            }
            return new Property(edmTypeName, propName, ValueType.PRIMITIVE, value);
        } else if (EdmInt16.getInstance() == edmType) {
            final short value = rset.getShort(column);
            if (rset.wasNull()) {
                return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
            }
            return new Property(edmTypeName, propName, ValueType.PRIMITIVE, value);
        } else if (EdmInt32.getInstance() == edmType) {
            final int value = rset.getInt(column);
            if (rset.wasNull()) {
                return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
            }
            return new Property(edmTypeName, propName, ValueType.PRIMITIVE, value);
        } else if (EdmInt64.getInstance() == edmType) {
            final long value = rset.getLong(column);
            if (rset.wasNull()) {
                return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
            }
            return new Property(edmTypeName, propName, ValueType.PRIMITIVE, value);
        } else if (EdmDecimal.getInstance() == edmType) {
            final BigDecimal value = rset.getBigDecimal(column);
            if (rset.wasNull()) {
                return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
            }
            return new Property(edmTypeName, propName, ValueType.PRIMITIVE, value);
        } else if (EdmBoolean.getInstance() == edmType) {
            final boolean value = rset.getBoolean(column);
            if (rset.wasNull()) {
                return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
            }
            return new Property(edmTypeName, propName, ValueType.PRIMITIVE, value);
        } else if (EdmSingle.getInstance() == edmType) {
            final float value = rset.getFloat(column);
            if (rset.wasNull()) {
                return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
            }
            return new Property(edmTypeName, propName, ValueType.PRIMITIVE, value);
        } else if (EdmDouble.getInstance() == edmType) {
            final double value = rset.getDouble(column);
            if (rset.wasNull()) {
                return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
            }
            return new Property(edmTypeName, propName, ValueType.PRIMITIVE, value);
        } else if (EdmDate.getInstance() == edmType) {
            final java.sql.Date value = rset.getDate(column);
            if (rset.wasNull()) {
                return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
            }
            return new Property(edmTypeName, propName, ValueType.PRIMITIVE, value);
        } else if (EdmDateTimeOffset.getInstance() == edmType) {
            final java.sql.Timestamp value = rset.getTimestamp(column);
            if (rset.wasNull()) {
                return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
            }
            return new Property(edmTypeName, propName, ValueType.PRIMITIVE, value);
        } else if (EdmTimeOfDay.getInstance() == edmType) {
            final java.sql.Time value = rset.getTime(column);
            if (rset.wasNull()) {
                return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
            }
            return new Property(edmTypeName, propName, ValueType.PRIMITIVE, value);
        } else if (EdmString.getInstance() == edmType) {
            String value = null;
            if (oiyoProp.getJdbcStream() != null && oiyoProp.getJdbcStream()) {
                try {
                    final Reader reader = rset.getCharacterStream(column);
                    if (rset.wasNull()) {
                        return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
                    }
                    if (reader != null) {
                        value = IOUtils.toString(reader);
                    }
                } catch (IOException ex) {
                    // [IY7107] UNEXPECTED: fail to read from CLOB
                    log.error(OiyokanMessages.IY7107 + ": " + property.getName() + ": " + ex.toString(), ex);
                    throw new ODataApplicationException(OiyokanMessages.IY7107 + ": " + property.getName(), //
                            500, Locale.ENGLISH);
                }
            } else {
                value = rset.getString(column);
                if (rset.wasNull()) {
                    return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
                }
            }
            if (oiyoProp.getLengthFixed() != null && oiyoProp.getLengthFixed() && oiyoProp.getMaxLength() != null) {
                if (value != null) {
                    // NULLではない場合は、固定長文字列。CHAR の後方に空白をFILL。
                    final int fixedLength = oiyoProp.getMaxLength();
                    value = StringUtils.rightPad(value, fixedLength);
                }
            }
            return new Property(edmTypeName, propName, ValueType.PRIMITIVE, value);
        } else if (EdmBinary.getInstance() == edmType) {
            try {
                final InputStream value = rset.getBinaryStream(column);
                if (rset.wasNull()) {
                    return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
                }
                return new Property(edmTypeName, propName, ValueType.PRIMITIVE, IOUtils.toByteArray(value));
            } catch (IOException ex) {
                // [M008] UNEXPECTED: fail to read from binary
                log.error(OiyokanMessages.IY7108 + ": " + property.getName() + ": " + ex.toString(), ex);
                throw new ODataApplicationException(OiyokanMessages.IY7108 + ": " + property.getName(), //
                        500, Locale.ENGLISH);
            }
        } else if (EdmGuid.getInstance() == edmType) {
            // Guid については UUID として読み込む。
            final Object obj = rset.getObject(column);
            if (rset.wasNull()) {
                return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
            }
            if (obj == null) {
                // UUID に null が与えられた場合、そのままnullをセット. (null対応)
                return new Property("Edm.Null", propName, ValueType.PRIMITIVE, null);
            } else if (obj instanceof java.util.UUID) {
                // h2 database で通過
                return new Property(edmTypeName, propName, ValueType.PRIMITIVE, (java.util.UUID) obj);
            } else if (obj instanceof String) {
                // SQL Server 2008 で通過
                java.util.UUID look = UUID.fromString((String) obj);
                return new Property(edmTypeName, propName, ValueType.PRIMITIVE, look);
            } else {
                // [M033] NOT SUPPORTED: unknown UUID object given
                log.error(OiyokanMessages.IY2106 + ": type[" + oiyoProp.getEdmType() + "], "
                        + obj.getClass().getCanonicalName());
                throw new ODataApplicationException(OiyokanMessages.IY2106 + ": type[" + oiyoProp.getEdmType() + "], "
                        + obj.getClass().getCanonicalName(), OiyokanMessages.IY2106_CODE, Locale.ENGLISH);
            }
        } else {
            // ARRAY と OTHER には対応しない。そもそもここ通過しないのじゃないの?
            // [M009] UNEXPECTED: missing impl
            log.error(OiyokanMessages.IY7109 + ": type[" + edmTypeName + "], " + property.getName());
            throw new ODataApplicationException(
                    OiyokanMessages.IY7109 + ": type[" + edmTypeName + "], " + property.getName(), //
                    500, Locale.ENGLISH);
        }
    }

    /**
     * リテラルまたはプレースホルダーをビルド.
     * 
     * @param sqlInfo     SQL info.
     * @param edmTypeName CSDL type.
     * @param inputParam  parameter text.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static void expandLiteralOrBindParameter(final OiyoSqlInfo sqlInfo, String edmTypeName,
            OiyoSettingsProperty property, Object inputParam) throws ODataApplicationException {
        if (inputParam == null) {
            log.trace("TRACE: expandLiteralOrBindParameter: null");
            sqlInfo.getSqlBuilder().append("?");
            sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, inputParam));
            return;
        }
        final EdmPrimitiveType edmType = OiyoEdmUtil.string2EdmType(edmTypeName);
        if (EdmSByte.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmSByte: " + "****"/* inputParam */);
            if (inputParam instanceof Byte //
                    || inputParam instanceof Short//
                    || inputParam instanceof Integer) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, inputParam));
            } else {
                // そのままSQL本文
                sqlInfo.getSqlBuilder().append(String.valueOf(inputParam));
            }
            return;
        }
        if (EdmByte.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmByte: " + "****"/* inputParam */);
            // 符号なしByteはJavaには該当する型がないので Shortで代用.
            if (inputParam instanceof Byte //
                    || inputParam instanceof Short//
                    || inputParam instanceof Integer) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, inputParam));
            } else {
                // そのままSQL本文
                sqlInfo.getSqlBuilder().append(String.valueOf(inputParam));
            }
            return;
        }
        if (EdmInt16.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmInt16: " + "****"/* inputParam */);
            if (inputParam instanceof Byte //
                    || inputParam instanceof Short//
                    || inputParam instanceof Integer) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, inputParam));
            } else {
                sqlInfo.getSqlBuilder().append("?");
                final String value = String.valueOf(inputParam);
                if ("null".equalsIgnoreCase(value)) {
                    // nullが文字列で渡ってくる場合でも正しく動作させる。
                    sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, (Short) null));
                } else {
                    sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, Short.valueOf(value)));
                }
            }
            return;
        }
        if (EdmInt32.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmInt32: " + "****"/* inputParam */);
            if (inputParam instanceof Byte //
                    || inputParam instanceof Short//
                    || inputParam instanceof Integer) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, inputParam));
            } else {
                sqlInfo.getSqlBuilder().append("?");
                final String value = String.valueOf(inputParam);
                if ("null".equalsIgnoreCase(value)) {
                    // nullが文字列で渡ってくる場合でも正しく動作させる。
                    sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, (Integer) null));
                } else {
                    sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, Integer.valueOf(value)));
                }
            }
            return;
        }
        if (EdmInt64.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmInt64: " + "****"/* inputParam */);
            if (inputParam instanceof Byte //
                    || inputParam instanceof Short //
                    || inputParam instanceof Integer //
                    || inputParam instanceof Long) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, inputParam));
            } else {
                sqlInfo.getSqlBuilder().append("?");
                final String value = String.valueOf(inputParam);
                if ("null".equalsIgnoreCase(value)) {
                    // nullが文字列で渡ってくる場合でも正しく動作させる。
                    sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, (Long) null));
                } else {
                    sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, Long.valueOf(value)));
                }
            }
            return;
        }
        if (EdmDecimal.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmDecimal: " + "****"/* inputParam */);
            if (inputParam instanceof BigDecimal) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, inputParam));
            } else {
                // 小数点付きの数値はパラメータとしては処理せずにそのまま文字列として連結.
                sqlInfo.getSqlBuilder().append(String.valueOf(inputParam));
            }
            return;
        }
        if (EdmBoolean.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmBoolean: " + "****"/* inputParam */);
            if (inputParam instanceof Boolean) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, (Boolean) inputParam));
            } else {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property,
                        Boolean.valueOf("true".equalsIgnoreCase(String.valueOf(inputParam)))));
            }
            return;
        }
        if (EdmSingle.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmSingle: " + "****"/* inputParam */);
            if (inputParam instanceof Float //
                    || inputParam instanceof Short//
                    || inputParam instanceof Integer) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, inputParam));
            } else {
                // 小数点付きの数値はパラメータとしては処理せずにそのまま文字列として連結.
                sqlInfo.getSqlBuilder().append(inputParam);
            }
            return;
        }
        if (EdmDouble.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmDouble: " + "****"/* inputParam */);
            if (inputParam instanceof Double //
                    || inputParam instanceof Short//
                    || inputParam instanceof Integer) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, inputParam));
            } else {
                // 小数点付きの数値はパラメータとしては処理せずにそのまま文字列として連結.
                sqlInfo.getSqlBuilder().append(inputParam);
            }
            return;
        }
        if (EdmDate.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmDate: " + "****"/* inputParam */);
            if (inputParam instanceof java.sql.Timestamp //
                    || inputParam instanceof java.sql.Date //
                    || inputParam instanceof java.util.Date//
                    || inputParam instanceof java.util.Calendar //
                    || inputParam instanceof TemporalAccessor) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, inputParam));
            } else {
                // ex: $filter の 2021-01-01 でここを通過.
                ZonedDateTime zdt = OiyoDateTimeUtil.parseStringDateTime(String.valueOf(inputParam));
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, zdt));
            }
            return;
        }
        if (EdmDateTimeOffset.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmDateTimeOffset: " + "****"/* inputParam */);
            if (inputParam instanceof java.sql.Timestamp //
                    || inputParam instanceof java.sql.Date //
                    || inputParam instanceof java.util.Date//
                    || inputParam instanceof java.util.Calendar //
                    || inputParam instanceof TemporalAccessor) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, inputParam));
            } else {
                // ex: $filter の 2020-12-31T21:53:00Z にてここを通過.
                ZonedDateTime zdt = OiyoDateTimeUtil.parseStringDateTime(String.valueOf(inputParam));
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, zdt));
            }
            return;
        }
        if (EdmTimeOfDay.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmTimeOfDay: " + "****"/* inputParam */);
            if (inputParam instanceof java.sql.Time) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, inputParam));
            } else if (inputParam instanceof java.util.Calendar) {
                java.util.Calendar cal = (java.util.Calendar) inputParam;
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add( //
                        new OiyoSqlInfo.SqlParam(property, OiyoJdbcUtil.toSqlTime(cal.getTime())));
            } else {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, //
                        OiyoDateTimeUtil.parseStringTime(String.valueOf(inputParam))));
            }
            return;
        }
        if (EdmString.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmString: " + "****"/* inputParam */);
            String value = String.valueOf(inputParam);
            if (value.startsWith("'") && value.endsWith("'")) {
                // 文字列リテラルについては前後のクオートを除去して記憶.
                value = value.substring(1, value.length() - 1);
            }

            if (property != null && property.getLengthFixed() != null && property.getLengthFixed()) {
                // CHAR 型の場合は rightPadを実施。
                value = StringUtils.rightPad(value, property.getMaxLength());
            }

            // 文字列リテラルとしてパラメータ化クエリで扱う.
            sqlInfo.getSqlBuilder().append("?");
            sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, value));
            return;
        }
        if (EdmBinary.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmBinary: " + "****"/* inputParam */);
            if (inputParam instanceof byte[] //
                    || inputParam instanceof ByteArrayInputStream) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, inputParam));
            } else {
                final byte[] look = new Base64().decode(String.valueOf(inputParam));
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, look));
            }
            return;
        }
        if (EdmGuid.getInstance() == edmType) {
            log.trace("TRACE: expandLiteralOrBindParameter: EdmGuid: " + "****"/* inputParam */);
            if (inputParam instanceof byte[] //
                    || inputParam instanceof ByteArrayInputStream) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, inputParam));
            } else {
                final String look = String.valueOf(inputParam);
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(new OiyoSqlInfo.SqlParam(property, look));
            }
            return;
        }

        // [M037] NOT SUPPORTED: Parameter Type
        log.error(OiyokanMessages.IY2108 + ": " + edmTypeName);
        throw new ODataApplicationException(OiyokanMessages.IY2108 + ": " + edmTypeName, //
                OiyokanMessages.IY2108_CODE, Locale.ENGLISH);
    }

    ////////////////////////////
    // 項目名に関するユーティリティ

    /**
     * かっこつき項目名のかっこを除去. これは OData API からの引き渡しの値にて発生.
     * 
     * @param escapedFieldName かっこ付き項目名.
     * @return かっこなし項目名.
     */
    public static String unescapeKakkoFieldName(String escapedFieldName) {
        String normalName = escapedFieldName;
        normalName = normalName.replaceAll("^\\[", "");
        normalName = normalName.replaceAll("\\]$", "");
        return normalName;
    }

    /**
     * 項目名のカッコをエスケープ付与.
     * 
     * @param sqlInfo   SQL設定情報.
     * @param fieldName 項目名.
     * @return 必要に応じてエスケープされた項目名.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static String escapeKakkoFieldName(OiyoSqlInfo sqlInfo, String fieldName) throws ODataApplicationException {
        final OiyokanConstants.DatabaseType databaseType = OiyoInfoUtil
                .getOiyoDatabaseTypeByEntitySetName(sqlInfo.getOiyoInfo(), sqlInfo.getEntitySetName());

        return escapeKakkoFieldName(databaseType, fieldName);
    }

    public static String escapeKakkoFieldName(OiyokanConstants.DatabaseType databaseType, String fieldName)
            throws ODataApplicationException {
        switch (databaseType) {
        case h2:
        case SQLSV2008:
            if (fieldName.indexOf(" ") <= 0 && fieldName.indexOf(".") <= 0) {
                // 空白のない場合はエスケープしない.
                return fieldName;
            }
            return "[" + fieldName + "]";

        case PostgreSQL:
        case ORCL18:
            if (fieldName.indexOf(" ") <= 0 && fieldName.indexOf(".") <= 0) {
                // 空白のない場合はエスケープしない.
                return fieldName;
            }
            return "\"" + fieldName + "\"";

        case MySQL:
        case BigQuery:
            if (fieldName.indexOf(" ") <= 0 && fieldName.indexOf(".") <= 0) {
                // 空白やピリオドのない場合はエスケープしない.
                return fieldName;
            }
            return "`" + fieldName + "`";

        default:
            // [M020] NOT SUPPORTED: Database type
            log.error(OiyokanMessages.IY7124 + ": " + databaseType);
            throw new ODataApplicationException(OiyokanMessages.IY7124 + ": " + databaseType, //
                    500, Locale.ENGLISH);
        }
    }

    ////////////////////////////
    // EXECUTE DML

    /**
     * 実行後、自動採集番された項目の値をreturnする。
     * 
     * @param connTargetDb 利用データベース接続.
     * @param sqlInfo      実行したいSQL情報.
     * @return (もしあれば)生成されたキーのリスト.
     * @throws ODataApplicationException
     */
    public static List<String> executeDml(Connection connTargetDb, OiyoSqlInfo sqlInfo, OiyoSettingsEntitySet entitySet,
            boolean returnGeneratedKeys) throws ODataApplicationException {
        final String sql = sqlInfo.getSqlBuilder().toString();
        // [IY1066] INFO: SQL exec
        log.info(OiyokanMessages.IY1066 + ": " + sql);

        final long startMillisec = System.currentTimeMillis();
        try (var stmt = connTargetDb.prepareStatement(sql, //
                (returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS))) {
            final int jdbcStmtTimeout = (entitySet.getJdbcStmtTimeout() == null ? 30 : entitySet.getJdbcStmtTimeout());
            stmt.setQueryTimeout(jdbcStmtTimeout);

            int idxColumn = 1;
            for (OiyoSqlInfo.SqlParam look : sqlInfo.getSqlParamList()) {
                OiyoCommonJdbcBindParamUtil.bindPreparedParameter(stmt, idxColumn++, look);
            }

            final int result = stmt.executeUpdate();
            if (result != 1) {
                // [IY3101] NO record processed. No Entity effects.
                log.warn(OiyokanMessages.IY3101 + ": " + sql);
                throw new ODataApplicationException(OiyokanMessages.IY3101 + ": " + sql, //
                        OiyokanMessages.IY3101_CODE, Locale.ENGLISH);
            }

            // 生成されたキーがあればそれを採用。
            final List<String> generatedKeys = new ArrayList<>();
            if (returnGeneratedKeys) {
                try (ResultSet rsKeys = stmt.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        final ResultSetMetaData rsmetaKeys = rsKeys.getMetaData();
                        for (int column = 1; column <= rsmetaKeys.getColumnCount(); column++) {
                            generatedKeys.add(rsKeys.getString(column));
                        }
                    }
                }
            }

            final long endMillisec = System.currentTimeMillis();
            final long elapsed = endMillisec - startMillisec;
            if (elapsed >= 10) {
                // [IY1067] INFO: SQL exec: elapsed
                log.info(OiyokanMessages.IY1067 + ": " + (endMillisec - startMillisec));
            }

            return generatedKeys;
        } catch (SQLIntegrityConstraintViolationException ex) {
            // [IY3401] Integrity constraint violation occured (DML). 制約違反.
            log.error(OiyokanMessages.IY3401 + ": " + sql + ", " + ex.toString());
            // 制約違反については例外的に ex の getMessage についても呼出元に返却.
            throw new ODataApplicationException(OiyokanMessages.IY3401 + ": " + sql + ": " + ex.getMessage(), //
                    OiyokanMessages.IY3401_CODE, Locale.ENGLISH);
        } catch (SQLTimeoutException ex) {
            // [IY3511] SQL timeout at exec insert/update/delete.
            log.error(OiyokanMessages.IY3511 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.IY3511 + ": " + sql, //
                    OiyokanMessages.IY3511_CODE, Locale.ENGLISH);
        } catch (SQLException ex) {
            if (ex.toString().toLowerCase().contains("unique constraint")/* PostgreSQL */ //
                    || ex.toString().toLowerCase().contains("重複したキー")/* SQLSV2008 */) {
                // [IY3402] Integrity constraint violation occured (DML). 制約違反.
                log.error(OiyokanMessages.IY3402 + ": " + sql + ", " + ex.toString());
                // 制約違反については例外的に ex の getMessage についても呼出元に返却.
                throw new ODataApplicationException(OiyokanMessages.IY3402 + ": " + sql + ": " + ex.getMessage(), //
                        OiyokanMessages.IY3402_CODE, Locale.ENGLISH);
            } else if (ex.toString().toLowerCase().contains("timed out") /* SQL Server 2008 */ ) {
                // [IY3512] SQL timeout at exec insert/update/delete.
                log.error(OiyokanMessages.IY3512 + ": " + sql + ", " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.IY3512 + ": " + sql, //
                        OiyokanMessages.IY3512_CODE, Locale.ENGLISH);
            } else {
                ex.printStackTrace();
                // [IY3151] Fail to execute SQL.
                log.error(OiyokanMessages.IY3151 + ": " + sql + ", " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.IY3151 + ": " + sql, //
                        OiyokanMessages.IY3151_CODE, Locale.ENGLISH);
            }
        }
    }
}
