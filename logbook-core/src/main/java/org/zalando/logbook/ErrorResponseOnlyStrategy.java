package org.zalando.logbook;

import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
public final class ErrorResponseOnlyStrategy implements Strategy {

    @Override
    public HttpRequest process(final HttpRequest request) throws IOException {
        return request.withBody();
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request,
            final Sink sink) {
        // do nothing
    }

    @Override
    public HttpResponse process(HttpRequest request, final HttpResponse response) throws IOException {
        return response.withBody();
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response,
            final Sink sink) throws IOException {

        if (response.getStatus() >= 400) {
            sink.writeBoth(correlation, request, response);
        }
    }

}
