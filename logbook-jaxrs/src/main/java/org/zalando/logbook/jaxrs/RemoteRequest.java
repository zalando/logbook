package org.zalando.logbook.jaxrs;

import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

final class RemoteRequest implements HttpRequest {

    private final ContainerRequestContext context;
    private byte[] body;

    public RemoteRequest(final ContainerRequestContext context) {
        this.context = context;
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
    public String getRemote() {
        // TODO find remote ip
        return context.getUriInfo().getRequestUri().getAuthority();
    }

    @Override
    public String getMethod() {
        return context.getMethod();
    }

    @Override
    public String getScheme() {
        return context.getUriInfo().getRequestUri().getScheme();
    }

    @Override
    public String getHost() {
        return context.getUriInfo().getRequestUri().getHost();
    }

    @Override
    public Optional<Integer> getPort() {
        return HttpMessages.getPort(context.getUriInfo().getRequestUri());
    }

    @Override
    public String getPath() {
        return context.getUriInfo().getRequestUri().getPath();
    }

    @Override
    public String getQuery() {
        return ofNullable(context.getUriInfo().getRequestUri().getQuery()).orElse("");
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return context.getHeaders();
    }

    @Nullable
    @Override
    public String getContentType() {
        return context.getHeaders().getFirst("Content-Type");
    }

    @Override
    public Charset getCharset() {
        return HttpMessages.getCharset(context.getMediaType());
    }

    @Override
    public HttpRequest withBody() throws IOException {
        if (body == null) {
            this.body = ByteStreams.toByteArray(context.getEntityStream());
            context.setEntityStream(new ByteArrayInputStream(body));
        }

        return this;
    }

    @Override
    public HttpRequest withoutBody() {
        this.body = new byte[0];
        return this;
    }

    @Override
    public byte[] getBody() {
        return body == null ? new byte[0] : body;
    }

}
