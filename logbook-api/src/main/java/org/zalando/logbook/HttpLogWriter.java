package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface HttpLogWriter {

    default boolean isActive(final RawHttpRequest request) throws IOException {
        return true;
    }

    void writeRequest(Precorrelation<String> precorrelation) throws IOException;

    void writeResponse(Correlation<String, String> correlation) throws IOException;

}
