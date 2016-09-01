package org.zalando.logbook.httpclient;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public final class LogbookHttpRequestInterceptor implements HttpRequestInterceptor {

    private final Logbook logbook;
    private final Localhost localhost;

    public LogbookHttpRequestInterceptor(final Logbook logbook) {
        this(logbook, Localhost.resolve());
    }

    LogbookHttpRequestInterceptor(final Logbook logbook, final Localhost localhost) {
        this.logbook = logbook;
        this.localhost = localhost;
    }

    @Override
    public void process(final HttpRequest httpRequest, final HttpContext context) throws HttpException, IOException {
        final LocalRequest request = new LocalRequest(httpRequest, localhost);
        final Optional<Correlator> correlator = logbook.write(request);
        correlator.ifPresent(writeCorrelator(context));
    }

    private Consumer<Correlator> writeCorrelator(final HttpContext context) {
        return correlator -> context.setAttribute(Attributes.CORRELATOR, correlator);
    }

}
