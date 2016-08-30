package org.zalando.logbook;

import java.util.Optional;

public abstract class ForwardingHttpRequest extends ForwardingHttpMessage implements HttpRequest {

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
    public String getRequestUri() {
        return delegate().getRequestUri();
    }

    @Override
    public String getHost() {
        return delegate().getHost();
    }

    @Override
    public String getScheme() {
        return delegate().getScheme();
    }

    @Override
    public Optional<Integer> getPort() {
        return delegate().getPort();
    }

    @Override
    public String getPath() {
        return delegate().getPath();
    }

    @Override
    public String getQuery() {
        return delegate().getQuery();
    }

}
