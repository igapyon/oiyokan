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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import jp.oiyokan.OiyokanMessages;

/**
 * oiyokan-sampledb.sql ファイルに関する処理。
 */
public class OiyokanResourceSqlUtil {
    /**
     * 指定されたSQLリソースからSQL文の配列を読み込み.
     * 
     * @param resourceSqlFileName リソースのSQLファイルの名前.
     * @return 読み込まれて分解されたSQL文の配列.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static String[] loadOiyokanResourceSql(final String resourceSqlFileName) throws ODataApplicationException {
        // このメソッドは、テストデータベースを生成する際に呼び出されます。
        // resources から読み込み。
        final ClassPathResource cpres = new ClassPathResource(resourceSqlFileName);
        try (InputStream inStream = cpres.getInputStream()) {
            String sqlresources = StreamUtils.copyToString(inStream, Charset.forName("UTF-8"));
            final String[] sqls = sqlresources.split(";");
            return sqls;
        } catch (IOException ex) {
            // [M022] UNEXPECTED: Fail to load setting SQL file
            System.err.println(OiyokanMessages.M022 + ": " + resourceSqlFileName + ": " + ex.toString());
            throw new ODataApplicationException(OiyokanMessages.M022, 500, Locale.ENGLISH);
        }
    }
}
