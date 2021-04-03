package jp.oiyokan.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;

import jp.oiyokan.OiyokanEdmProvider;
import jp.oiyokan.OiyokanEntityCollectionProcessor;

/**
 * Utility class for OData test
 */
public class OiyokanTestUtil {
    public static ODataResponse callRequestGetResponse(String rawODataPath, String rawQueryPath) throws Exception {
        final ODataHttpHandler handler = OiyokanTestUtil.getHandler();
        final ODataRequest req = new ODataRequest();
        req.setMethod(HttpMethod.GET);
        req.setRawBaseUri("http://localhost:8080/odata4.svc");
        req.setRawODataPath(rawODataPath);
        req.setRawQueryPath(rawQueryPath);
        req.setRawRequestUri(req.getRawBaseUri() + req.getRawODataPath() + "?" + req.getRawQueryPath());

        return handler.process(req);
    }

    public static ODataHttpHandler getHandler() throws Exception {
        final OData odata = OData.newInstance();

        // EdmProvider を登録.
        final ServiceMetadata edm = odata.createServiceMetadata(new OiyokanEdmProvider(), new ArrayList<>());
        final ODataHttpHandler handler = odata.createHandler(edm);

        // EntityCollectionProcessor を登録.
        handler.register(new OiyokanEntityCollectionProcessor());
        return handler;
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
     * InputStream を String に変換.
     * 
     * @param inStream 入力ストリーム.
     * @return 文字列.
     * @throws IOException 入出力例外が発生した場合.
     */
    public static String stream2String(InputStream inStream) throws IOException {
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
}
