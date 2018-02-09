package org.zalando.logbook.feignlogger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpRequest;

import feign.Request;

final class LocalRequest implements RawHttpRequest, org.zalando.logbook.HttpRequest {

    private final Request request;
    private final Localhost localhost;
    private final URI originalRequestUri;

    private byte[] body;

    LocalRequest(final Request request, final Localhost localhost) {
        this.request = request;
        this.localhost = localhost;
        this.originalRequestUri = getOriginalRequestUri(request);
    }

    private static URI getOriginalRequestUri(final Request request) {
        try {
            return new URI(request.url());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public String getProtocolVersion() {
        return "1.1";
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
        return request.method();
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

        for (Entry<String, Collection<String>> entry : request.headers().entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }

        return builder.build();
    }

    @Override
    public String getContentType() {
        return Optional.ofNullable(request.headers().get("Content-Type"))
            .map(m -> m.iterator())
            .filter(m -> m.hasNext())
            .map(Iterator::next)
            .orElse("");
    }

    @Override
    public Charset getCharset() {
        return request.charset();
    }    

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public org.zalando.logbook.HttpRequest withBody() throws IOException {
        this.body = request.body();
        
        return this;
    }

}
