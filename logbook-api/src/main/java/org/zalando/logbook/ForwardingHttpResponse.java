package org.zalando.logbook;

public interface ForwardingHttpResponse extends ForwardingHttpMessage, ForwardingBaseHttpResponse, HttpResponse {

    @Override
    HttpResponse delegate();

}
