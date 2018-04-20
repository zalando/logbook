package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
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
