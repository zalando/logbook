package org.zalando.logbook.jaxws;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
final class HttpExchangeAccessor implements Accessor {

    private final HttpExchange exchange;

    @Override
    public String getRemote() {
        return exchange.getRemoteAddress().getHostString();
    }

    @Override
    public String getProtocolVersion() {
        return exchange.getProtocol();
    }

    @Override
    public String getMethod() {
        return exchange.getRequestMethod();
    }

    @Override
    public String getScheme() {
        return exchange instanceof HttpsExchange ? "https" : "http";
    }

    @Override
    public String getHost() {
        return exchange.getLocalAddress().getHostString();
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.of(exchange.getLocalAddress().getPort());
    }

    @Override
    public String getPath() {
        return exchange.getRequestURI().toString();
    }

    @Override
    public String getQuery() {
        return Optional.ofNullable(exchange.getRequestURI().getQuery()).orElse("");
    }

}
