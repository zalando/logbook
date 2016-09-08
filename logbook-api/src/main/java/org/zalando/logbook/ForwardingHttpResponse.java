package org.zalando.logbook;

@FunctionalInterface
public interface ForwardingHttpResponse extends ForwardingHttpMessage, ForwardingBaseHttpResponse, HttpResponse {

    @Override
    HttpResponse delegate();

}
