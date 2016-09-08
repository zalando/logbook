package org.zalando.logbook;

import java.io.IOException;

@FunctionalInterface
public interface ForwardingRawHttpResponse extends ForwardingBaseHttpResponse, RawHttpResponse {

    @Override
    RawHttpResponse delegate();

    @Override
    default HttpResponse withBody() throws IOException {
        return delegate().withBody();
    }

}
