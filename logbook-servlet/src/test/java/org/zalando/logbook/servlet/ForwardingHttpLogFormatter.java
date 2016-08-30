package org.zalando.logbook.servlet;

import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;

// non final so we can wrap a spy around it
class ForwardingHttpLogFormatter implements HttpLogFormatter {

    private final HttpLogFormatter formatter;

    protected ForwardingHttpLogFormatter(final HttpLogFormatter formatter) {
        this.formatter = formatter;
    }

    protected HttpLogFormatter delegate() {
        return formatter;
    }

    @Override
    public String format(final Precorrelation<HttpRequest> precorrelation) throws IOException {
        return delegate().format(precorrelation);
    }

    @Override
    public String format(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        return delegate().format(correlation);
    }

    @Override
    public String toString() {
        return delegate().toString();
    }
}
