package org.zalando.logbook.okhttp2;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpResponse;

import static com.squareup.okhttp.ResponseBody.create;
import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

final class RemoteResponse implements RawHttpResponse, HttpResponse {

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
        final ResponseBody body = requireNonNull(response.body(), "Body is never null for normal responses");

        if (canSkipBody(response)) {
            this.body = new byte[0];
        } else {
            final byte[] bytes = body.bytes();

            this.response = response.newBuilder()
                    .body(create(body.contentType(), bytes))
                    .build();

            this.body = bytes;
        }

        return this;
    }

    static boolean canSkipBody(Response response) {
        if("HEAD".equals(response.request().method())) {
            return true;
        }
        return response.code() == HTTP_NO_CONTENT || response.code() == HTTP_NOT_MODIFIED;
    }

    public Response toResponse() {
        return response;
    }

    @Override
    public byte[] getBody() {
        return body;
    }

}
