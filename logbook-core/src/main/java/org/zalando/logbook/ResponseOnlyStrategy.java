package org.zalando.logbook;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class ResponseOnlyStrategy implements Strategy {

    @Override
    public HttpRequest process(final HttpRequest request) {
        return request.withoutBody();
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request, final Sink sink) {
        // do nothing
    }

}
