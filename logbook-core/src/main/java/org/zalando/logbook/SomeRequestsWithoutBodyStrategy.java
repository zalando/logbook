package org.zalando.logbook;

import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
public final class SomeRequestsWithoutBodyStrategy implements Strategy {

    @Override
    public HttpRequest process(final HttpRequest request) throws IOException {
        return request.getRequestUri().contains("/attachments") ? request.withoutBody() : request.withBody();
    }

}
