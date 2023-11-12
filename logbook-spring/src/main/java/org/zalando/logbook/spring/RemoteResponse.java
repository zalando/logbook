package org.zalando.logbook.spring;

import lombok.RequiredArgsConstructor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MimeType;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
final class RemoteResponse implements HttpResponse {

    private final ClientHttpResponse response;
    private boolean withBody;
    private byte[] body;

    @SuppressWarnings("deprecation")
    @Override
    public int getStatus() {
        try {
            return response.getStatusCode().value();
        } catch (NoSuchMethodError e) {
            try {
                // support spring-boot 2.x as fallback
                return response.getRawStatusCode();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getProtocolVersion() {
        // TODO find the real thing
        return "HTTP/1.1";
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.of(response.getHeaders());
    }

    @Nullable
    @Override
    public String getContentType() {
        return Optional.ofNullable(response.getHeaders().getContentType()).map(MimeType::toString).orElse(null);
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(response.getHeaders().getContentType()).map(MimeType::getCharset).orElse(UTF_8);
    }

    @Override
    public HttpResponse withBody() {
        this.withBody = true;
        return this;
    }

    @Override
    public RemoteResponse withoutBody() {
        this.withBody = false;
        return this;
    }

    @Override
    public byte[] getBody() {
        if (this.withBody) {
            if (this.body == null) {
                this.body = getBodyChecked();
            }
            return this.body;
        }
        return new byte[0];
    }

    private byte[] getBodyChecked() {
        try {
            InputStream responseBodyStream = response.getBody();
            responseBodyStream.mark(Integer.MAX_VALUE);
            byte[] data = ByteStreams.toByteArray(responseBodyStream);
            responseBodyStream.reset();
            return data;
        } catch (IOException e) {
            return new byte[0];
        }
    }
}
