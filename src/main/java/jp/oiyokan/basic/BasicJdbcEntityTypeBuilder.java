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

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanCsdlEntitySet;
import jp.oiyokan.h2.data.TinyH2DbSample;

/**
 * 典型的で基本的な JDBC処理を利用した EntityType を構築します。
 */
public class BasicJdbcEntityTypeBuilder {
    /**
     * 処理対象となる EntitySet.
     */
    private OiyokanCsdlEntitySet entitySet = null;

    /**
     * コンストラクタ。
     * 
     * @param entitySet OiyokanCsdlEntitySetのインスタンス.
     */
    public BasicJdbcEntityTypeBuilder(OiyokanCsdlEntitySet entitySet) {
        this.entitySet = entitySet;
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println( //
                    "OData v4: EntityType: " + entitySet.getName() + " (Oiyokan: " + OiyokanConstants.VERSION + ")");
    }

    /**
     * EntityType を取得.
     *
     * @return 取得された EntityType.
     */
    public CsdlEntityType getEntityType() {
        // インメモリ作業データベースに接続.
        try (Connection conn = BasicDbUtil.getInternalConnection()) {
            // テーブルをセットアップ.
            TinyH2DbSample.createTable(conn);

            // CSDL要素型として情報を組み上げ.
            CsdlEntityType entityType = new CsdlEntityType();
            entityType.setName(entitySet.getEntityNameIyo());

            // 基本的な動作: バッファ的な h2 データベースから該当情報を取得.
            final List<CsdlProperty> propertyList = new ArrayList<>();
            entityType.setProperties(propertyList);

            // SELECT * について、この箇所のみ記述を許容。
            // DatabaseMetaData では取りづらい情報があるためこちらを採用。
            try (PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM " + entitySet.getDbTableNameLocalIyo() + " LIMIT 1")) {
                ResultSetMetaData rsmeta = stmt.getMetaData();
                final int columnCount = rsmeta.getColumnCount();
                for (int column = 1; column <= columnCount; column++) {
                    propertyList.add(BasicDbUtil.resultSetMetaData2CsdlProperty(rsmeta, column));
                }

                // テーブルのキー情報
                final List<CsdlPropertyRef> keyRefList = new ArrayList<>();
                final DatabaseMetaData dbmeta = conn.getMetaData();
                final ResultSet rsKey = dbmeta.getPrimaryKeys(null, null, entitySet.getDbTableNameLocalIyo());
                for (; rsKey.next();) {
                    // キー名は利用しない: rsKey.getString("PK_NAME");
                    String colName = rsKey.getString("COLUMN_NAME");

                    CsdlPropertyRef propertyRef = new CsdlPropertyRef();
                    propertyRef.setName(colName);
                    keyRefList.add(propertyRef);
                }

                entityType.setKey(keyRefList);
            }

            // 構築結果を記憶。
            entitySet.setEntityType(entityType);
            return entityType;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException("DB meta 取得失敗:" + ex.toString(), ex);
        }
    }
}
