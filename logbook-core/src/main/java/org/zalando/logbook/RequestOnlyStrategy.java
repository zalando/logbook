package org.zalando.logbook;

public final class RequestOnlyStrategy implements Strategy {

    @Override
    public HttpResponse process(HttpRequest request, final HttpResponse response) {
        return response.withoutBody();
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response, final Sink sink) {
        // do nothing
    }

}
