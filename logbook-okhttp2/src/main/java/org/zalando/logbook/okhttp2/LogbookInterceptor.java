package org.zalando.logbook.okhttp2;

import java.io.IOException;
import java.util.Optional;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Response;

import org.apiguardian.api.API;
import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.zalando.fauxpas.FauxPas.throwingConsumer;

@API(status = EXPERIMENTAL)
public final class LogbookInterceptor implements Interceptor {

    private final Logbook logbook;

    public LogbookInterceptor(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        final LocalRequest request = new LocalRequest(chain.request());
        final Optional<Correlator> correlator = logbook.write(request);

        final RemoteResponse response = new RemoteResponse(chain.proceed(request.toRequest()));

        correlator.ifPresent(throwingConsumer(c -> c.write(response)));

        return response.toResponse();
    }

}
