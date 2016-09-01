package org.zalando.logbook;

public abstract class ForwardingHttpResponse extends ForwardingHttpMessage implements HttpResponse {

    @Override
    protected abstract HttpResponse delegate();

    @Override
    public int getStatus() {
        return delegate().getStatus();
    }

}
