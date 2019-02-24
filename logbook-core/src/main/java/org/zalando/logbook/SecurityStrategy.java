package org.zalando.logbook;

import java.io.IOException;

/**
 * A {@link SecurityStrategy} is a {@link Strategy strategy} which is meant to be used in server-side environments to
 * give the best possible compromise between security and observability.
 *
 * This strategy discards requests bodies.
 */
public final class SecurityStrategy implements Strategy {

    @Override
    public HttpRequest process(final HttpRequest request) {
        return request.withoutBody();
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request, final Sink sink) {
        // defer decision until response is available
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response,
            final Sink sink) throws IOException {

        final int status = response.getStatus();
        if (status == 401 || status == 403) {
            sink.writeBoth(correlation, request, response);
        }
    }

}
