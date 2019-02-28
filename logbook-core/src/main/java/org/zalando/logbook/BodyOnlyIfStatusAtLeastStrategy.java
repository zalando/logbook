package org.zalando.logbook;

import lombok.AllArgsConstructor;
import org.apiguardian.api.API;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@AllArgsConstructor
public final class BodyOnlyIfStatusAtLeastStrategy implements Strategy {

    private final int status;

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request, final Sink sink) {
        // defer decision until response is available
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response,
            final Sink sink) throws IOException {

        if (response.getStatus() >= status) {
            sink.writeBoth(correlation, request, response);
        } else {
            sink.writeBoth(correlation, request.withoutBody(), response.withoutBody());
        }
    }

}
