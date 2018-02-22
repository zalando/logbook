package org.zalando.logbook.servlet;

import lombok.SneakyThrows;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.list;
import static java.util.stream.Collectors.joining;


final class RemoteRequest extends HttpServletRequestWrapper implements RawHttpRequest, HttpRequest {

    private static final byte[] EMPTY_BODY = new byte[0];

    private final FormRequestMode formRequestMode = FormRequestMode.fromProperties();

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

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(getCharacterEncoding()).map(Charset::forName).orElse(UTF_8);
    }

    @Override
    public HttpRequest withBody() throws IOException {
        if (isFormRequest()) {
            switch (formRequestMode) {
                case PARAMETER:
                    this.body = reconstructBodyFromParameters();
                    return this;
                case OFF:
                    this.body = EMPTY_BODY;
                    return this;
            }
        }

        body = ByteStreams.toByteArray(super.getInputStream());
        return this;
    }

    private boolean isFormRequest() {
        return getContentType() != null && getContentType().startsWith("application/x-www-form-urlencoded");
    }

    private byte[] reconstructBodyFromParameters() {
        return getParameterMap().entrySet().stream()
                .flatMap(entry -> Arrays.stream(entry.getValue())
                        .map(value -> encode(entry.getKey()) + "=" + encode(value)))
                .collect(joining("&"))
                .getBytes(UTF_8);
    }

    private static String encode(final String s) {
        return encode(s, "UTF-8");
    }

    @SneakyThrows
    static String encode(final String s, final String charset) {
        return URLEncoder.encode(s, charset);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return body == null ?
                super.getInputStream() :
                new ServletInputStreamAdapter(new ByteArrayInputStream(body));
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
