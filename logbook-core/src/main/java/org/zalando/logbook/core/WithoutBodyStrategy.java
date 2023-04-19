package org.zalando.logbook.core;

import org.apiguardian.api.API;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.HttpResponse;
import org.zalando.logbook.api.Strategy;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public final class WithoutBodyStrategy implements Strategy {

    @Override
    public HttpRequest process(final HttpRequest request) {
        return request.withoutBody();
    }

    @Override
    public HttpResponse process(final HttpRequest request, final HttpResponse response) {
        return response.withoutBody();
    }

}
