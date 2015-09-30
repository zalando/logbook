package org.zalando.logbook;

import com.google.common.collect.Multimap;

import java.io.IOException;
import java.nio.charset.Charset;

// TODO find common interface for Raw+Normal
public interface RawHttpResponse {

    int getStatus();

    Multimap<String, String> getHeaders();

    Charset getCharset();

    HttpResponse withBody() throws IOException;

}
