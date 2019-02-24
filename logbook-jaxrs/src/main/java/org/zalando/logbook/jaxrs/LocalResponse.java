package org.zalando.logbook.jaxrs;

import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerResponseContext;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class LocalResponse implements HttpResponse {

    private final ContainerResponseContext context;

    private TeeOutputStream stream;
    private byte[] body;

    public LocalResponse(final ContainerResponseContext context) {
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
    public int getStatus() {
        return context.getStatus();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return context.getStringHeaders();
    }

    @Nullable
    @Override
    public String getContentType() {
        return Objects.toString(context.getMediaType(), null);
    }

    @Override
    public Charset getCharset() {
        return HttpMessages.getCharset(context.getMediaType());
    }

    @Override
    public HttpResponse withBody() {
        if (stream == null) {
            this.stream = new TeeOutputStream(context.getEntityStream());
            context.setEntityStream(stream);
        }

        return this;
    }

    @Override
    public HttpResponse withoutBody() {
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
