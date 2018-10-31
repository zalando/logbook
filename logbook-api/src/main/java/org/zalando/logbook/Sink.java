package org.zalando.logbook;

import java.io.IOException;

public interface Sink {

    default boolean isActive() {
        return true;
    }

    void write(Precorrelation precorrelation, HttpRequest request) throws IOException;
    void write(Correlation correlation, HttpRequest request, HttpResponse response) throws IOException;

    // TODO needed?!
    void writeBoth(Correlation correlation, HttpRequest request, HttpResponse response) throws IOException;

}
