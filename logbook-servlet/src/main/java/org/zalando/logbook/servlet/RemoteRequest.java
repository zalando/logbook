package org.zalando.logbook.servlet;

import lombok.SneakyThrows;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

import javax.activation.MimeType;
import javax.annotation.Nullable;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

final class RemoteRequest extends HttpServletRequestWrapper implements HttpRequest {

    private final FormRequestMode formRequestMode = FormRequestMode.fromProperties();

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
        if (body == null) {
            if (isFormRequest()) {
                switch (formRequestMode) {
                    case PARAMETER:
                        this.body = reconstructBodyFromParameters();
                        return this;
                    case OFF:
                        this.body = new byte[0];
                        return this;
                    default:
                        break;
                }
            }

            this.body = ByteStreams.toByteArray(super.getInputStream());
        }

        return this;
    }

    @Override
    public HttpRequest withoutBody() {
        this.body = new byte[0];
        return this;
    }

    private boolean isFormRequest() {
        return Optional.ofNullable(getContentType())
                .flatMap(MimeTypes::parse)
                .filter(this::isFormRequest)
                .isPresent();
    }

    private boolean isFormRequest(final MimeType contentType) {
        return "application".equals(contentType.getPrimaryType()) &&
                "x-www-form-urlencoded".equals(contentType.getSubType());
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
        return body == null ? new byte[0] : body;
    }
}
