package org.zalando.logbook;

public interface ForwardingBaseHttpResponse extends ForwardingBaseHttpMessage, BaseHttpResponse {

    @Override
    BaseHttpResponse delegate();

    @Override
    default int getStatus() {
        return delegate().getStatus();
    }

}
