package org.zalando.logbook;

import java.io.IOException;

/**
 * Proof of concept
 */
final class SomeRequestsWithoutBodyStrategy implements Strategy {

    @Override
    public HttpRequest process(final HttpRequest request) throws IOException {
        return request.getRequestUri().contains("/attachments") ? request.withoutBody() : request.withBody();
    }

}
