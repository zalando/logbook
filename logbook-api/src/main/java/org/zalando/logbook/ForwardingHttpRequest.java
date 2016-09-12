package org.zalando.logbook;

public interface ForwardingHttpRequest extends ForwardingHttpMessage, ForwardingBaseHttpRequest, HttpRequest {

    @Override
    HttpRequest delegate();

}
