package org.zalando.logbook;

import com.google.common.collect.Multimap;

abstract class ForwardingHttpRequest extends ForwardingHttpMessage implements HttpRequest {

    @Override
    protected abstract HttpRequest delegate();

    @Override
    public String getRemote() {
        return delegate().getRemote();
    }

    @Override
    public String getMethod() {
        return delegate().getMethod();
    }

    @Override
    public String getRequestURI() {
        return delegate().getRequestURI();
    }

    @Override
    public Multimap<String, String> getParameters() {
        return delegate().getParameters();
    }

}
