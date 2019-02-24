package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface ForwardingHttpResponse extends ForwardingHttpMessage, HttpResponse {

    @Override
    HttpResponse delegate();

    @Override
    default int getStatus() {
        return delegate().getStatus();
    }

    @Override
    default HttpResponse withBody() throws IOException {
        return delegate().withBody();
    }

    @Override
    default HttpResponse withoutBody() {
        return delegate().withoutBody();
    }

}
