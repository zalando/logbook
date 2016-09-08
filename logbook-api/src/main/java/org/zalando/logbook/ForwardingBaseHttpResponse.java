package org.zalando.logbook;

@FunctionalInterface
public interface ForwardingBaseHttpResponse extends ForwardingBaseHttpMessage, BaseHttpResponse {

    @Override
    BaseHttpResponse delegate();

    @Override
    default int getStatus() {
        return delegate().getStatus();
    }

}
