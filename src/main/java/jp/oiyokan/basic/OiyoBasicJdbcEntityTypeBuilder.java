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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanCsdlEntitySet;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.dto.OiyokanSettingsDatabase;
import jp.oiyokan.settings.OiyokanNamingUtil;
import jp.oiyokan.settings.OiyokanSettingsUtil;

/**
 * 典型的で基本的な JDBC処理を利用した EntityType を構築。
 * 
 * 内部データベースをもとにした Oiyo 形式での処理であるため接続先のリソースの種類によらず Oiyokan ではこのクラスを利用.
 */
public class OiyoBasicJdbcEntityTypeBuilder {
    /**
     * 処理対象となる EntitySet.
     */
    private OiyokanCsdlEntitySet entitySet = null;

    /**
     * コンストラクタ。
     * 
     * @param entitySet OiyokanCsdlEntitySetのインスタンス.
     */
    public OiyoBasicJdbcEntityTypeBuilder(OiyokanCsdlEntitySet entitySet) {
        this.entitySet = entitySet;
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println( //
                    "OData v4: EntityType: " + entitySet.getName() + " (Oiyokan: " + OiyokanConstants.VERSION + ")");
    }

    /**
     * EntityType を取得.
     *
     * @return 取得された EntityType.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public CsdlEntityType getEntityType() throws ODataApplicationException {
        // 内部データベースである インメモリ作業データベースに接続.
        // EntityTypeはインメモリ内部データベースの情報をもとに構築.
        OiyokanSettingsDatabase settingsInternalDatabase = OiyokanSettingsUtil
                .getOiyokanDatabase(OiyokanConstants.OIYOKAN_INTERNAL_DB);

        try (Connection connInterDb = OiyoBasicJdbcUtil.getConnection(settingsInternalDatabase)) {
            // CSDL要素型として情報を組み上げ.
            final CsdlEntityType entityType = new CsdlEntityType();
            entityType.setName(entitySet.getEntityNameIyo());

            // 基本的な動作: 内部データベースである h2 データベースから該当する Oiyo による情報取得.
            final List<CsdlProperty> propertyList = new ArrayList<>();
            entityType.setProperties(propertyList);

            // SELECT * について、この箇所のみ記述を許容。
            // DatabaseMetaData では情報を取りづらい場合があるためこちら ResultSetMetaData を使用。
            final String sql = "SELECT * FROM " + entitySet.getDbTableNameLocalOiyo() + " LIMIT 1";
            if (OiyokanConstants.IS_TRACE_ODATA_V4)
                System.err.println("OData v4: TRACE: Entity: SQL: " + sql);
            try (PreparedStatement stmt = connInterDb.prepareStatement(sql)) {
                ResultSetMetaData rsmeta = stmt.getMetaData();
                final int columnCount = rsmeta.getColumnCount();
                for (int column = 1; column <= columnCount; column++) {
                    propertyList.add(OiyoBasicJdbcUtil.resultSetMetaData2CsdlProperty(rsmeta, column));
                }

                // テーブルのキー情報
                final List<CsdlPropertyRef> keyRefList = new ArrayList<>();
                final DatabaseMetaData dbmeta = connInterDb.getMetaData();
                final ResultSet rsKey = dbmeta.getPrimaryKeys(null, null, entitySet.getDbTableNameLocalOiyo());
                for (; rsKey.next();) {
                    // キー名はここでは利用する必要がない: rsKey.getString("PK_NAME");
                    String colName = rsKey.getString("COLUMN_NAME");

                    CsdlPropertyRef propertyRef = new CsdlPropertyRef();
                    propertyRef.setName(OiyokanNamingUtil.db2Entity(colName));
                    keyRefList.add(propertyRef);
                }

                if (keyRefList.size() == 0) {
                    // キーがないものは OData 的に不都合があるため警告する。
                    if (OiyokanConstants.IS_TRACE_ODATA_V4) {
                        System.err.println("OData v4: WARNING: No ID: " + entitySet.getName());
                        System.err.println("OData v4: WARNING: Set primary key on Oiyo table: "
                                + entitySet.getDbTableNameLocalOiyo());
                    }
                }

                entityType.setKey(keyRefList);
            }

            // 構築結果を記憶。
            entitySet.setEntityType(entityType);
            return entityType;
        } catch (SQLException ex) {
            // [M019] UNEXPECTED: Fail to get database meta
            System.err.println(OiyokanMessages.M019 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M019, OiyokanMessages.M019_CODE, Locale.ENGLISH);
        }
    }
}