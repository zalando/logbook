package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface HttpLogFormatter {

    String format(Precorrelation precorrelation, HttpRequest request) throws IOException;
    String format(Correlation correlation, HttpResponse response) throws IOException;

}
