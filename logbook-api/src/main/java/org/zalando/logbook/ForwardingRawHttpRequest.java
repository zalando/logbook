package org.zalando.logbook;

import java.io.IOException;

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
