package org.zalando.logbook;

import com.google.common.collect.Multimap;

import java.io.IOException;
import java.nio.charset.Charset;

// TODO find common interface for Raw+Normal
public interface RawHttpRequest {

    String getRemote();

    String getMethod();

    String getRequestURI();

    Multimap<String, String> getHeaders();

    Charset getCharset();

    Multimap<String, String> getParameters();

    HttpRequest withBody() throws IOException;

}
