package org.zalando.logbook.jaxrs;

import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.Sink;
import org.zalando.logbook.Strategy;

import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

final class WithBodyStrategy implements Strategy {

    @Override
    public HttpRequest process(final HttpRequest request) throws IOException {
        return request.withBody().withBody();
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request,
            final Sink sink) throws IOException {

        request.getBodyAsString();
        sink.write(precorrelation, request);
    }

    @Override
    public HttpResponse process(final HttpRequest request, final HttpResponse response) throws IOException {
        return response.withBody().withBody();
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response,
            final Sink sink) throws IOException {

        response.getBodyAsString();
        sink.write(correlation, request, response);
    }

}
