package org.zalando.logbook.httpclient;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpRequest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.util.EntityUtils.toByteArray;


final class LocalRequest implements RawHttpRequest, org.zalando.logbook.HttpRequest {

    private final HttpRequest request;
    private final Localhost localhost;
    private final URI originalRequestUri;

    private byte[] body;

    LocalRequest(final HttpRequest request, final Localhost localhost) {
        this.request = request;
        this.localhost = localhost;
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
        try {
            return localhost.getAddress();
        } catch (@SuppressWarnings("unused") final UnknownHostException e) {
            return InetAddress.getLoopbackAddress().getHostAddress();
        }
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
        return body;
    }

    @Override
    public org.zalando.logbook.HttpRequest withBody() throws IOException {
        if (request instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) this.request;
            this.body = toByteArray(request.getEntity());
            request.setEntity(new ByteArrayEntity(body));
        } else {
            this.body = new byte[0];
        }

        return this;
    }

}
