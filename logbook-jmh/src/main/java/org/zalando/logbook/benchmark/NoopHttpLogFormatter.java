package org.zalando.logbook.benchmark;

import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;

public class NoopHttpLogFormatter implements HttpLogFormatter {

    private static final String EMPTY = "{}";

    @Override
    public String format(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        return EMPTY;
    }

    @Override
    public String format(final Correlation correlation, final HttpResponse response) throws IOException {
        return EMPTY;
    }

}
