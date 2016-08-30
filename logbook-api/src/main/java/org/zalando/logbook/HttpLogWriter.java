package org.zalando.logbook;

import java.io.IOException;

public interface HttpLogWriter {

    default boolean isActive(final RawHttpRequest request) throws IOException {
        return true;
    }

    void writeRequest(final Precorrelation<String> precorrelation) throws IOException;

    void writeResponse(final Correlation<String, String> correlation) throws IOException;

}
