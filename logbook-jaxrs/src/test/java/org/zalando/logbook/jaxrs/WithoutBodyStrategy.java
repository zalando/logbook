package org.zalando.logbook.jaxrs;

import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Strategy;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;

final class WithoutBodyStrategy implements Strategy {

    @Override
    public HttpRequest process(final HttpRequest request) throws IOException {
        request.getBodyAsString();
        request.withoutBody();
        assertThat(request.getBodyAsString(), is(emptyString()));
        request.withBody();
        return request.withoutBody();
    }

    @Override
    public HttpResponse process(final HttpRequest request, final HttpResponse response) throws IOException {
        response.getBodyAsString();
        response.withoutBody();
        assertThat(response.getBodyAsString(), is(emptyString()));
        response.withBody();
        return response.withoutBody();
    }

}
