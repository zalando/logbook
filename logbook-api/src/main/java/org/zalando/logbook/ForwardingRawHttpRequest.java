package org.zalando.logbook;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class ForwardingRawHttpRequest implements RawHttpRequest {

    protected abstract RawHttpRequest delegate();

    @Override
    public Map<String, List<String>> getHeaders() {
        return delegate().getHeaders();
    }

    @Override
    public String getContentType() {
        return delegate().getContentType();
    }

    @Override
    public Charset getCharset() {
        return delegate().getCharset();
    }

    @Override
    public Origin getOrigin() {
        return delegate().getOrigin();
    }

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
    public String getScheme() {
        return delegate().getScheme();
    }

    @Override
    public String getHost() {
        return delegate().getHost();
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

    @Override
    public String getProtocolVersion() {
        return delegate().getProtocolVersion();
    }

    @Override
    public HttpRequest withBody() throws IOException {
        return delegate().withBody();
    }

    @Override
    public String toString() {
        return delegate().toString();
    }
}
