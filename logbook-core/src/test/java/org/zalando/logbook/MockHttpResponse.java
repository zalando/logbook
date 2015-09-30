package org.zalando.logbook;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

final class MockHttpResponse implements HttpResponse {

    @Override
    public int getStatus() {
        return 200;
    }

    @Override
    public Multimap<String, String> getHeaders() {
        return ImmutableMultimap.of(
                "Content-Type", "application/json"
        );
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public Charset getCharset() {
        return StandardCharsets.UTF_8;
    }

    @Override
    public byte[] getBody() {
        return "{\"success\":true}".getBytes(getCharset());
    }

}
