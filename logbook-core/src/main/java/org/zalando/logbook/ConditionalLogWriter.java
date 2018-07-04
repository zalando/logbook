package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;
import java.util.function.Predicate;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public final class ConditionalLogWriter implements HttpLogWriter {

    private final Predicate<? super Correlation<String, String>> predicate;
    private final HttpLogWriter delegate;

    public ConditionalLogWriter(final Predicate<? super Correlation<String, String>> predicate, final HttpLogWriter delegate) {
        this.predicate = predicate;
        this.delegate = delegate;
    }

    @Override
    public void writeRequest(final Precorrelation<String> precorrelation) {
        // do nothing
    }

    @Override
    public void writeResponse(final Correlation<String, String> correlation) throws IOException {
        if (predicate.test(correlation)) {
            // delayed request logging until we have the response at hand
            delegate.writeRequest(correlation);
            delegate.writeResponse(correlation);
        }
    }

}
