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

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.basic.BasicDbUtil;

/**
 * 実際に返却するデータ本体を組み上げるクラス.
 * 
 * このクラスには、テスト用データを構築する処理も含む.
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
     */
    public static void createTable(final Connection conn) {
        // System.err.println("TRACE: 作業用データベーステーブルを作成");

        try (var stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " //
                + "ODataAppInfos (" //
                + "KeyName VARCHAR(20) NOT NULL" // primary key. これほんとは Key とかにして Key = version とかで分岐したい.
                + ",KeyValue VARCHAR(255)" //
                + ",PRIMARY KEY(KeyName)" //
                + ")")) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalArgumentException("テーブル作成に失敗: " + ex.toString(), ex);
        }

        try (var stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " //
                + "MyProducts (" //
                + "ID INT NOT NULL" // primary key.
                + ",Name VARCHAR(80) NOT NULL" //
                + ",Description VARCHAR(250)" //

                // テスト実験するためのフィールド.

                // SByte, h2:TINYINT(?)
                + ",Sbyte1 TINYINT DEFAULT 127" //

                // Int16, h2:SMALLINT
                + ",Int16a SMALLINT DEFAULT 32767" //

                // Int32, h2:INT
                + ",Int32a INT DEFAULT 2147483647" //

                // Int64, h2:BIGINT
                + ",Int64a BIGINT DEFAULT 2147483647" //

                // 【諸事情によりINT MAX以上をサンプルから割愛】 + ",Int64b BIGINT DEFAULT 99999999999" //
                // 【諸事情によりINT MAX以上をサンプルから割愛】 + ",Int64max BIGINT DEFAULT 9223372036854775807"

                // Decimal, h2:DECIMAL
                + ",Decimal1 DECIMAL(6,2) DEFAULT 1234.56" //

                // String, h2:VARCHAR, h2:CHAR
                + ",StringChar2 CHAR(2) DEFAULT 'C2'" //
                + ",StringVar255 VARCHAR(255) DEFAULT 'VARCHAR255'" //
                + ",StringVar65535 VARCHAR(65535) DEFAULT 'VARCHAR65535'" //

                // H2の全文検索の対象外: Binary, h2:BINARY

                // Boolean, h2:BOOLEAN
                + ",Boolean1 BOOLEAN DEFAULT FALSE NOT NULL" //

                // Single, h2:REAL
                + ",Single1 REAL DEFAULT 123.456789" //

                // Double, h2:DOUBLE
                + ",Double1 DOUBLE DEFAULT 123.4567890123" //

                // Date, h2:DATE
                + ",Date1 DATE DEFAULT CURRENT_DATE() NOT NULL" //

                // DateTimeOffset, h2:TIMESTAMP
                + ",DateTimeOffset1 TIMESTAMP DEFAULT CURRENT_TIMESTAMP() NOT NULL" //

                // TimeOfDay, h2:TIME
                + ",TimeOfDay1 TIME DEFAULT CURRENT_TIME()" //

                + ",PRIMARY KEY(ID)" //
                + ")")) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalArgumentException("テーブル作成に失敗: " + ex.toString(), ex);
        }
    }

    /**
     * 情報を格納するためのテーブルをセットアップします。
     * 
     * @param conn データベース接続。
     */
    public static void setupTableData(final Connection conn) {
        try (var stmt = conn.prepareStatement("SELECT COUNT(*) FROM ODataAppInfos")) {
            stmt.executeQuery();
            var rset = stmt.getResultSet();
            rset.next();
            if (rset.getInt(1) > 0) {
                return;
            }
        } catch (SQLException ex) {
            throw new IllegalArgumentException("検索失敗:" + ex.toString(), ex);
        }

        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println( //
                    "OData v4: build sample data: " + " (Oiyokan: " + OiyokanConstants.VERSION + ")");

        // 全文検索関連の準備.
        try {
            try (PreparedStatement stmt = conn
                    .prepareStatement("CREATE ALIAS IF NOT EXISTS FT_INIT FOR \"org.h2.fulltext.FullText.init\"")) {
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("CALL FT_INIT()")) {
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new IllegalArgumentException("全文検索の初期設定に失敗: " + ex.toString(), ex);
        }

        ///////////////////////////////////////////
        // バージョン情報に関するデータの追加
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
            throw new IllegalArgumentException("テーブル作成に失敗: " + ex.toString(), ex);
        }

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
            throw new IllegalArgumentException("テーブル作成に失敗: " + ex.toString(), ex);
        }

        try {
            try (PreparedStatement stmt = conn.prepareStatement("CALL FT_CREATE_INDEX('PUBLIC', 'MyProducts', NULL)")) {
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("CALL FT_REINDEX()")) {
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new IllegalArgumentException("全文検索の初期設定に失敗: " + ex.toString(), ex);
        }
    }
}
