package org.zalando.logbook;

@FunctionalInterface
public interface ForwardingHttpRequest extends ForwardingHttpMessage, ForwardingBaseHttpRequest, HttpRequest {

    @Override
    HttpRequest delegate();

}
