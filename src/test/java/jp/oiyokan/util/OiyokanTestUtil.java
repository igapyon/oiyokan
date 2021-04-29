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
package jp.oiyokan.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;

import jp.oiyokan.OiyokanEdmProvider;
import jp.oiyokan.OiyokanEntityCollectionProcessor;
import jp.oiyokan.OiyokanEntityProcessor;

/**
 * Utility class for OData test
 */
public class OiyokanTestUtil {
    public static ODataResponse callGet(String rawODataPath, String rawQueryPath) throws Exception {
        final ODataHttpHandler handler = OiyokanTestUtil.getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/odata4.svc");
        req.setRawODataPath(rawODataPath);
        req.setRawQueryPath(rawQueryPath);
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() //
                + (rawQueryPath != null && rawQueryPath.trim().length() > 0 ? "?" : "") //
                + req.getRawQueryPath());

        return handler.process(req);
    }

    public static ODataHttpHandler getHandler() throws Exception {
        final OData odata = OData.newInstance();

        // EdmProvider を登録.
        final ServiceMetadata edm = odata.createServiceMetadata(new OiyokanEdmProvider(), new ArrayList<>());
        final ODataHttpHandler handler = odata.createHandler(edm);

        // EntityCollectionProcessor を登録.
        handler.register(new OiyokanEntityCollectionProcessor());

        // EntityProcessor を登録.
        handler.register(new OiyokanEntityProcessor());

        return handler;
    }

    /**
     * InputStream を String に変換.
     * 
     * @param inStream 入力ストリーム.
     * @return 文字列.
     * @throws IOException 入出力例外が発生した場合.
     */
    public static String stream2String(InputStream inStream) throws IOException {
        if (inStream == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"))) {
            for (;;) {
                String line = reader.readLine();
                if (line == null)
                    break;
                builder.append(line);
            }
        }
        return builder.toString();
    }

    /////////////////
    // INSERT

    public static ODataResponse callPost(String rawODataPath, String bodyJson) throws Exception {
        final ODataHttpHandler handler = OiyokanTestUtil.getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.POST);
        req.setRawBaseUri("http://localhost:8080/odata4.svc");
        req.setRawODataPath(rawODataPath);
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath());
        req.addHeader("Content-type", "application/json; odata.metadata=minimal");

        req.setBody(new ByteArrayInputStream(bodyJson.getBytes("UTF-8")));

        return handler.process(req);
    }

    /////////////////
    // DELETE

    public static ODataResponse callDelete(String rawODataPath) throws Exception {
        final ODataHttpHandler handler = OiyokanTestUtil.getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.DELETE);
        req.setRawBaseUri("http://localhost:8080/odata4.svc");
        req.setRawODataPath(rawODataPath);
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath());
        req.addHeader("Content-type", "application/json; odata.metadata=minimal");

        return handler.process(req);
    }

    /////////////////
    // UPDATE

    public static ODataResponse callPatch(String rawODataPath, String bodyJson, final boolean ifMatch,
            final boolean ifNoneMatch) throws Exception {
        final ODataHttpHandler handler = OiyokanTestUtil.getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.PATCH);
        req.setRawBaseUri("http://localhost:8080/odata4.svc");
        req.setRawODataPath(rawODataPath);
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath());
        req.addHeader("Content-type", "application/json; odata.metadata=minimal");
        if (ifMatch) {
            req.addHeader("If-Match", "*");
        }
        if (ifNoneMatch) {
            req.addHeader("If-None-Match", "*");
        }

        req.setBody(new ByteArrayInputStream(bodyJson.getBytes("UTF-8")));

        return handler.process(req);
    }

    private static final Object lock = new Object();
    private static volatile int nextUniqueId = 20000;

    public static final int getNextUniqueId() {
        synchronized (lock) {
            return nextUniqueId++;
        }
    }

    public static final String getValueFromResultByKey(final String result, final String key) {
        final Pattern pat = Pattern.compile("[,][\"]" + key + "[\"][:].*?[,|}]");
        final Matcher mat = pat.matcher(result);

        for (; mat.find();) {
            final String word = mat.group();
            // System.err.println("word:" + word);
            final String idColonNumber = word.substring(1, word.length() - 1);
            // System.err.println("idColonNumber" + idColonNumber);
            final String number = idColonNumber.substring(3 + key.length());
            return number;
        }
        throw new IllegalArgumentException("Unexpected: result:" + result + ", key:" + key);
    }
}
