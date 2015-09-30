package org.zalando.logbook;

abstract class ForwardingHttpResponse extends ForwardingHttpMessage implements HttpResponse {

    @Override
    protected abstract HttpResponse delegate();

    @Override
    public int getStatus() {
        return delegate().getStatus();
    }

}
