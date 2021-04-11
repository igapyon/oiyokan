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

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;

/**
 * Oiyokan 関連の URL まわりユーティリティクラス.
 */
public class BasicUrlUtil {
    private BasicUrlUtil() {
    }

    /**
     * 与えられた文字列を URL クエリとしてエンコード.
     * 
     * @param inputString URLクエリ.
     * @return エンコード済みURLクエリ.
     */
    public static String encodeUrlQuery(String inputString) {
        BitSet urlSafe = new BitSet();
        urlSafe.set('0', '9' + 1);
        urlSafe.set('-');
        urlSafe.set('=');
        urlSafe.set('&');
        urlSafe.set('%');
        urlSafe.set('A', 'Z' + 1);
        urlSafe.set('a', 'z' + 1);
        try {
            return new String(URLCodec.encodeUrl(urlSafe, inputString.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * 与えられた エンコード済み URL をデコード.
     * 
     * @param encodedString エンコード済みURL.
     * @return もとのURL.
     */
    public static String decodeUrlQuery(String encodedString) {
        try {
            return new URLCodec("UTF-8").decode(encodedString);
        } catch (DecoderException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * 与えられた文字列を キー用にエンコード.
     * 
     * @param inputString URLクエリ.
     * @return エンコード済みURLクエリ.
     */
    public static String encodeUrl4Key(String inputString) {
        BitSet urlSafe = new BitSet();
        urlSafe.set('0', '9' + 1);
        urlSafe.set('_');
        urlSafe.set('A', 'Z' + 1);
        urlSafe.set('a', 'z' + 1);
        try {
            return new String(URLCodec.encodeUrl(urlSafe, inputString.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
