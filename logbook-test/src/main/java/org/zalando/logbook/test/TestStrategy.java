package org.zalando.logbook.test;

import org.zalando.logbook.api.Correlation;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.HttpResponse;
import org.zalando.logbook.api.Precorrelation;
import org.zalando.logbook.api.Sink;
import org.zalando.logbook.api.Strategy;

import java.io.IOException;

public final class TestStrategy implements Strategy {

    @Override
    public HttpRequest process(final HttpRequest request) throws IOException {
        return request
                .withBody() // offering
                .withoutBody() // un-buffering
                .withoutBody() // un-buffering (self transition)
                .withBody(); // offering
    }

    @Override
    public void write(
            final Precorrelation precorrelation,
            final HttpRequest request,
            final Sink sink) throws IOException {

        request.getBody();
        request.withoutBody() // ignoring
                .withBody() // buffering
                .withBody(); // buffering (self transition)

        if (request.getHeaders().containsKey("ignore")) {
            request.withoutBody();
        }

        sink.write(precorrelation, request);
    }

    @Override
    public HttpResponse process(
            final HttpRequest request,
            final HttpResponse response) throws IOException {

        return response
                .withBody() // offering
                .withoutBody() // un-buffering
                .withoutBody() // un-buffering (self transition)
                .withBody(); // offering
    }

    @Override
    public void write(
            final Correlation correlation,
            final HttpRequest request,
            final HttpResponse response,
            final Sink sink) throws IOException {

        response.getBody();
        response.withoutBody() // ignoring
                .withBody() // buffering
                .withBody(); // buffering (self transition)

        if (request.getHeaders().containsKey("ignore")) {
            response.withoutBody();
        }

        sink.write(correlation, request, response);
    }

}
