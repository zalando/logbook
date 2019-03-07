package org.zalando.logbook.lle;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.io.IOException;

import org.apiguardian.api.API;
import org.slf4j.Marker;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.Precorrelation;

@API(status = EXPERIMENTAL)
public interface LogbackLogstashLogWriter {

    default boolean isActive() {
        return true;
    }

    void write(Precorrelation precorrelation, Marker request, String message) throws IOException;
    void write(Correlation correlation, Marker response, String message) throws IOException;

}
