package org.zalando.logbook.httpclient;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apiguardian.api.API;
import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class LogbookHttpRequestInterceptor implements HttpRequestInterceptor {

    private final Logbook logbook;

    public LogbookHttpRequestInterceptor(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public void process(final HttpRequest httpRequest, final HttpContext context) throws HttpException, IOException {
        final LocalRequest request = new LocalRequest(httpRequest);
        final Optional<Correlator> correlator = logbook.write(request);
        correlator.ifPresent(writeCorrelator(context));
    }

    private Consumer<Correlator> writeCorrelator(final HttpContext context) {
        return correlator -> context.setAttribute(Attributes.CORRELATOR, correlator);
    }

}
