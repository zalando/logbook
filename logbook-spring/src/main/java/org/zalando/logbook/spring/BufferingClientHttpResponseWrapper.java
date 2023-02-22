package org.zalando.logbook.spring;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

public class BufferingClientHttpResponseWrapper implements ClientHttpResponse {

    private final ClientHttpResponse delegate;
    private final InputStream body;

    public BufferingClientHttpResponseWrapper(ClientHttpResponse delegate) throws IOException {
        this.delegate = delegate;
        final InputStream delegateBody = delegate.getBody();
        this.body = delegateBody.markSupported() ? delegateBody : new BufferedInputStream(delegateBody);
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return delegate.getStatusCode();
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return delegate.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return delegate.getStatusText();
    }

    @Override
    public void close() {
        try {
            body.close();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
        delegate.close();
    }

    @Override
    public InputStream getBody() {
        return body;
    }

    @Override
    public HttpHeaders getHeaders() {
        return delegate.getHeaders();
    }
}
