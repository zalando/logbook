package org.zalando.logbook.spring;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

@Slf4j
public class BufferingClientHttpResponseWrapper implements ClientHttpResponse {

    private final ClientHttpResponse delegate;
    private final InputStream body;

    public BufferingClientHttpResponseWrapper(ClientHttpResponse delegate) throws IOException {
        this.delegate = delegate;
        InputStream delegateBody;
        try {
            delegateBody = delegate.getBody();
        } catch (IOException e) {
            log.trace("Could not extract body of the response. Falling back to empty InputStream");
            // As java 8 version is used for compilation, InputStream.nullInputStream() is not yet available.
            delegateBody = new InputStream() {
                @Override
                public int read() {
                    return -1;
                }
            };
        }
        this.body = delegateBody.markSupported() ? delegateBody : new BufferedInputStream(delegateBody);
    }

    @Override
    public @NotNull HttpStatusCode getStatusCode() throws IOException {
        return delegate.getStatusCode();
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getRawStatusCode() throws IOException {
        return delegate.getStatusCode().value();
    }

    @Override
    public @NotNull String getStatusText() throws IOException {
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
    public @NotNull InputStream getBody() {
        return body;
    }

    @Override
    public @NotNull HttpHeaders getHeaders() {
        return delegate.getHeaders();
    }
}
