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
    public static final String VERSION = "20210327a";

    /**
     * Oiyokan がリクエストを処理する際の 'OData v4' からはじまるトレースを出力するかどうか。
     */
    public static final boolean IS_TRACE_ODATA_V4 = true;

    /**
     * 内部DBへの定義名. OCSDL およびバージョン番号を格納.
     */
    public static final String OIYOKAN_INTERNAL_DB = "oiyokanInternal";

    /**
     * 内部DBへの定義名. ほぼビルドテスト用途.
     */
    public static final String OIYOKAN_INTERNAL_TARGET_DB = "oiyokanInternalTarget";
}
