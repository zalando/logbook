package org.zalando.logbook;

import org.apiguardian.api.API;
import org.zalando.logbook.attributes.RequestAttributesExtractor;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface ForwardingStrategy extends Strategy {

    Strategy delegate();

    @Override
    default HttpRequest process(final HttpRequest request) throws IOException {
        return delegate().process(request);
    }

    @Override
    default void write(final Precorrelation precorrelation, final HttpRequest request,
                       final Sink sink) throws IOException {
        delegate().write(precorrelation, request, sink);
    }

    @Override
    default HttpResponse process(final HttpRequest request, final HttpResponse response) throws IOException {
        return delegate().process(request, response);
    }

    @Override
    default void write(final Correlation correlation, final HttpRequest request, final HttpResponse response,
                       final Sink sink) throws IOException {
        delegate().write(correlation, request, response, sink);
    }

    @Override
    default RequestAttributesExtractor getRequestAttributesExtractor() {
        return delegate().getRequestAttributesExtractor();
    }

}
