package org.zalando.logbook.servlet;

import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;

// non final so we can wrap a spy around it
public class ForwardingHttpLogFormatter implements HttpLogFormatter {

    private final HttpLogFormatter formatter;

    public ForwardingHttpLogFormatter(final HttpLogFormatter formatter) {
        this.formatter = formatter;
    }

    private HttpLogFormatter delegate() {
        return formatter;
    }

    @Override
    public String format(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        return delegate().format(precorrelation, request);
    }

    @Override
    public String format(final Correlation correlation, final HttpResponse response)
            throws IOException {
        return delegate().format(correlation, response);
    }

    @Override
    public String toString() {
        return delegate().toString();
    }

}
