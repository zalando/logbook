package org.zalando.logbook.httpclient;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.zalando.logbook.Origin;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.util.EntityUtils.toByteArray;

final class LocalRequest implements org.zalando.logbook.HttpRequest {

    private final HttpRequest request;
    private final URI originalRequestUri;

    private byte[] body;

    LocalRequest(final HttpRequest request) {
        this.request = request;
        this.originalRequestUri = getOriginalRequestUri(request);
    }

    private static URI getOriginalRequestUri(final HttpRequest request) {
        if (request instanceof HttpRequestWrapper) {
            return extractRequestUri(HttpRequestWrapper.class.cast(request).getOriginal());
        } else if (request instanceof HttpUriRequest) {
            return HttpUriRequest.class.cast(request).getURI();
        } else {
            return extractRequestUri(request);
        }
    }

    private static URI extractRequestUri(final HttpRequest request) {
        return URI.create(request.getRequestLine().getUri());
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public String getProtocolVersion() {
        return request.getRequestLine().getProtocolVersion().toString();
    }

    @Override
    public String getRemote() {
        return "localhost";
    }

    @Override
    public String getMethod() {
        return request.getRequestLine().getMethod();
    }


    @Override
    public String getScheme() {
        return originalRequestUri.getScheme();
    }

    @Override
    public String getHost() {
        return originalRequestUri.getHost();
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.of(originalRequestUri.getPort()).filter(p -> p != -1);
    }

    @Override
    public String getPath() {
        return originalRequestUri.getPath();
    }

    @Override
    public String getQuery() {
        return Optional.ofNullable(originalRequestUri.getQuery()).orElse("");
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        final HeadersBuilder builder = new HeadersBuilder();

        for (final Header header : request.getAllHeaders()) {
            builder.put(header.getName(), header.getValue());
        }

        return builder.build();
    }

    @Override
    public String getContentType() {
        return Optional.of(request)
                .map(request -> request.getFirstHeader("Content-Type"))
                .map(Header::getValue)
                .orElse("");
    }

    @Override
    public Charset getCharset() {
        return Optional.of(request)
                .map(request -> request.getFirstHeader("Content-Type"))
                .map(Header::getValue)
                .map(ContentType::parse)
                .map(ContentType::getCharset)
                .orElse(UTF_8);
    }

    @Override
    public byte[] getBody() {
        return body == null ? new byte[0] : body;
    }

    @Override
    public org.zalando.logbook.HttpRequest withBody() throws IOException {
        if (body == null) {
            if (request instanceof HttpEntityEnclosingRequest) {
                final HttpEntityEnclosingRequest original = (HttpEntityEnclosingRequest) request;
                this.body = toByteArray(original.getEntity());
                original.setEntity(new ByteArrayEntity(body));
            } else {
                return withoutBody();
            }
        }

        return this;
    }

    @Override
    public org.zalando.logbook.HttpRequest withoutBody() {
        this.body = new byte[0];
        return this;
    }

}
