package org.zalando.logbook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface HttpLogWriter {

    default boolean isActive(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        return true;
    }

    void writeRequest(final String request) throws IOException;

    void writeResponse(final String response) throws IOException;

}
