package org.zalando.logbook.jaxws;

import lombok.AllArgsConstructor;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Optional;

@AllArgsConstructor
final class EndpointAddressAccessor implements Accessor {

    private final URI uri;

    @Override
    public String getRemote() {
        return "localhost";
    }

    @Override
    public String getProtocolVersion() {
        // fake, but we don't know any better...
        return "HTTP/1.1";
    }

    @Override
    public String getMethod() {
        // fake, but SOAP kind of demands it, so...
        return "POST";
    }

    @Override
    public String getScheme() {
        return uri.getScheme();
    }

    @Override
    public String getHost() {
        return uri.getHost();
    }

    @Override
    public Optional<Integer> getPort() {
        @Nullable final Integer port = uri.getPort() == -1 ? null : uri.getPort();
        return Optional.ofNullable(port);
    }

    @Override
    public String getPath() {
        return uri.getPath();
    }

    @Override
    public String getQuery() {
        return Optional.ofNullable(uri.getQuery()).orElse("");
    }

}
