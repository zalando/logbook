package org.zalando.logbook;

import java.io.IOException;

@FunctionalInterface
public interface ForwardingRawHttpRequest extends ForwardingBaseHttpRequest, RawHttpRequest {

    @Override
    RawHttpRequest delegate();

    @Override
    default HttpRequest withBody() throws IOException {
        return delegate().withBody();
    }

}
