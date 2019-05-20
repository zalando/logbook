package org.zalando.logbook;

import java.io.IOException;

import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;

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
