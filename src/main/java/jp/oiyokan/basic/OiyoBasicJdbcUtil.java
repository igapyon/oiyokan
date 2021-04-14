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
package jp.oiyokan.basic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.sql.Types;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.util.StreamUtils;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanCsdlEntitySet;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.basic.sql.OiyoSqlInfo;
import jp.oiyokan.dto.OiyokanSettingsDatabase;
import jp.oiyokan.settings.OiyokanNamingUtil;

/**
 * Oiyokan 関連の JDBC まわりユーティリティクラス.
 */
public class OiyoBasicJdbcUtil {
    private OiyoBasicJdbcUtil() {
    }

    /**
     * 指定データベースへのDB接続を取得します。
     * 
     * @param settingsDatabase データベース設定情報.
     * @return データベース接続.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static Connection getConnection(OiyokanSettingsDatabase settingsDatabase) throws ODataApplicationException {
        Connection conn;
        // OData server 起動シーケンスにてドライバ存在チェックは既に実施済み.
        // Class.forName(settingsDatabase.getJdbcDriver());

        System.err.println("TRACE: DEBUG: DB connect: " + settingsDatabase.getName() + " ("
                + settingsDatabase.getDescription() + ")");

        try {
            if (settingsDatabase.getJdbcUser() == null || settingsDatabase.getJdbcUser().trim().length() == 0) {
                // User が指定ない場合はURLで接続.
                conn = DriverManager.getConnection(settingsDatabase.getJdbcUrl());
            } else {
                // User が指定ある場合は user と pass を利用.
                conn = DriverManager.getConnection(settingsDatabase.getJdbcUrl(), settingsDatabase.getJdbcUser(),
                        settingsDatabase.getJdbcPass());
            }
            // TRANSACTION_READ_COMMITTED を設定.
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        } catch (SQLException ex) {
            // [M005] UNEXPECTED: データベースの接続に失敗:
            // しばらく待って再度トライしてください。しばらく経っても改善しない場合はIT部門に連絡してください
            System.err.println(OiyokanMessages.M005 + ": " + settingsDatabase.getName() + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M005 + ": " + settingsDatabase.getName(), //
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
     * CsdlEntityType 生成時にテーブル情報からプロパティを生成.
     * 
     * see:
     * https://olingo.apache.org/javadoc/odata4/org/apache/olingo/commons/api/edm/EdmPrimitiveType.html
     * 
     * @param rsmeta ResultSetMetaDataインスタンス.
     * @param column 項目番号.
     * @return CsdlProperty 情報.
     * @throws SQLException              SQL例外が発生した場合.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static CsdlProperty resultSetMetaData2CsdlProperty(ResultSetMetaData rsmeta, int column)
            throws ODataApplicationException, SQLException {
        // DB上の名称直接ではなく命名ユーティリティを通過させてから処理.
        final CsdlProperty csdlProp = new CsdlProperty()
                .setName(OiyokanNamingUtil.db2Entity(rsmeta.getColumnName(column)));
        switch (rsmeta.getColumnType(column)) {
        case Types.TINYINT:
            csdlProp.setType(EdmPrimitiveTypeKind.SByte.getFullQualifiedName());
            break;
        case Types.SMALLINT:
            csdlProp.setType(EdmPrimitiveTypeKind.Int16.getFullQualifiedName());
            break;
        case Types.INTEGER: /* INT */
            csdlProp.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            break;
        case Types.BIGINT:
            csdlProp.setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
            break;
        case Types.DECIMAL:
            csdlProp.setType(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName());
            csdlProp.setScale(rsmeta.getScale(column));
            csdlProp.setPrecision(rsmeta.getPrecision(column));
            break;
        case Types.NUMERIC:
            // postgres で発生.
            csdlProp.setType(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName());
            csdlProp.setScale(rsmeta.getScale(column));
            csdlProp.setPrecision(rsmeta.getPrecision(column));
            break;
        case Types.BOOLEAN:
            csdlProp.setType(EdmPrimitiveTypeKind.Boolean.getFullQualifiedName());
            break;
        case Types.BIT:
            // postgres / SQL Server で発生.
            csdlProp.setType(EdmPrimitiveTypeKind.Boolean.getFullQualifiedName());
            break;
        case Types.REAL:
            csdlProp.setType(EdmPrimitiveTypeKind.Single.getFullQualifiedName());
            break;
        case Types.DOUBLE:
            csdlProp.setType(EdmPrimitiveTypeKind.Double.getFullQualifiedName());
            break;
        case Types.DATE:
            csdlProp.setType(EdmPrimitiveTypeKind.Date.getFullQualifiedName());
            break;
        case Types.TIMESTAMP:
            csdlProp.setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName());
            break;
        case Types.TIME:
            csdlProp.setType(EdmPrimitiveTypeKind.TimeOfDay.getFullQualifiedName());
            break;
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.LONGNVARCHAR:
        case Types.CLOB:
            csdlProp.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            if (rsmeta.getColumnDisplaySize(column) > 0 && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                csdlProp.setMaxLength(rsmeta.getColumnDisplaySize(column));
            }
            break;
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
        case Types.BLOB:
            if ("UUID".equalsIgnoreCase(rsmeta.getColumnTypeName(column))) {
                csdlProp.setType(EdmPrimitiveTypeKind.Guid.getFullQualifiedName());
            } else {
                csdlProp.setType(EdmPrimitiveTypeKind.Binary.getFullQualifiedName());
                if (rsmeta.getColumnDisplaySize(column) > 0
                        && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                    csdlProp.setMaxLength(rsmeta.getColumnDisplaySize(column));
                }
            }
            break;
        default:
            // [M006] NOT SUPPORTED: CSDL: JDBC Type
            System.err.println(OiyokanMessages.M006 + ": " + rsmeta.getColumnType(column));
            throw new ODataApplicationException(OiyokanMessages.M006 + ": " + rsmeta.getColumnType(column), //
                    500, Locale.ENGLISH);
        }

        // NULL許容かどうか。不明な場合は設定しない。
        switch (rsmeta.isNullable(column)) {
        case ResultSetMetaData.columnNullable:
            csdlProp.setNullable(true);
            break;
        case ResultSetMetaData.columnNoNulls:
            csdlProp.setNullable(false);
            break;
        default:
            // なにもしない.
            break;
        }

        // TODO デフォルト値の取得???

        // System.err.println("TRACE: " + csdlProp.getName() + ": " +
        // csdlProp.getType());

        return csdlProp;
    }

    /**
     * 実際の EntityCollection 生成時に、ResultSet から Property を作成.
     * 
     * @param rset         結果セット.
     * @param rsmeta       結果セットメタデータ.
     * @param column       項目番号. 1オリジン.
     * @param iyoEntitySet EntitySetインスタンス.
     * @return 作成された Property.
     * @throws SQLException              SQL例外が発生した場合.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static Property resultSet2Property(ResultSet rset, ResultSetMetaData rsmeta, int column,
            OiyokanCsdlEntitySet iyoEntitySet) throws ODataApplicationException, SQLException {
        // 基本的に CSDL で処理するが、やむを得ない場所のみ ResultSetMetaData を利用する
        final String propName = OiyokanNamingUtil.db2Entity(rsmeta.getColumnName(column));

        final CsdlProperty csdlProp = iyoEntitySet.getEntityType().getProperty(propName);
        if (csdlProp == null) {
            // [M034] ERROR: An unknown field name was specified. The field names are case
            // sensitive. Make sure the Oiyo field name matches the target field name.
            System.err.println(OiyokanMessages.M034 + ": colname:" + rsmeta.getColumnName(column)
                    + ", propname(should):" + propName);

            System.err.println("TRACE: EntityName: " + iyoEntitySet.getEntityType().getName());
            for (CsdlProperty look : iyoEntitySet.getEntityType().getProperties()) {
                System.err.println("  PropName: " + look.getName());
            }

            throw new ODataApplicationException(OiyokanMessages.M034 + ": colname:" + rsmeta.getColumnName(column)
                    + ", propname(should):" + propName, 500, Locale.ENGLISH);
        }
        if ("Edm.SByte".equals(csdlProp.getType())) {
            return new Property("Edm.SByte", propName, ValueType.PRIMITIVE, rset.getByte(column));
        } else if ("Edm.Byte".equals(csdlProp.getType())) {
            // 符号なしのバイト. h2 database には該当なし.
            // Edm.Byteに相当する型がJavaにないので Shortで代替.
            return new Property("Edm.Byte", propName, ValueType.PRIMITIVE, rset.getShort(column));
        } else if ("Edm.Int16".equals(csdlProp.getType())) {
            return new Property("Edm.Int16", propName, ValueType.PRIMITIVE, rset.getShort(column));
        } else if ("Edm.Int32".equals(csdlProp.getType())) {
            return new Property("Edm.Int32", propName, ValueType.PRIMITIVE, rset.getInt(column));
        } else if ("Edm.Int64".equals(csdlProp.getType())) {
            return new Property("Edm.Int64", propName, ValueType.PRIMITIVE, rset.getLong(column));
        } else if ("Edm.Decimal".equals(csdlProp.getType())) {
            return new Property("Edm.Decimal", propName, ValueType.PRIMITIVE, rset.getBigDecimal(column));
        } else if ("Edm.Boolean".equals(csdlProp.getType())) {
            return new Property("Edm.Boolean", propName, ValueType.PRIMITIVE, rset.getBoolean(column));
        } else if ("Edm.Single".equals(csdlProp.getType())) {
            return new Property("Edm.Single", propName, ValueType.PRIMITIVE, rset.getFloat(column));
        } else if ("Edm.Double".equals(csdlProp.getType())) {
            return new Property("Edm.Double", propName, ValueType.PRIMITIVE, rset.getDouble(column));
        } else if ("Edm.Date".equals(csdlProp.getType())) {
            return new Property("Edm.Date", propName, ValueType.PRIMITIVE, rset.getDate(column));
        } else if ("Edm.DateTimeOffset".equals(csdlProp.getType())) {
            return new Property("Edm.DateTimeOffset", propName, ValueType.PRIMITIVE, rset.getTimestamp(column));
        } else if ("Edm.TimeOfDay".equals(csdlProp.getType())) {
            return new Property("Edm.TimeOfDay", propName, ValueType.PRIMITIVE, rset.getTime(column));
        } else if ("Edm.String".equals(csdlProp.getType())) {
            // 基本的に CSDL で処理するが、やむを得ない場所のみ ResultSetMetaData を利用する
            // TODO FIXME ただしこれは事前に CSDL に記憶可能。
            if (Types.CLOB == rsmeta.getColumnType(column)) {
                try {
                    return new Property("Edm.String", propName, ValueType.PRIMITIVE,
                            StreamUtils.copyToString(rset.getAsciiStream(column), Charset.forName("UTF-8")));
                } catch (IOException ex) {
                    // [M007] UNEXPECTED: fail to read from CLOB
                    System.err
                            .println(OiyokanMessages.M007 + ": " + rsmeta.getColumnName(column) + ": " + ex.toString());
                    throw new ODataApplicationException(OiyokanMessages.M007 + ": " + rsmeta.getColumnName(column), //
                            500, Locale.ENGLISH);
                }
            } else {
                return new Property("Edm.String", propName, ValueType.PRIMITIVE, rset.getString(column));
            }
        } else if ("Edm.Binary".equals(csdlProp.getType())) {
            try {
                return new Property("Edm.Binary", propName, ValueType.PRIMITIVE,
                        StreamUtils.copyToByteArray(rset.getBinaryStream(column)));
            } catch (IOException ex) {
                // [M008] UNEXPECTED: fail to read from binary
                System.err.println(OiyokanMessages.M008 + ": " + rsmeta.getColumnName(column) + ": " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.M008 + ": " + rsmeta.getColumnName(column), //
                        500, Locale.ENGLISH);
            }
        } else if ("Edm.Guid".equals(csdlProp.getType())) {
            // Guid については UUID として読み込む。
            final Object obj = rset.getObject(column);
            if (obj == null) {
                // UUID に null が与えられた場合、そのままnullをセット. (null対応)
                return new Property("Edm.Guid", propName, ValueType.PRIMITIVE, null);
            } else if (obj instanceof java.util.UUID) {
                // h2 database で通過
                return new Property("Edm.Guid", propName, ValueType.PRIMITIVE, (java.util.UUID) obj);
            } else if (obj instanceof String) {
                // SQL Server 2008 で通過
                java.util.UUID look = UUID.fromString((String) obj);
                return new Property("Edm.Guid", propName, ValueType.PRIMITIVE, look);
            } else {
                // [M033] NOT SUPPORTED: unknown UUID object given
                System.err.println(OiyokanMessages.M033 + ": type[" + csdlProp.getType() + "], "
                        + obj.getClass().getCanonicalName());
                throw new ODataApplicationException(OiyokanMessages.M033 + ": type[" + csdlProp.getType() + "], "
                        + obj.getClass().getCanonicalName(), OiyokanMessages.M033_CODE, Locale.ENGLISH);
            }
        } else {
            // ARRAY と OTHER には対応しない。そもそもここ通過しないのじゃないの?
            // [M009] UNEXPECTED: missing impl
            System.err.println(
                    OiyokanMessages.M009 + ": type[" + csdlProp.getType() + "], " + rsmeta.getColumnName(column));
            throw new ODataApplicationException(
                    OiyokanMessages.M009 + ": type[" + csdlProp.getType() + "], " + rsmeta.getColumnName(column), //
                    500, Locale.ENGLISH);
        }
    }

    /**
     * PreparedStatement のバインドパラメータを設定.
     * 
     * @param stmt   PreparedStatement のインスタンス.
     * @param column 項目番号. 1オリジン.
     * @param value  セットしたい値.
     * @throws SQLException              SQL例外が発生した場合.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static void bindPreparedParameter(PreparedStatement stmt, int column, Object value)
            throws ODataApplicationException, SQLException {
        final boolean IS_SHOW_DEBUG = false;

        if (value == null) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setNull: null");
            stmt.setNull(column, Types.NULL);
        } else if (value instanceof Byte) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setByte: " + value);
            stmt.setByte(column, (Byte) value);
        } else if (value instanceof Short) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setShort: " + value);
            stmt.setShort(column, (Short) value);
        } else if (value instanceof Integer) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setInt: " + value);
            stmt.setInt(column, (Integer) value);
        } else if (value instanceof Long) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setLong: " + value);
            stmt.setLong(column, (Long) value);
        } else if (value instanceof BigDecimal) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setBigDecimal: " + value);
            // Oiyokan では 小数点は基本的にリテラルのまま残すため、このコードは通過しない.
            stmt.setBigDecimal(column, (BigDecimal) value);
        } else if (value instanceof Boolean) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setBoolean: " + value);
            stmt.setBoolean(column, (Boolean) value);
        } else if (value instanceof Float) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setFloat: " + value);
            // Oiyokan では 小数点は基本的にリテラルのまま残すため、このコードは通過しない.
            stmt.setFloat(column, (Float) value);
        } else if (value instanceof Double) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setDouble: " + value);
            // Oiyokan では 小数点は基本的にリテラルのまま残すため、このコードは通過しない.
            stmt.setDouble(column, (Double) value);
        } else if (value instanceof java.sql.Time) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setTime: " + value);
            // java.util.Dateより先に記載が必要
            java.sql.Time look = (java.sql.Time) value;
            stmt.setTime(column, look);
        } else if (value instanceof java.sql.Date) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setDate(1): " + value);
            // java.util.Dateより先に記載が必要
            java.sql.Date look = (java.sql.Date) value;
            stmt.setDate(column, look);
        } else if (value instanceof java.util.Date) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setDate(2): " + value);
            // java.sql.Timestampはここを通過.
            java.util.Date udate = (java.util.Date) value;
            java.sql.Date sdate = new java.sql.Date(udate.getTime());
            stmt.setDate(column, sdate);
        } else if (value instanceof java.util.Calendar) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setDate(3): " + value);
            java.util.Calendar cal = (java.util.Calendar) value;
            java.sql.Date sdate = new java.sql.Date(cal.getTime().getTime());
            stmt.setDate(column, sdate);
        } else if (value instanceof ZonedDateTime) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setDate(4): " + value);
            ZonedDateTime zdt = (ZonedDateTime) value;
            java.util.Date look = OiyoBasicDateTimeUtil.zonedDateTime2Date(zdt);
            java.sql.Date sdate = new java.sql.Date(look.getTime());
            stmt.setDate(column, sdate);
        } else if (value instanceof String) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setString: " + value);
            stmt.setString(column, (String) value);
        } else if (value instanceof byte[]) {
            if (IS_SHOW_DEBUG)
                System.err.println("TRACE: PreparedStatement#setBytes: " + value);
            byte[] look = (byte[]) value;
            stmt.setBytes(column, look);
        } else {
            // [M010] NOT SUPPORTED: Parameter Type
            System.err.println(OiyokanMessages.M010 + ": " + value.getClass().getCanonicalName());
            throw new ODataApplicationException(OiyokanMessages.M010 + ": " + value.getClass().getCanonicalName(), //
                    OiyokanMessages.M010_CODE, Locale.ENGLISH);
        }
    }

    private static final boolean IS_DEBUG_EXPAND_LITERAL = false;

    /**
     * リテラルまたはプレースホルダーをビルド.
     * 
     * @param sqlInfo    SQL info.
     * @param csdlType   CSDL type.
     * @param inputParam parameter text.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static void expandLiteralOrBindParameter(final OiyoSqlInfo sqlInfo, String csdlType, Object inputParam)
            throws ODataApplicationException {
        if (inputParam == null) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: null: ");
            sqlInfo.getSqlBuilder().append("?");
            sqlInfo.getSqlParamList().add(inputParam);
            return;
        }
        if ("Edm.SByte".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmSByte: " + inputParam);
            if (inputParam instanceof Byte //
                    || inputParam instanceof Short//
                    || inputParam instanceof Integer) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(inputParam);
            } else {
                // そのままSQL本文
                sqlInfo.getSqlBuilder().append(String.valueOf(inputParam));
            }
            return;
        }
        if ("Edm.Byte".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmByte: " + inputParam);
            // 符号なしByteはJavaには該当する型がないので Shortで代用.
            if (inputParam instanceof Byte //
                    || inputParam instanceof Short//
                    || inputParam instanceof Integer) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(inputParam);
            } else {
                // そのままSQL本文
                sqlInfo.getSqlBuilder().append(String.valueOf(inputParam));
            }
            return;
        }
        if ("Edm.Int16".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmInt16: " + inputParam);
            if (inputParam instanceof Byte //
                    || inputParam instanceof Short//
                    || inputParam instanceof Integer) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(inputParam);
            } else {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(Short.valueOf(String.valueOf(inputParam)));
            }
            return;
        }
        if ("Edm.Int32".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmInt32: " + inputParam);
            if (inputParam instanceof Byte //
                    || inputParam instanceof Short//
                    || inputParam instanceof Integer) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(inputParam);
            } else {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(Integer.valueOf(String.valueOf(inputParam)));
            }
            return;
        }
        if ("Edm.Int64".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmInt64: " + inputParam);
            if (inputParam instanceof Byte //
                    || inputParam instanceof Short//
                    || inputParam instanceof Integer) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(inputParam);
            } else {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(Long.valueOf(String.valueOf(inputParam)));
            }
            return;
        }
        if ("Edm.Decimal".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmDecimal: " + inputParam);
            if (inputParam instanceof BigDecimal) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(inputParam);
            } else {
                // 小数点付きの数値はパラメータとしては処理せずにそのまま文字列として連結.
                sqlInfo.getSqlBuilder().append(String.valueOf(inputParam));
            }
            return;
        }
        if ("Edm.Boolean".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmBoolean: " + inputParam);
            if (inputParam instanceof Boolean) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add((Boolean) inputParam);
            } else {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(Boolean.valueOf("true".equalsIgnoreCase(String.valueOf(inputParam))));
            }
            return;
        }
        if ("Edm.Single".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmSingle: " + inputParam);
            if (inputParam instanceof Float //
                    || inputParam instanceof Short//
                    || inputParam instanceof Integer) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(inputParam);
            } else {
                // 小数点付きの数値はパラメータとしては処理せずにそのまま文字列として連結.
                sqlInfo.getSqlBuilder().append(inputParam);
            }
            return;
        }
        if ("Edm.Double".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmDouble: " + inputParam);
            if (inputParam instanceof Double //
                    || inputParam instanceof Short//
                    || inputParam instanceof Integer) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(inputParam);
            } else {
                // 小数点付きの数値はパラメータとしては処理せずにそのまま文字列として連結.
                sqlInfo.getSqlBuilder().append(inputParam);
            }
            return;
        }
        if ("Edm.Date".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmDate: " + inputParam);
            if (inputParam instanceof java.sql.Date //
                    || inputParam instanceof java.util.Date//
                    || inputParam instanceof java.util.Calendar) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(inputParam);
            } else {
                ZonedDateTime zdt = OiyoBasicDateTimeUtil.parseStringDateTime(String.valueOf(inputParam));
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(zdt);
            }
            return;
        }
        if ("Edm.DateTimeOffset".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmDateTimeOffset: " + inputParam);
            if (inputParam instanceof java.sql.Date //
                    || inputParam instanceof java.util.Date//
                    || inputParam instanceof java.util.Calendar) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(inputParam);
            } else if (inputParam instanceof TemporalAccessor) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(inputParam);
            } else {
                ZonedDateTime zdt = OiyoBasicDateTimeUtil.parseStringDateTime(String.valueOf(inputParam));
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(zdt);
            }
            return;
        }
        if ("Edm.TimeOfDay".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmTimeOfDay: " + inputParam);
            if (inputParam instanceof java.sql.Time) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(inputParam);
            } else if (inputParam instanceof java.util.Calendar) {
                java.util.Calendar cal = (java.util.Calendar) inputParam;
                java.sql.Time look = new java.sql.Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                        cal.get(Calendar.SECOND));
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(look);
            } else {
                ZonedDateTime zdt = OiyoBasicDateTimeUtil.parseStringDateTime(String.valueOf(inputParam));
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(zdt);
            }
            return;
        }
        if ("Edm.String".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmString: " + inputParam);
            String value = String.valueOf(inputParam);
            if (value.startsWith("'") && value.endsWith("'")) {
                // 文字列リテラルについては前後のクオートを除去して記憶.
                value = value.substring(1, value.length() - 1);

                // 文字列リテラルとしてパラメータ化クエリで扱う.
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(value);
            } else {
                // 文字列リテラルとしてパラメータ化クエリで扱う.
                // そのまま出力するとエラーになる点に注意!
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(value);
            }
            return;
        }
        if ("Edm.Binary".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmBinary: " + inputParam);
            if (inputParam instanceof byte[] //
                    || inputParam instanceof ByteArrayInputStream) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(inputParam);
            } else {
                final byte[] look = new Base64().decode(String.valueOf(inputParam));
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(look);
            }
            return;
        }
        if ("Edm.Guid".equals(csdlType)) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmGuid: " + inputParam);
            if (inputParam instanceof byte[] //
                    || inputParam instanceof ByteArrayInputStream) {
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(inputParam);
            } else {
                final String look = String.valueOf(inputParam);
                sqlInfo.getSqlBuilder().append("?");
                sqlInfo.getSqlParamList().add(look);
            }
            return;
        }

        // [M037] NOT SUPPORTED: Parameter Type
        System.err.println(OiyokanMessages.M037 + ": " + csdlType);
        throw new ODataApplicationException(OiyokanMessages.M037 + ": " + csdlType, //
                OiyokanMessages.M037_CODE, Locale.ENGLISH);
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
        switch (sqlInfo.getEntitySet().getDatabaseType()) {
        case h2:
        case MSSQL2008:
            if (fieldName.indexOf(" ") <= 0 && fieldName.indexOf(".") <= 0) {
                // 空白のない場合はエスケープしない.
                return fieldName;
            }
            return "[" + fieldName + "]";

        case postgres:
        case ORACLE:
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
            System.err.println(OiyokanMessages.M020 + ": " + sqlInfo.getSettingsDatabase().getType());
            throw new ODataApplicationException(OiyokanMessages.M020 + ": " + sqlInfo.getSettingsDatabase().getType(),
                    500, Locale.ENGLISH);
        }
    }

    ////////////////////////////
    // EXECUTE DML

    /**
     * TODO FIXME 自動採集番された項目の値をreturnすること。
     * 
     * @param connTargetDb 利用データベース接続.
     * @param sqlInfo      実行したいSQL情報.
     * @return (もしあれば)生成されたキーのリスト.
     * @throws ODataApplicationException
     */
    public static List<String> executeDml(Connection connTargetDb, OiyoSqlInfo sqlInfo, boolean returnGeneratedKeys)
            throws ODataApplicationException {
        final String sql = sqlInfo.getSqlBuilder().toString();
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: TRACE: SQL exec: " + sql);

        final long startMillisec = System.currentTimeMillis();
        try (var stmt = connTargetDb.prepareStatement(sql, //
                (returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS))) {
            // set query timeout
            stmt.setQueryTimeout(OiyokanConstants.JDBC_STMT_TIMEOUT);

            int idxColumn = 1;
            for (Object look : sqlInfo.getSqlParamList()) {
                // System.err.println("TRACE: param: " + look.toString());
                OiyoBasicJdbcUtil.bindPreparedParameter(stmt, idxColumn++, look);
            }

            final int result = stmt.executeUpdate();
            if (result != 1) {
                // [M201] NO record processed. No Entity effects.
                System.err.println(OiyokanMessages.M201 + ": " + sql);
                throw new ODataApplicationException(OiyokanMessages.M201 + ": " + sql, //
                        OiyokanMessages.M201_CODE, Locale.ENGLISH);
            }

            // 生成されたキーがあればそれを採用。
            final List<String> generatedKeys = new ArrayList<>();
            if (returnGeneratedKeys) {
                final ResultSet rsKeys = stmt.getGeneratedKeys();
                if (rsKeys.next()) {
                    final ResultSetMetaData rsmetaKeys = rsKeys.getMetaData();
                    for (int column = 1; column <= rsmetaKeys.getColumnCount(); column++) {
                        generatedKeys.add(rsKeys.getString(column));
                    }
                }
            }

            final long endMillisec = System.currentTimeMillis();
            if (OiyokanConstants.IS_TRACE_ODATA_V4) {
                final long elapsed = endMillisec - startMillisec;
                if (elapsed >= 10) {
                    System.err.println("OData v4: TRACE: SQL: elapsed: " + (endMillisec - startMillisec));
                }
            }

            return generatedKeys;
        } catch (SQLIntegrityConstraintViolationException ex) {
            // [M202] Integrity constraint violation occured (DML). 制約違反.
            System.err.println(OiyokanMessages.M202 + ": " + sql + ", " + ex.toString());
            // 制約違反だけだと意味が不明であろうからメッセージも返却.
            throw new ODataApplicationException(OiyokanMessages.M202 + ": " + sql + ": " + ex.getMessage(), //
                    OiyokanMessages.M202_CODE, Locale.ENGLISH);
        } catch (SQLTimeoutException ex) {
            // [M203] SQL timeout at execute.
            System.err.println(OiyokanMessages.M203 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M203 + ": " + sql, //
                    OiyokanMessages.M203_CODE, Locale.ENGLISH);
        } catch (SQLException ex) {
            ex.printStackTrace();
            // [M204] Fail to execute SQL.
            System.err.println(OiyokanMessages.M204 + ": " + sql + ", " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M204 + ": " + sql, //
                    OiyokanMessages.M204_CODE, Locale.ENGLISH);
        }
    }
}