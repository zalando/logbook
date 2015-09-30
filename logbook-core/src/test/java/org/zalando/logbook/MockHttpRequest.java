package org.zalando.logbook;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

final class MockHttpRequest implements HttpRequest {

    @Override
    public String getRemote() {
        return "127.0.0.1";
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public String getRequestURI() {
        return "/test";
    }

    @Override
    public Multimap<String, String> getParameters() {
        return ImmutableMultimap.of(
                "limit", "1"
        );
    }

    @Override
    public Multimap<String, String> getHeaders() {
        return ImmutableMultimap.of(
                "Accept", "application/json",
                "Content-Type", "text/plain"
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
        return "Hello, world!".getBytes(getCharset());
    }

}
