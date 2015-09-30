package org.zalando.logbook;

import com.google.common.collect.ForwardingObject;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.nio.charset.Charset;

abstract class ForwardingHttpMessage extends ForwardingObject implements HttpMessage {

    @Override
    protected abstract HttpMessage delegate();

    @Override
    public Multimap<String, String> getHeaders() {
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
    public byte[] getBody() throws IOException {
        return delegate().getBody();
    }

    @Override
    public String getBodyAsString() throws IOException {
        return delegate().getBodyAsString();
    }

}
