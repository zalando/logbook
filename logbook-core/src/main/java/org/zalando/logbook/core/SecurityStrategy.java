package org.zalando.logbook.core;

import org.apiguardian.api.API;
import org.zalando.logbook.api.Correlation;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.HttpResponse;
import org.zalando.logbook.api.Precorrelation;
import org.zalando.logbook.api.Sink;
import org.zalando.logbook.api.Strategy;

import java.io.IOException;

import static org.apiguardian.api.API.Status.MAINTAINED;

/**
 * A {@link SecurityStrategy} is a {@link Strategy strategy} which is meant to be used in server-side environments to
 * give the best possible compromise between security and observability.
 * <p>
 * This strategy discards requests bodies.
 */
@API(status = MAINTAINED)
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
