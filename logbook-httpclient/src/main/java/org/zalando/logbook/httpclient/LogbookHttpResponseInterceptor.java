package org.zalando.logbook.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.protocol.HttpContext;
import org.apiguardian.api.API;
import org.zalando.logbook.Correlator;

import java.io.IOException;
import java.util.Optional;

import static org.apiguardian.api.API.Status.STABLE;
import static org.zalando.fauxpas.FauxPas.throwingConsumer;

/**
 * A response interceptor for synchronous responses. For {@link HttpAsyncClient} support, please use
 * {@link LogbookHttpAsyncResponseConsumer} instead.
 *
 * @see LogbookHttpRequestInterceptor
 * @see LogbookHttpAsyncResponseConsumer
 */
@API(status = STABLE)
public final class LogbookHttpResponseInterceptor implements HttpResponseInterceptor {

    @Override
    public void process(final HttpResponse original, final HttpContext context) throws IOException {
        final Optional<Correlator> correlator = findCorrelator(context);

        correlator.ifPresent(throwingConsumer(c -> c.write(new RemoteResponse(original))));
    }

    private Optional<Correlator> findCorrelator(final HttpContext context) {
        return Optional.ofNullable(context.getAttribute(Attributes.CORRELATOR)).map(Correlator.class::cast);
    }

}
