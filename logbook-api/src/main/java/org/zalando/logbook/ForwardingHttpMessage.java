package org.zalando.logbook;

import java.io.IOException;

public interface ForwardingHttpMessage extends ForwardingBaseHttpMessage, HttpMessage {

    @Override
    HttpMessage delegate();

    @Override
    default byte[] getBody() throws IOException {
        return delegate().getBody();
    }

    @Override
    default String getBodyAsString() throws IOException {
        return delegate().getBodyAsString();
    }

}
