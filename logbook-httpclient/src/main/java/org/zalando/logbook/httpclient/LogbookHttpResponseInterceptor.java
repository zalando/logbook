package org.zalando.logbook.httpclient;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.protocol.HttpContext;
import org.zalando.logbook.Correlator;

import java.io.IOException;
import java.util.Optional;

/**
 * A response interceptor for synchronous responses. For {@link HttpAsyncClient} support, please use
 * {@link LogbookHttpAsyncResponseConsumer} instead.
 *
 * @see LogbookHttpRequestInterceptor
 * @see LogbookHttpAsyncResponseConsumer
 */
public final class LogbookHttpResponseInterceptor implements HttpResponseInterceptor {

    @Override
    public void process(final HttpResponse original, final HttpContext context) throws HttpException, IOException {
        final Optional<Correlator> correlator = findCorrelator(context);

        if (correlator.isPresent()) {
            correlator.get().write(new RemoteResponse(original));
        }
    }

    private Optional<Correlator> findCorrelator(final HttpContext context) {
        return Optional.ofNullable(context.getAttribute(Attributes.CORRELATOR)).map(Correlator.class::cast);
    }

}
