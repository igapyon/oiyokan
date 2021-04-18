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
package jp.oiyokan.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.dto.OiyoSettingsDatabase;

/**
 * Oiyokan (OData v4 server) が動作する際に必要になる内部管理データベースのバージョン情報および Oiyo情報 をセットアップ.
 */
public class OiyokanKanDatabase {
    private static final Log log = LogFactory.getLog(OiyokanKanDatabase.class);

    private OiyokanKanDatabase() {
    }

    /**
     * 内部データベースの情報一式をセットアップします。
     * 
     * @return true:新規作成, false:既に存在.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static synchronized boolean setupKanDatabase(OiyoInfo oiyoInfo) throws ODataApplicationException {
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println( //
                    "OData v4: setup oiyokanKan database (Oiyokan: " + OiyokanConstants.VERSION + ")");

        OiyoSettingsDatabase settingsInterDatabase = OiyoInfoUtil.getOiyoDatabaseByName(oiyoInfo,
                OiyokanConstants.OIYOKAN_KAN_DB);

        try (Connection connInterDb = OiyoCommonJdbcUtil.getConnection(settingsInterDatabase)) {
            // Internal Database の バージョン情報および Oiyokanテーブルを setup.

            // Oiyokan が動作する上で必要なテーブルのセットアップ.
            try (var stmt = connInterDb.prepareStatement("CREATE TABLE IF NOT EXISTS " //
                    + "Oiyokan (" //
                    + "KeyName VARCHAR(20) NOT NULL" //
                    + ",KeyValue VARCHAR(255)" //
                    + ",PRIMARY KEY(KeyName)" //
                    + ")")) {
                stmt.executeUpdate();
            } catch (SQLException ex) {
                // [M027] UNEXPECTED: Fail to create local table: Oiyokan
                System.err.println(OiyokanMessages.IY7115 + ": " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.IY7115, 500, Locale.ENGLISH);
            }

            // ODataAppInfos が既に存在するかどうか確認. 存在する場合は処理中断.
            try (var stmt = connInterDb.prepareStatement("SELECT COUNT(*) FROM Oiyokan")) {
                stmt.executeQuery();
                var rset = stmt.getResultSet();
                rset.next();
                if (rset.getInt(1) > 0) {
                    // すでにテーブルがセットアップ済み。処理中断します。
                    return false;
                }
            } catch (SQLException ex) {
                // [M028] UNEXPECTED: Fail to check local table exists: Oiyokan
                System.err.println(OiyokanMessages.IY7116 + ": " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.IY7116, 500, Locale.ENGLISH);
            }

            ///////////////////////////////////////////
            // 内部データの作成に突入.
            if (OiyokanConstants.IS_TRACE_ODATA_V4)
                System.err.println( //
                        "OData v4: setup internal data " + " (Oiyokan: " + OiyokanConstants.VERSION + ")");

            ///////////////////////////////////////////
            // ODataAppInfos にバージョン情報などデータの追加
            try (var stmt = connInterDb.prepareStatement("INSERT INTO Oiyokan (KeyName, KeyValue) VALUES ("
                    + OiyoCommonJdbcUtil.getQueryPlaceholderString(2) + ")")) {
                stmt.setString(1, "Version");
                stmt.setString(2, OiyokanConstants.VERSION);
                stmt.executeUpdate();

                stmt.clearParameters();
                stmt.setString(1, "Provider");
                stmt.setString(2, OiyokanConstants.NAME);
                stmt.executeUpdate();

                connInterDb.commit();
            } catch (SQLException ex) {
                // [M029] UNEXPECTED: Fail to execute SQL for local internal table
                System.err.println(OiyokanMessages.IY7117 + ": " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.IY7117, 500, Locale.ENGLISH);
            }

            // 新規作成.
            return true;
        } catch (SQLException ex) {
            // [M004] UNEXPECTED: Database error in setup internal database.
            System.err.println(OiyokanMessages.IY7104 + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.IY7104, 500, Locale.ENGLISH);
        }
    }
}
