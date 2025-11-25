package org.zalando.logbook.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class BufferingClientHttpResponseWrapper implements ClientHttpResponse {

    private final ClientHttpResponse delegate;
    private final InputStream body;

    public BufferingClientHttpResponseWrapper(ClientHttpResponse delegate) throws IOException {
        this.delegate = delegate;
        // Must call getHeaders() first as some URLConnection implementations throw exceptions first time
        // getInputStream() is requested if response code >= 400. E.g. sun.net.www.protocol.http.HttpURLConnection.
        delegate.getHeaders();
        InputStream delegateBody = delegate.getBody();
        this.body = delegateBody.markSupported() ? delegateBody : new BufferedInputStream(delegateBody);
    }

    @Override
    public @Nonnull HttpStatusCode getStatusCode() throws IOException {
        return delegate.getStatusCode();
    }

    @Override
    public @Nonnull String getStatusText() throws IOException {
        return delegate.getStatusText();
    }

    @Override
    public void close() {
        try {
            body.close();
        } catch (IOException e){
            throw new RuntimeException(e);
        } finally {
            delegate.close();
        }
    }

    @Override
    public @Nonnull InputStream getBody() {
        return body;
    }

    @Override
    public @Nonnull HttpHeaders getHeaders() {
        return delegate.getHeaders();
    }
}
