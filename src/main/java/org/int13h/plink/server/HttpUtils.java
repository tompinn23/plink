package org.int13h.plink.server;

import org.apache.hc.core5.http.ContentType;

import java.nio.charset.Charset;

public class HttpUtils {

    public static Charset getCharset(CharSequence contentType, Charset defaultCharset) {
        if(contentType == null) {
            return defaultCharset;
        }
        var ct = ContentType.parseLenient(contentType);
        return ct.getCharset(defaultCharset);
    }
}
