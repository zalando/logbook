package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface Strategy {

    default HttpRequest process(final HttpRequest request) throws IOException {
        return request.withBody();
    }

    default void write(final Precorrelation precorrelation, final HttpRequest request,
            final Sink sink) throws IOException {
        sink.write(precorrelation, request);
    }

    default HttpResponse process(final HttpRequest request, final HttpResponse response) throws IOException {
        return response.withBody();
    }

    default void write(final Correlation correlation, final HttpRequest request, final HttpResponse response,
            final Sink sink) throws IOException {
        sink.write(correlation, request, response);
    }

}
