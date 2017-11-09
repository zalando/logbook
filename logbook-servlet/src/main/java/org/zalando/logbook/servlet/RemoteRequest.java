package org.zalando.logbook.servlet;

import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpRequest;

import javax.annotation.Nullable;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.Collections.list;
import static java.util.stream.Collectors.joining;


final class RemoteRequest extends HttpServletRequestWrapper implements RawHttpRequest, HttpRequest {

    /**
     * Null until we successfully intercepted it.
     */
    @Nullable
    private byte[] body;

    RemoteRequest(final HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getProtocolVersion() {
        return getProtocol();
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public String getRemote() {
        return getRemoteAddr();
    }

    @Override
    public String getHost() {
        return getServerName();
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.of(getServerPort());
    }

    @Override
    public String getPath() {
        return getRequestURI();
    }

    @Override
    public String getQuery() {
        return Optional.ofNullable(getQueryString()).orElse("");
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        final HeadersBuilder builder = new HeadersBuilder();
        final Enumeration<String> names = getHeaderNames();

        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            builder.put(name, list(getHeaders(name)));
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked") // warnings appear when using Servlet API 2.5
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameterMap = super.getParameterMap();
        if (getContentType() == null || !getContentType().toLowerCase().startsWith("application/x-www-form-urlencoded")) {
            return parameterMap;
        }
        String formEncoded = parameterMap.entrySet().stream()
                .flatMap(entry -> stream(entry.getValue()).map(value -> entry.getKey() + "=" + value))
                .collect(joining("&"));
        body = formEncoded.getBytes(getCharset());
        return parameterMap;
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(getCharacterEncoding()).map(Charset::forName).orElse(UTF_8);
    }

    @Override
    public HttpRequest withBody() throws IOException {
        if (body == null) {
            body = ByteStreams.toByteArray(super.getInputStream());
        }
        return this;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (body == null) {
            withBody();
        }
        return new ServletInputStreamAdapter(new ByteArrayInputStream(body));
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharset()));
    }

    @Override
    public byte[] getBody() {
        return body;
    }
}