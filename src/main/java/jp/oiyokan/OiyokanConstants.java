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
package jp.oiyokan;

/**
 * Oiyokan (OData v4 server) の定数.
 */
public class OiyokanConstants {
    /**
     * Oiyokan の名前.
     */
    public static final String NAME = "Oiyokan";

    /**
     * Oiyokan のバージョン番号
     */
    public static final String VERSION = "1.7.20210426a";

    /**
     * 実験的な $search 機能(全文検索)が有効化されているかどうか。
     * 
     * 当面、リリース時には false.
     */
    public static final boolean IS_EXPERIMENTAL_SEARCH_ENABLED = false;

    /**
     * 接続先リソースの Databaseの型の列挙. 基本的にはリレーショナルデータベースを想定.
     */
    public enum DatabaseType {
        /** h2 database */
        h2,
        /** Postgres */
        postgres,
        /** MySQL */
        MySQL,
        /** MSSQLSV 2008 */
        SQLSV2008,
        /** ORCL18 */
        ORCL18,
        /** BigQuery : placeholder, not supported, not tested */
        BigQuery,
    };

    /**
     * 内部DBへの定義名. この DBはアプリ起動都度初期化される揮発的なもの. Oiyo および Oiyokan 設定及びバージョン番号を格納.
     */
    public static final String OIYOKAN_KAN_DB = "oiyokanKan";
}
