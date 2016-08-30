package org.zalando.logbook.servlet;

import org.zalando.logbook.ForwardingHttpRequest;
import org.zalando.logbook.HttpRequest;

import java.io.IOException;

final class UnauthorizedHttpRequest extends ForwardingHttpRequest {

    private final RemoteRequest request;

    public UnauthorizedHttpRequest(final RemoteRequest request) {
        this.request = request;
    }

    @Override
    protected HttpRequest delegate() {
        return request;
    }

    @Override
    public byte[] getBody() throws IOException {
        return new byte[0];
    }

    @Override
    public String getBodyAsString() throws IOException {
        return "";
    }

}
