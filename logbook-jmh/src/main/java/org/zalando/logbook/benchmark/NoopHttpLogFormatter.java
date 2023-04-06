package org.zalando.logbook.benchmark;

import org.zalando.logbook.api.Correlation;
import org.zalando.logbook.api.HttpLogFormatter;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.HttpResponse;
import org.zalando.logbook.api.Precorrelation;

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
