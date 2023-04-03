package org.zalando.logbook.api;

import java.io.IOException;

public interface Sink {

    default boolean isActive() {
        return true;
    }

    void write(Precorrelation precorrelation, HttpRequest request) throws IOException;

    void write(Correlation correlation, HttpRequest request, HttpResponse response) throws IOException;

    default void writeBoth(final Correlation correlation, final HttpRequest request, final HttpResponse response)
            throws IOException {
        write(correlation, request);
        write(correlation, request, response);
    }

}
