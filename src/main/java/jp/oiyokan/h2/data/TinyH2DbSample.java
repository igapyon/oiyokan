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
package jp.oiyokan.h2.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;

import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.basic.BasicDbUtil;

/**
 * Oiyokan (OData v4 server) が動作する際に必要になる内部データおよびサンプルデータを提供.
 */
public class TinyH2DbSample {
    // 増殖カウント. 負荷確認したい場合は 5000程度に増やす.
    private static final int ZOUSYOKU = 100;

    private TinyH2DbSample() {
    }

    /**
     * 情報を格納するためのテーブルをセットアップします。
     * 
     * @param conn データベース接続。
     * @return true:新規作成, false:既に存在.
     */
    public static boolean createTable(final Connection conn) throws ODataApplicationException {
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println( //
                    "OData v4: setup internal table: " + " (Oiyokan: " + OiyokanConstants.VERSION + ")");

        // Oiyokan が動作する上で必要なテーブルのセットアップ.
        try (var stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " //
                + "ODataAppInfos (" //
                + "KeyName VARCHAR(20) NOT NULL" //
                + ",KeyValue VARCHAR(255)" //
                + ",PRIMARY KEY(KeyName)" //
                + ")")) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new ODataApplicationException("テーブル作成に失敗: " + ex.toString(), 500, Locale.ENGLISH);
        }

        // ODataAppInfos が既に存在するかどうか確認. 存在する場合は処理中断.
        try (var stmt = conn.prepareStatement("SELECT COUNT(*) FROM ODataAppInfos")) {
            stmt.executeQuery();
            var rset = stmt.getResultSet();
            rset.next();
            if (rset.getInt(1) > 0) {
                // すでにテーブルがセットアップ済み。処理中断します。
                return false;
            }
        } catch (SQLException ex) {
            throw new ODataApplicationException("Fail to SQL: " + ex.toString(), 500, Locale.ENGLISH, ex);
        }

        ///////////////////////////////////////////
        // ODataAppInfos にバージョン情報などデータの追加
        try (var stmt = conn.prepareStatement("INSERT INTO ODataAppInfos (KeyName, KeyValue) VALUES ("
                + BasicDbUtil.getQueryPlaceholderString(2) + ")")) {
            stmt.setString(1, "Version");
            stmt.setString(2, OiyokanConstants.VERSION);
            stmt.executeUpdate();

            stmt.clearParameters();
            stmt.setString(1, "Provider");
            stmt.setString(2, OiyokanConstants.NAME);
            stmt.executeUpdate();

            conn.commit();
        } catch (SQLException ex) {
            throw new ODataApplicationException("テーブル作成に失敗: " + ex.toString(), 500, Locale.ENGLISH);
        }

        final String[] sqls = OiyokanResourceSqlUtil.loadOiyokanResourceSql("oiyokan-testdb.sql");
        for (String sql : sqls) {
            try (var stmt = conn.prepareStatement(sql.trim())) {
                // System.err.println("SQL: " + sql);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                throw new ODataApplicationException("SQL実行に失敗: " + ex.toString(), 500, Locale.ENGLISH);
            }
        }

        // 新規作成.
        return true;
    }

    /**
     * 情報を格納するためのテーブルをセットアップします。
     * 
     * @param conn データベース接続。
     */
    public static void setupTableData(final Connection conn) throws ODataApplicationException {

        ///////////////////////
        // ダミーなデータの追加
        try (var stmt = conn.prepareStatement("INSERT INTO MyProducts (ID, Name, Description) VALUES ("
                + BasicDbUtil.getQueryPlaceholderString(3) + ")")) {
            int idCounter = 1;
            stmt.setInt(1, idCounter++);
            stmt.setString(2, "MacBookPro16,2");
            stmt.setString(3, "MacBook Pro (13-inch, 2020, Thunderbolt 3ポートx 4)");
            stmt.executeUpdate();

            stmt.clearParameters();
            stmt.setInt(1, idCounter++);
            stmt.setString(2, "MacBookPro E2015");
            stmt.setString(3, "MacBook Pro (Retina, 13-inch, Early 2015)");
            stmt.executeUpdate();

            stmt.clearParameters();
            stmt.setInt(1, idCounter++);
            stmt.setString(2, "Surface Laptop 2");
            stmt.setString(3, "Surface Laptop 2, 画面:13.5 インチ PixelSense ディスプレイ, インテル Core");
            stmt.executeUpdate();

            conn.commit();

            for (int index = 0; index < ZOUSYOKU; index++) {
                stmt.clearParameters();
                stmt.setInt(1, idCounter++);
                stmt.setString(2, "PopTablet" + index);
                stmt.setString(3, "増殖タブレット Laptop Intel Core" + index);
                stmt.executeUpdate();
            }
            conn.commit();

            for (int index = 0; index < ZOUSYOKU; index++) {
                stmt.clearParameters();
                stmt.setInt(1, idCounter++);
                stmt.setString(2, "DummyPC" + index);
                stmt.setString(3, "ダミーなPC" + index);
                stmt.executeUpdate();
            }
            conn.commit();
        } catch (SQLException ex) {
            throw new ODataApplicationException("テーブル作成に失敗: " + ex.toString(), 500, Locale.ENGLISH);
        }

        try {
            try (PreparedStatement stmt = conn.prepareStatement("CALL FT_CREATE_INDEX('PUBLIC', 'MyProducts', NULL)")) {
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("CALL FT_REINDEX()")) {
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new ODataApplicationException("全文検索の初期設定に失敗: " + ex.toString(), 500, Locale.ENGLISH);
        }
    }
}
