package org.zalando.logbook.servlet;

import org.zalando.logbook.ForwardingRawHttpRequest;
import org.zalando.logbook.HttpRequest;

import java.io.IOException;

final class UnauthorizedRawHttpRequest extends ForwardingRawHttpRequest {

    private final RemoteRequest request;

    UnauthorizedRawHttpRequest(final RemoteRequest request) {
        this.request = request;
    }

    @Override
    protected RemoteRequest delegate() {
        return request;
    }

    @Override
    public HttpRequest withBody() throws IOException {
        return new UnauthorizedHttpRequest(delegate());
    }

}
