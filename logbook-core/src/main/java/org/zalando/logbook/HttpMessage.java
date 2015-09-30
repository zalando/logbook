package org.zalando.logbook;

import com.google.common.collect.Multimap;

import java.io.IOException;
import java.nio.charset.Charset;

public interface HttpMessage {

    Multimap<String, String> getHeaders();

    String getContentType();

    Charset getCharset();

    byte[] getBody() throws IOException;

    default String getBodyAsString() throws IOException {
        return new String(getBody(), getCharset());
    }

}
