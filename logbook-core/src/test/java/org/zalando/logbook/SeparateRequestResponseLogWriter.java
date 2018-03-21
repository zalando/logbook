package org.zalando.logbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

// proof of concept
final class SeparateRequestResponseLogWriter implements HttpLogWriter {

    private final Logger incoming = LoggerFactory.getLogger("http.incoming");
    private final Logger outgoing = LoggerFactory.getLogger("http.outgoing");

    // TODO overwrite isActive?

    @Override
    public void writeRequest(final Precorrelation<String> precorrelation) throws IOException {
        choose(precorrelation.getOriginalRequest()).trace(precorrelation.getRequest());
    }

    @Override
    public void writeResponse(final Correlation<String, String> correlation) throws IOException {
        choose(correlation.getOriginalResponse()).trace(correlation.getResponse());
    }

    private Logger choose(final HttpRequest request) {
        return request.getOrigin() == Origin.REMOTE ? incoming : outgoing;
    }

    private Logger choose(final HttpResponse response) {
        return response.getOrigin() == Origin.LOCAL ? incoming : outgoing;
    }

}
