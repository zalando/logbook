package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface ForwardingRawHttpResponse extends ForwardingBaseHttpResponse, RawHttpResponse {

    @Override
    RawHttpResponse delegate();

    @Override
    default HttpResponse withBody() throws IOException {
        return delegate().withBody();
    }

    @Override
    default void withoutBody() throws IOException {
        delegate().withoutBody();
    }

}
