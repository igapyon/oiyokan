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
package jp.oiyokan.common;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.dto.OiyoSettings;

/**
 * Oiyokan の実行に関する基本的な情報。
 */
public class OiyoInfo {
    /**
     * JSONから読み込んだ oiyokan-settings.json の内容.
     */
    private OiyoSettings settings = null;

    /**
     * パスフレーズ。O
     */
    private String passphrase = null;

    /**
     * oiyokan-settings.json 設定情報の取得.
     * 
     * @return oiyokan-settings.json setting info.
     */
    public OiyoSettings getSettings() {
        return settings;
    }

    /**
     * oiyokan-settings.json 設定情報の設定.
     * 
     * @param settings oiyokan-settings.json setting info.
     */
    public void setSettings(OiyoSettings settings) {
        this.settings = settings;
    }

    /**
     * パスフレーズを取得.
     * 
     * @return パスフレーズ.
     */
    public String getPassphrase() {
        if (passphrase == null) {
            final String lookup = System.getenv(OiyokanConstants.OIYOKAN_PASSPHRASE);
            if (lookup == null || lookup.trim().length() == 0) {
                passphrase = OiyokanConstants.OIYOKAN_PASSPHRASE;
            } else {
                passphrase = lookup;
            }
        }

        return passphrase;
    }
}
