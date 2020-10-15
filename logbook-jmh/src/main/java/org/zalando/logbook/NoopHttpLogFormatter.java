package org.zalando.logbook;

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
