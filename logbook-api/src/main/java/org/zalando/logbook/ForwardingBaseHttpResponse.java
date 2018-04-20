package org.zalando.logbook;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface ForwardingBaseHttpResponse extends ForwardingBaseHttpMessage, BaseHttpResponse {

    @Override
    BaseHttpResponse delegate();

    @Override
    default int getStatus() {
        return delegate().getStatus();
    }

}
