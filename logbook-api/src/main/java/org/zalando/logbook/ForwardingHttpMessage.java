package org.zalando.logbook;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public abstract class ForwardingHttpMessage implements HttpMessage {

    protected abstract HttpMessage delegate();

    @Override
    public String getProtocolVersion() {
        return delegate().getProtocolVersion();
    }

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
    public byte[] getBody() throws IOException {
        return delegate().getBody();
    }

    @Override
    public String getBodyAsString() throws IOException {
        return delegate().getBodyAsString();
    }

    @Override
    public String toString() {
        return delegate().toString();
    }
}
