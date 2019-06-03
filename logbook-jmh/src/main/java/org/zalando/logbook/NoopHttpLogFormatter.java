package org.zalando.logbook;

import java.io.IOException;

public class NoopHttpLogFormatter implements HttpLogFormatter {

    private static final String EMPTY = "{}";
    
    @Override
    public String format(Precorrelation precorrelation, HttpRequest request) throws IOException {
        return EMPTY;
    }

    @Override
    public String format(Correlation correlation, HttpResponse response) throws IOException {
        return EMPTY;
    }

}
