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

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Locale;
import java.util.UUID;

import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.util.StreamUtils;

import jp.oiyokan.OiyokanCsdlEntitySet;
import jp.oiyokan.OiyokanNamingUtil;
import jp.oiyokan.dto.OiyokanSettingsDatabase;

/**
 * Oiyokan 関連のDBまわりユーティリティクラス.
 */
public class BasicDbUtil {
    private BasicDbUtil() {
    }

    /**
     * 内部データベースへのDB接続を取得します。
     * 
     * @param settingsDatabase データベース設定情報.
     * @return データベース接続.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static Connection getConnection(OiyokanSettingsDatabase settingsDatabase) throws ODataApplicationException {
        Connection conn;
        // OData server 起動シーケンスにてドライバ存在チェックは既に実施済み.
        // Class.forName(settingsDatabase.getJdbcDriver());

        System.err.println(
                "TRACE: DEBUG: DB接続開始: " + settingsDatabase.getName() + " (" + settingsDatabase.getDescription() + ")");

        try {
            if (settingsDatabase.getJdbcUser() == null || settingsDatabase.getJdbcUser().trim().length() == 0) {
                conn = DriverManager.getConnection(settingsDatabase.getJdbcUrl());
            } else {
                conn = DriverManager.getConnection(settingsDatabase.getJdbcUrl(), settingsDatabase.getJdbcUser(),
                        settingsDatabase.getJdbcPass());
            }
        } catch (SQLException ex) {
            System.err.println("OData v4: UNEXPECTED: データベースの接続に失敗: [" + settingsDatabase.getName()
                    + "] しばらく待って再度トライしてください。しばらく経っても改善しない場合はIT部門に連絡してください: " + ex.toString());
            throw new ODataApplicationException("OData v4: UNEXPECTED: データベースの接続に失敗: [" + settingsDatabase.getName()
                    + "] しばらく待って再度トライしてください。しばらく経っても改善しない場合はIT部門に連絡してください", 500, Locale.ENGLISH);
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
            // postgres で発生.
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
            System.err.println("NOT SUPPORTED: CSDL: JDBC Type: " + rsmeta.getColumnType(column));
            throw new ODataApplicationException("NOT SUPPORTED: CSDL: JDBC Type: " + rsmeta.getColumnType(column), 500,
                    Locale.ENGLISH);
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
        if ("Edm.SByte".equals(csdlProp.getType())) {
            return new Property(null, propName, ValueType.PRIMITIVE, rset.getByte(column));
        } else if ("Edm.Int16".equals(csdlProp.getType())) {
            return new Property(null, propName, ValueType.PRIMITIVE, rset.getShort(column));
        } else if ("Edm.Int32".equals(csdlProp.getType())) {
            return new Property(null, propName, ValueType.PRIMITIVE, rset.getInt(column));
        } else if ("Edm.Int64".equals(csdlProp.getType())) {
            return new Property(null, propName, ValueType.PRIMITIVE, rset.getLong(column));
        } else if ("Edm.Decimal".equals(csdlProp.getType())) {
            return new Property(null, propName, ValueType.PRIMITIVE, rset.getBigDecimal(column));
        } else if ("Edm.Boolean".equals(csdlProp.getType())) {
            return new Property(null, propName, ValueType.PRIMITIVE, rset.getBoolean(column));
        } else if ("Edm.Single".equals(csdlProp.getType())) {
            return new Property(null, propName, ValueType.PRIMITIVE, rset.getFloat(column));
        } else if ("Edm.Double".equals(csdlProp.getType())) {
            return new Property(null, propName, ValueType.PRIMITIVE, rset.getDouble(column));
        } else if ("Edm.Date".equals(csdlProp.getType())) {
            return new Property(null, propName, ValueType.PRIMITIVE, rset.getDate(column));
        } else if ("Edm.DateTimeOffset".equals(csdlProp.getType())) {
            return new Property(null, propName, ValueType.PRIMITIVE, rset.getTimestamp(column));
        } else if ("Edm.TimeOfDay".equals(csdlProp.getType())) {
            return new Property(null, propName, ValueType.PRIMITIVE, rset.getTime(column));
        } else if ("Edm.String".equals(csdlProp.getType())) {
            // 基本的に CSDL で処理するが、やむを得ない場所のみ ResultSetMetaData を利用する
            // TODO FIXME ただしこれは事前に CSDL に記憶可能。
            if (Types.CLOB == rsmeta.getColumnType(column)) {
                try {
                    return new Property(null, propName, ValueType.PRIMITIVE,
                            StreamUtils.copyToString(rset.getAsciiStream(column), Charset.forName("UTF-8")));
                } catch (IOException ex) {
                    System.err.println("UNEXPECTED: fail to read from CLOB: " + rsmeta.getColumnName(column) + ": "
                            + ex.toString());
                    throw new ODataApplicationException(
                            "UNEXPECTED: fail to read from CLOB: " + rsmeta.getColumnName(column), 500, Locale.ENGLISH);
                }
            } else {
                return new Property(null, propName, ValueType.PRIMITIVE, rset.getString(column));
            }
        } else if ("Edm.Binary".equals(csdlProp.getType())) {
            try {
                return new Property(null, propName, ValueType.PRIMITIVE,
                        StreamUtils.copyToByteArray(rset.getBinaryStream(column)));
            } catch (IOException ex) {
                System.err.println(
                        "UNEXPECTED: fail to read from binary: " + rsmeta.getColumnName(column) + ": " + ex.toString());
                throw new ODataApplicationException(
                        "UNEXPECTED: fail to read from binary: " + rsmeta.getColumnName(column), 500, Locale.ENGLISH);
            }
        } else if ("Edm.Guid".equals(csdlProp.getType())) {
            // Guid については UUID として読み込む。
            java.util.UUID look = (UUID) rset.getObject(column);
            return new Property(null, propName, ValueType.PRIMITIVE, look);
        } else {
            // ARRAY と OTHER には対応しない。そもそもここ通過しないのじゃないの?
            System.err.println(
                    "UNEXPECTED: missing impl: type[" + csdlProp.getType() + "], " + rsmeta.getColumnName(column));
            throw new ODataApplicationException(
                    "UNEXPECTED: missing impl: type[" + csdlProp.getType() + "], " + rsmeta.getColumnName(column), 500,
                    Locale.ENGLISH);
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
        if (value instanceof Byte) {
            stmt.setByte(column, (Byte) value);
        } else if (value instanceof Short) {
            stmt.setShort(column, (Short) value);
        } else if (value instanceof Integer) {
            stmt.setInt(column, (Integer) value);
        } else if (value instanceof Long) {
            stmt.setLong(column, (Long) value);
        } else if (value instanceof BigDecimal) {
            stmt.setBigDecimal(column, (BigDecimal) value);
        } else if (value instanceof Boolean) {
            stmt.setBoolean(column, (Boolean) value);
        } else if (value instanceof Float) {
            stmt.setFloat(column, (Float) value);
        } else if (value instanceof Double) {
            stmt.setDouble(column, (Double) value);
        } else if (value instanceof java.util.Date) {
            // java.sql.Timestampはここを通過.
            java.util.Date udate = (java.util.Date) value;
            java.sql.Date sdate = new java.sql.Date(udate.getTime());
            stmt.setDate(column, sdate);
        } else if (value instanceof String) {
            stmt.setString(column, (String) value);
        } else {
            System.err.println("NOT SUPPORTED: Parameter Type: " + value.getClass().getCanonicalName());
            throw new ODataApplicationException("NOT SUPPORTED: Parameter Type: " + value.getClass().getCanonicalName(),
                    500, Locale.ENGLISH);
        }
    }
}
