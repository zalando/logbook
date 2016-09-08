package org.zalando.logbook.servlet;

import org.zalando.logbook.ForwardingRawHttpRequest;
import org.zalando.logbook.HttpRequest;

import java.io.IOException;

// TODO replace with filtering
final class UnauthorizedRawHttpRequest implements ForwardingRawHttpRequest {

    private final RemoteRequest request;

    UnauthorizedRawHttpRequest(final RemoteRequest request) {
        this.request = request;
    }

    @Override
    public RemoteRequest delegate() {
        return request;
    }

    @Override
    public HttpRequest withBody() throws IOException {
        return new UnauthorizedHttpRequest(delegate());
    }

}
