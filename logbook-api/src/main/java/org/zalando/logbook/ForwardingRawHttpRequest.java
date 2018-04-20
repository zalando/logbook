package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface ForwardingRawHttpRequest extends ForwardingBaseHttpRequest, RawHttpRequest {

    @Override
    RawHttpRequest delegate();

    @Override
    default HttpRequest withBody() throws IOException {
        return delegate().withBody();
    }

    @Override
    default void withoutBody() throws IOException {
        delegate().withoutBody();
    }

}
