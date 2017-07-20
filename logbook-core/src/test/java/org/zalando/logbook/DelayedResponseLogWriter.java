package org.zalando.logbook;

import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;

// proof of concept
final class DelayedResponseLogWriter implements HttpLogWriter {

    private final HttpLogWriter delegate;

    DelayedResponseLogWriter(final HttpLogWriter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void writeRequest(final Precorrelation<String> precorrelation) throws IOException {
        // do nothing
    }

    @Override
    public void writeResponse(final Correlation<String, String> correlation) throws IOException {
        final HttpResponse response = correlation.getOriginalResponse();

        if (response.getStatus() >= 400) {
            // delayed request logging until we have the response at hand
            delegate.writeRequest(new SimplePrecorrelation<>(correlation.getId(), correlation.getRequest()));
            delegate.writeResponse(correlation);
        }
    }

}
