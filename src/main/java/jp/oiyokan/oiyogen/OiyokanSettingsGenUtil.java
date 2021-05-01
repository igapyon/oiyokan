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
package jp.oiyokan.oiyogen;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsEntityType;
import jp.oiyokan.dto.OiyoSettingsProperty;
import jp.oiyokan.util.OiyoEdmUtil;
import jp.oiyokan.util.OiyoJdbcUtil;
import jp.oiyokan.util.OiyoMapJdbcEdmUtil;

/**
 * Oiyokan (OData v4 server) が動作する際に必要になる内部管理データベースのバージョン情報および Oiyo情報 をセットアップ.
 */
public class OiyokanSettingsGenUtil {
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(OiyokanSettingsGenUtil.class);

    private OiyokanSettingsGenUtil() {
    }

    /**
     * oiyokan-settings.json を生成する
     * 
     * @param connTargetDb
     * @param tableName
     * @param databaseType
     * @return
     * @throws SQLException
     * @throws ODataApplicationException
     */
    public static OiyoSettingsEntitySet generateSettingsEntitySet(Connection connTargetDb, String tableName,
            OiyokanConstants.DatabaseType databaseType) throws SQLException, ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = new OiyoSettingsEntitySet();
        entitySet.setEntityType(new OiyoSettingsEntityType());

        final Map<String, String> defaultValueMap = new HashMap<>();
        {
            final ResultSet rsdbmetacolumns = connTargetDb.getMetaData().getColumns(null, null, tableName, "%");
            for (; rsdbmetacolumns.next();) {
                String colName = rsdbmetacolumns.getString("COLUMN_NAME");
                String defValue = rsdbmetacolumns.getString("COLUMN_DEF");
                defaultValueMap.put(colName, defValue);
            }
        }

        entitySet.setName(tableName.replaceAll(" ", ""));
        entitySet.setDbSettingName("oiyoUnitTestDb");
        entitySet.setDescription("Description.");
        entitySet.setCanCreate(true);
        entitySet.setCanRead(true);
        entitySet.setCanUpdate(true);
        entitySet.setCanDelete(true);
        // TODO FIXME 処理の共通化
        entitySet.getEntityType().setName(tableName.replaceAll(" ", "_"));
        entitySet.getEntityType().setDbName(tableName);
        entitySet.getEntityType().setProperty(new ArrayList<OiyoSettingsProperty>());
        entitySet.getEntityType().setKeyName(new ArrayList<String>());

        try (PreparedStatement stmt = connTargetDb.prepareStatement(
                "SELECT * FROM " + OiyoCommonJdbcUtil.escapeKakkoFieldName(databaseType, tableName))) {
            ResultSetMetaData rsmeta = stmt.getMetaData();
            final int columnCount = rsmeta.getColumnCount();
            for (int column = 1; column <= columnCount; column++) {
                final OiyoSettingsProperty property = new OiyoSettingsProperty();
                entitySet.getEntityType().getProperty().add(property);

                String columnName = rsmeta.getColumnName(column);
                property.setName(columnName.replaceAll(" ", "_"));
                property.setDbName(columnName);
                property.setDbType(rsmeta.getColumnTypeName(column));

                final int jdbcTypes = rsmeta.getColumnType(column);
                property.setJdbcType(OiyoJdbcUtil.types2String(jdbcTypes));
                try {
                    EdmPrimitiveType edmType = OiyoMapJdbcEdmUtil.jdbcTypes2Edm(jdbcTypes);
                    property.setEdmType(OiyoEdmUtil.edmType2String(edmType));
                } catch (IllegalArgumentException ex) {
                    property.setEdmType("NOT.SUPPORTED");
                }
                switch (rsmeta.getColumnType(column)) {
                case Types.DECIMAL:
                    if (rsmeta.getPrecision(column) > 0) {
                        property.setPrecision(rsmeta.getPrecision(column));
                        property.setScale(rsmeta.getScale(column));
                    }
                    break;
                case Types.NUMERIC:
                    // postgres で発生.
                    if (rsmeta.getPrecision(column) > 0) {
                        property.setPrecision(rsmeta.getPrecision(column));
                        property.setScale(rsmeta.getScale(column));
                    }
                    break;
                case Types.CHAR:
                    property.setLengthFixed(true);
                    property.setMaxLength(rsmeta.getColumnDisplaySize(column));
                    break;
                case Types.VARCHAR:
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        property.setMaxLength(rsmeta.getColumnDisplaySize(column));
                    }
                    break;
                case Types.LONGVARCHAR:
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        property.setMaxLength(rsmeta.getColumnDisplaySize(column));
                    }
                    break;
                case Types.LONGNVARCHAR:
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        property.setMaxLength(rsmeta.getColumnDisplaySize(column));
                    }
                    break;
                case Types.CLOB:
                    if (rsmeta.getColumnDisplaySize(column) > 0
                            && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                        property.setMaxLength(rsmeta.getColumnDisplaySize(column));
                    }
                    break;
                case Types.BINARY:
                    if ("UUID".equalsIgnoreCase(rsmeta.getColumnTypeName(column))) {
                        // TODO FIXME 対応調査.
                        // 型名が UUID の時だけ特殊な挙動をする.
                        property.setEdmType("Edm.Guid");
                    } else {
                        if (rsmeta.getColumnDisplaySize(column) > 0
                                && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                            property.setMaxLength(rsmeta.getColumnDisplaySize(column));
                        }
                    }
                    break;
                case Types.VARBINARY:
                    if ("UUID".equalsIgnoreCase(rsmeta.getColumnTypeName(column))) {
                        // 型名が UUID の時だけ特殊な挙動をする.
                        property.setEdmType("Edm.Guid");
                    } else {
                        if (rsmeta.getColumnDisplaySize(column) > 0
                                && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                            property.setMaxLength(rsmeta.getColumnDisplaySize(column));
                        }
                    }
                    break;
                case Types.LONGVARBINARY:
                    if ("UUID".equalsIgnoreCase(rsmeta.getColumnTypeName(column))) {
                        // 型名が UUID の時だけ特殊な挙動をする.
                        property.setEdmType("Edm.Guid");
                    } else {
                        if (rsmeta.getColumnDisplaySize(column) > 0
                                && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                            property.setMaxLength(rsmeta.getColumnDisplaySize(column));
                        }
                    }
                    break;
                case Types.BLOB:
                    if ("UUID".equalsIgnoreCase(rsmeta.getColumnTypeName(column))) {
                        // 型名が UUID の時だけ特殊な挙動をする.
                        property.setEdmType("Edm.Guid");
                    } else {
                        if (rsmeta.getColumnDisplaySize(column) > 0
                                && rsmeta.getColumnDisplaySize(column) != Integer.MAX_VALUE) {
                            property.setMaxLength(rsmeta.getColumnDisplaySize(column));
                        }
                    }
                    break;
                case Types.ARRAY:
                    // postgres で発生. 対応しない.
                    break;
                case Types.OTHER:
                    // postgres で発生. 対応しない.
                    break;
                }

                if (defaultValueMap.get(columnName) != null) {
                    property.setDbDefault(defaultValueMap.get(columnName));
                }

                final int nullableVal = rsmeta.isNullable(column);
                if (ResultSetMetaData.columnNoNulls == nullableVal) {
                    property.setNullable(false);
                } else if (ResultSetMetaData.columnNullable == nullableVal) {
                    property.setNullable(true);
                } else if (ResultSetMetaData.columnNullableUnknown == nullableVal) {
                    // 不明は null.
                    property.setNullable(null);
                }

                if (property.getDbDefault() != null //
                        && (property.getDbDefault().contains("NEXT VALUE FOR")
                                || property.getDbDefault().contains("nextval("))) {
                    if (!"IDENTITY".equalsIgnoreCase(property.getDbType()) //
                            && !"SERIAL".equalsIgnoreCase(property.getDbType()) //
                            && !"SEQUENCE".equalsIgnoreCase(property.getDbType())) {
                        property.setDbType("IDENTITY");
                    }
                    property.setDbDefault(null);
                }

                if ("IDENTITY".equalsIgnoreCase(property.getDbType()) //
                        || "SERIAL".equalsIgnoreCase(property.getDbType()) //
                        || "SEQUENCE".equalsIgnoreCase(property.getDbType()) //
                ) {
                    // TODO SQLSV2008の時の挙動は調査が必要.
                    property.setAutoGenKey(true);

                    // autoGenKey の場合には nullable である必要がある。
                    if (property.getNullable() != null && property.getNullable() == false) {
                        property.setNullable(true);
                    }
                }

                if ("BLOB".equalsIgnoreCase(property.getDbType()) //
                        || "CLOB".equalsIgnoreCase(property.getDbType()) //
                        || "TEXT".equalsIgnoreCase(property.getDbType()) //
                ) {
                    // Stream軽の入出力をヒント.
                    property.setJdbcStream(true);
                }
            }

            // テーブルのキー情報
            final DatabaseMetaData dbmeta = connTargetDb.getMetaData();
            final ResultSet rsKey = dbmeta.getPrimaryKeys(null, null, tableName);
            for (; rsKey.next();) {
                String colName = rsKey.getString("COLUMN_NAME");
                // TODO FIXME 処理の共通化の検討.
                colName = colName.replaceAll(" ", "_");
                entitySet.getEntityType().getKeyName().add(colName);
            }
        }

        return entitySet;
    }

    /**
     * 与えられた情報から DDL を生成.
     * 
     * @param databaseType db type.
     * @param entitySet    EntitySet info.
     * @param sql          sql buinding.
     * @throws ODataApplicationException OData App exception occured.
     */
    public static final void generateDdl(OiyokanConstants.DatabaseType databaseType, OiyoSettingsEntitySet entitySet,
            StringBuilder sql) throws ODataApplicationException {
        final OiyoSettingsEntityType entityType = entitySet.getEntityType();
        sql.append("CREATE TABLE");
        sql.append(" IF NOT EXISTS");
        sql.append("\n");

        sql.append("  " + OiyoCommonJdbcUtil.escapeKakkoFieldName(databaseType, entityType.getDbName()) + " (\n");

        boolean isFirst = true;
        for (OiyoSettingsProperty prop : entityType.getProperty()) {
            sql.append("    ");
            if (isFirst) {
                isFirst = false;
            } else {
                sql.append(", ");
            }
            sql.append(OiyoCommonJdbcUtil.escapeKakkoFieldName(databaseType, prop.getDbName()));
            if (prop.getDbDefault() != null && prop.getDbDefault().indexOf("NEXT VALUE FOR") >= 0) {
                // h2 database 特殊ルール
                sql.append(" IDENTITY");
            } else {
                sql.append(" " + prop.getDbType());
            }

            if (prop.getMaxLength() != null && prop.getMaxLength() > 0) {
                sql.append("(" + prop.getMaxLength() + ")");
            }
            if (prop.getPrecision() != null && prop.getPrecision() > 0) {
                sql.append("(" + prop.getPrecision());
                if (prop.getScale() != null) {
                    sql.append("," + prop.getScale());
                }
                sql.append(")");
            }
            if (prop.getDbDefault() != null) {
                if (prop.getDbDefault().startsWith("NEXT VALUE FOR")) {
                    // h2 database 特殊ルール
                } else {
                    sql.append(" DEFAULT " + prop.getDbDefault());
                }
            }
            if (prop.getNullable() != null && prop.getNullable() == false) {
                sql.append(" NOT NULL");
            }
            sql.append("\n");
        }

        if (entityType.getKeyName().size() > 0) {
            sql.append("    , PRIMARY KEY(");
            isFirst = true;
            for (String key : entityType.getKeyName()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sql.append(",");
                }

                OiyoSettingsProperty prop = null;
                for (OiyoSettingsProperty look : entitySet.getEntityType().getProperty()) {
                    if (look.getName().equalsIgnoreCase(key)) {
                        prop = look;
                    }
                }
                if (prop == null) {
                    throw new IllegalArgumentException("EntitySetからProperty定義が発見できない. JSONファイル破損の疑い: EntitySet:"
                            + entitySet.getName() + ", key:" + key);
                }

                sql.append(OiyoCommonJdbcUtil.escapeKakkoFieldName(databaseType, prop.getDbName()));
            }
            sql.append(")\n");
        }

        sql.append("  );\n");
        sql.append("\n");
    }
}
