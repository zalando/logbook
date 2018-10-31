package org.zalando.logbook.okhttp;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static okhttp3.ResponseBody.create;

final class RemoteResponse implements HttpResponse {

    private Response response;
    private byte[] body;

    public RemoteResponse(final Response response) {
        this.response = response;
    }

    @Override
    public int getStatus() {
        return response.code();
    }

    @Override
    public String getProtocolVersion() {
        // see https://tools.ietf.org/html/rfc7230#section-2.6
        return response.protocol().toString().toUpperCase(Locale.ROOT);
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return response.headers().toMultimap();
    }

    @Override
    public String getContentType() {
        return contentType().map(MediaType::toString).orElse("");
    }

    @Override
    public Charset getCharset() {
        return contentType().map(MediaType::charset).orElse(UTF_8);
    }

    private Optional<MediaType> contentType() {
        return Optional.ofNullable(response.body())
                .map(ResponseBody::contentType);
    }

    @Override
    public HttpResponse withBody() throws IOException {
        if (body == null) {
            final ResponseBody entity = requireNonNull(response.body(), "Body is never null for normal responses");

            if (entity.contentLength() == 0L) {
                return withoutBody();
            } else {
                this.body = entity.bytes();

                this.response = response.newBuilder()
                        .body(create(entity.contentType(), body))
                        .build();

            }
        }

        return this;
    }

    @Override
    public RemoteResponse withoutBody() {
        this.body = new byte[0];
        return this;
    }

    public Response toResponse() {
        return response;
    }

    @Override
    public byte[] getBody() {
        return body == null ? new byte[0] : body;
    }

}
