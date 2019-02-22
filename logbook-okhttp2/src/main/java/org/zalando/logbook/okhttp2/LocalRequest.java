package org.zalando.logbook.okhttp2;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import okio.Buffer;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.squareup.okhttp.HttpUrl.defaultPort;
import static com.squareup.okhttp.RequestBody.create;
import static java.nio.charset.StandardCharsets.UTF_8;

final class LocalRequest implements HttpRequest {

    private Request request;
    private byte[] body;

    LocalRequest(final Request request) {
        this.request = request;
    }

    @Override
    public String getRemote() {
        return "localhost";
    }

    @Override
    public String getMethod() {
        return request.method();
    }

    @Override
    public String getScheme() {
        return request.httpUrl().scheme();
    }

    @Override
    public String getHost() {
        return request.httpUrl().host();
    }

    @Override
    public Optional<Integer> getPort() {
        final int port = request.httpUrl().port();
        final int defaultPort = defaultPort(request.httpUrl().scheme());
        return port == defaultPort ? Optional.empty() : Optional.of(port);
    }

    @Override
    public String getPath() {
        return request.url().getPath();
    }

    @Override
    public String getQuery() {
        return Optional.ofNullable(request.httpUrl().query()).orElse("");
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
    public Map<String, List<String>> getHeaders() {
        return request.headers().toMultimap();
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
        return Optional.ofNullable(request.body())
                .map(RequestBody::contentType);
    }

    @Override
    public HttpRequest withBody() throws IOException {
        if (body == null) {
            @Nullable final RequestBody entity = request.body();

            if (entity == null) {
                return withoutBody();
            } else {
                this.body = bytes(entity);

                this.request = request.newBuilder()
                        .method(request.method(), create(entity.contentType(), body))
                        .build();
            }
        }

        return this;
    }

    @Override
    public HttpRequest withoutBody() {
        this.body = new byte[0];
        return this;
    }

    private static byte[] bytes(final RequestBody body) throws IOException {
        final Buffer buffer = new Buffer();
        body.writeTo(buffer);
        return buffer.readByteArray();
    }

    Request toRequest() {
        return request;
    }

    @Override
    public byte[] getBody() {
        return body == null ? new byte[0] : body;
    }

}
