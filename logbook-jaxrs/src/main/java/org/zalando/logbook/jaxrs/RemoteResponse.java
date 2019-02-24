package org.zalando.logbook.jaxrs;

import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import javax.ws.rs.client.ClientResponseContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class RemoteResponse implements HttpResponse {

    private final ClientResponseContext context;
    private byte[] body;

    public RemoteResponse(final ClientResponseContext context) {
        this.context = context;
    }

    @Override
    public int getStatus() {
        return context.getStatus();
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
    public Map<String, List<String>> getHeaders() {
        return context.getHeaders();
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
    public HttpResponse withBody() throws IOException {
        if (body == null) {
            this.body = ByteStreams.toByteArray(context.getEntityStream());
            context.setEntityStream(new ByteArrayInputStream(body));
        }
        return this;
    }

    @Override
    public HttpResponse withoutBody() {
        this.body = new byte[0];
        return this;
    }

    @Override
    public byte[] getBody() {
        return body == null ? new byte[0] : body;
    }

}
