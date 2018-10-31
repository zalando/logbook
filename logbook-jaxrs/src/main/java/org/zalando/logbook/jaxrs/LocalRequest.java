package org.zalando.logbook.jaxrs;

import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import javax.ws.rs.client.ClientRequestContext;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

final class LocalRequest implements HttpRequest {

    private final ClientRequestContext context;

    private TeeOutputStream stream;
    private byte[] body;

    public LocalRequest(final ClientRequestContext context) {
        this.context = context;
    }

    @Override
    public String getProtocolVersion() {
        // TODO find the real thing
        return "HTTP/1.1";
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public String getRemote() {
        return "localhost";
    }

    @Override
    public String getMethod() {
        return context.getMethod();
    }

    @Override
    public String getScheme() {
        return context.getUri().getScheme();
    }

    @Override
    public String getHost() {
        return context.getUri().getHost();
    }

    @Override
    public Optional<Integer> getPort() {
        return HttpMessages.getPort(context.getUri());
    }

    @Override
    public String getPath() {
        return context.getUri().getPath();
    }

    @Override
    public String getQuery() {
        return ofNullable(context.getUri().getQuery()).orElse("");
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return context.getStringHeaders();
    }

    @Nullable
    @Override
    public String getContentType() {
        return context.getStringHeaders().getFirst("Content-Type");
    }

    @Override
    public Charset getCharset() {
        return HttpMessages.getCharset(context.getMediaType());
    }

    @Override
    public HttpRequest withBody() {
        if (stream == null) {
            this.stream = new TeeOutputStream(context.getEntityStream());
            context.setEntityStream(stream);
        }

        return this;
    }

    @Override
    public HttpRequest withoutBody() {
        if (stream != null) {
            context.setEntityStream(stream.getOriginal());
            this.stream = null;
        }

        return this;
    }

    @Override
    public byte[] getBody() {
        if (body == null) {
            if (stream == null) {
                return new byte[0];
            }

            this.body = stream.toByteArray();
        }

        return body;
    }

}
