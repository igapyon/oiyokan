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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;

/**
 * Oiyokan 関連のDBまわりユーティリティクラス.
 */
public class BasicDbUtil {
    private BasicDbUtil() {
    }

    /**
     * 内部データベースへのDB接続を取得します。
     * 
     * @return データベース接続。
     */
    public static Connection getInternalConnection() {
        Connection conn;
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        // SQL Server 互換モードで動作させる.
        final var jdbcConnStr = "jdbc:h2:mem:oiyokan;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;MODE=MSSQLServer";
        // System.err.println("TRACE: DEMO: [connect jdbc] " + jdbcConnStr);
        try {
            conn = DriverManager.getConnection(//
                    jdbcConnStr, "sa", "");
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex);
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
     * @param rsmeta ResultSetMetaDataインスタンス.
     * @param column 項目番号.
     * @return CsdlProperty 情報.
     * @throws SQLException SQL例外が発生した場合.
     */
    public static CsdlProperty resultSetMetaData2CsdlProperty(ResultSetMetaData rsmeta, int column)
            throws SQLException {
        final CsdlProperty csdlProp = new CsdlProperty().setName(rsmeta.getColumnName(column));
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
        case Types.BOOLEAN:
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
            csdlProp.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            csdlProp.setMaxLength(rsmeta.getColumnDisplaySize(column));
            break;
        default:
            // TODO なにか手当が必要。あるいは、この場合はログ吐いたうえで処理対象から外すのが無難かも。
            csdlProp.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            break;
        }

        if (false) {
            // TODO FIXME いまここを有効にすると、なんとエラーが出てしまう。
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
        }

        // TODO デフォルト値の取得???

        return csdlProp;
    }

    /**
     * 実際の EntityCollection 生成時に、ResultSet から Property を作成.
     * 
     * @param rset   結果セット.
     * @param rsmeta 結果セットメタデータ.
     * @param column 項目番号. 1オリジン.
     * @return 作成された Property
     * @throws SQLException SQL例外が発生した場合.
     */
    public static Property resultSet2Property(ResultSet rset, ResultSetMetaData rsmeta, int column)
            throws SQLException {
        Property prop = null;
        final String columnName = rsmeta.getColumnName(column);
        switch (rsmeta.getColumnType(column)) {
        case Types.TINYINT:
            prop = new Property(null, columnName, ValueType.PRIMITIVE, rset.getByte(column));
            break;
        case Types.SMALLINT:
            prop = new Property(null, columnName, ValueType.PRIMITIVE, rset.getShort(column));
            break;
        case Types.INTEGER:
            prop = new Property(null, columnName, ValueType.PRIMITIVE, rset.getInt(column));
            break;
        case Types.BIGINT:
            prop = new Property(null, columnName, ValueType.PRIMITIVE, rset.getLong(column));
            break;
        case Types.DECIMAL:
            prop = new Property(null, columnName, ValueType.PRIMITIVE, rset.getBigDecimal(column));
            break;
        case Types.BOOLEAN:
            prop = new Property(null, columnName, ValueType.PRIMITIVE, rset.getBoolean(column));
            break;
        case Types.REAL:
            prop = new Property(null, columnName, ValueType.PRIMITIVE, rset.getFloat(column));
            break;
        case Types.DOUBLE:
            prop = new Property(null, columnName, ValueType.PRIMITIVE, rset.getDouble(column));
            break;
        case Types.DATE:
            prop = new Property(null, columnName, ValueType.PRIMITIVE, rset.getDate(column));
            break;
        case Types.TIMESTAMP:
            prop = new Property(null, columnName, ValueType.PRIMITIVE, rset.getTimestamp(column));
            break;
        case Types.TIME:
            prop = new Property(null, columnName, ValueType.PRIMITIVE, rset.getTime(column));
            break;
        case Types.CHAR:
        case Types.VARCHAR:
        default:
            prop = new Property(null, columnName, ValueType.PRIMITIVE, rset.getString(column));
            break;
        }
        return prop;
    }

    /**
     * PreparedStatement のバインドパラメータを設定.
     * 
     * @param stmt   PreparedStatement のインスタンス.
     * @param column 項目番号. 1オリジン.
     * @param value  セットしたい値.
     * @throws SQLException SQL例外が発生した場合.
     */
    public static void bindPreparedParameter(PreparedStatement stmt, int column, Object value) throws SQLException {
        if (value instanceof Integer) {
            stmt.setInt(column, (Integer) value);
        } else {
            // TODO 他の型への対応を追加。
            stmt.setString(column, (String) value);
        }
    }
}
