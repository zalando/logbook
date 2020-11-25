package org.zalando.logbook.jaxws;

import lombok.AllArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@AllArgsConstructor
final class HttpServletRequestAccessor implements Accessor {

    private final HttpServletRequest request;

    @Override
    public String getRemote() {
        return request.getRemoteAddr();
    }

    @Override
    public String getProtocolVersion() {
        return request.getProtocol();
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getScheme() {
        return request.getScheme();
    }

    @Override
    public String getHost() {
        return request.getServerName();
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.of(request.getServerPort());
    }

    @Override
    public String getPath() {
        return request.getRequestURI();
    }

    @Override
    public String getQuery() {
        return Optional.ofNullable(request.getQueryString()).orElse("");
    }

}
