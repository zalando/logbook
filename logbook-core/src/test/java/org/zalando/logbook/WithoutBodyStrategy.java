package org.zalando.logbook;

/**
 * Proof of concept
 */
final class WithoutBodyStrategy implements Strategy {

    @Override
    public HttpRequest process(final HttpRequest request) {
        return request.withoutBody();
    }

    @Override
    public HttpResponse process(final HttpRequest request, final HttpResponse response) {
        return response.withoutBody();
    }

}
