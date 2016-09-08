package org.zalando.logbook.servlet;

import org.zalando.logbook.ForwardingHttpRequest;
import org.zalando.logbook.HttpRequest;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

// TODO replace with filtering
final class UnauthorizedHttpRequest implements ForwardingHttpRequest {

    private final RemoteRequest request;

    public UnauthorizedHttpRequest(final RemoteRequest request) {
        this.request = request;
    }

    @Override
    public HttpRequest delegate() {
        return request;
    }

    @Override
    public byte[] getBody() throws IOException {
        return getBodyAsString().getBytes(UTF_8);
    }

    @Override
    public String getBodyAsString() throws IOException {
        return "<skipped>";
    }

}
