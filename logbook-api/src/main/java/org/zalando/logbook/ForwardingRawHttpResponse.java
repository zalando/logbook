package org.zalando.logbook;

import java.io.IOException;

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
