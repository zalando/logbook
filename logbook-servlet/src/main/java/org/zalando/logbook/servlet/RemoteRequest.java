package org.zalando.logbook.servlet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;
import org.zalando.logbook.common.MediaTypeQuery;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.list;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PROTECTED;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;
import static org.zalando.logbook.servlet.ByteStreams.toByteArray;

final class RemoteRequest extends HttpServletRequestWrapper implements HttpRequest {

    private final AtomicReference<State> state;

    private interface State {

        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer(final ServletRequest request) throws IOException {
            return this;
        }

        default ServletInputStream getInputStream(final ServletRequest request) throws IOException {
            return request.getInputStream();
        }

        default byte[] getBody() {
            return new byte[0];
        }

    }

    @AllArgsConstructor
    private static final class Unbuffered implements State {

        private final FormRequestMode formRequestMode;

        @Override
        public State with() {
            return new Offering(formRequestMode);
        }

    }

    @AllArgsConstructor
    private static final class Offering implements State {

        private static final Predicate<String> FORM_REQUEST =
                MediaTypeQuery.compile("application/x-www-form-urlencoded");

        private final FormRequestMode formRequestMode;

        @Override
        public State without() {
            return new Unbuffered(formRequestMode);
        }

        @Override
        public State buffer(final ServletRequest request) throws IOException {
            if (isFormRequest(request)) {
                switch (formRequestMode) {
                    case PARAMETER:
                        return new Buffering(reconstructFormBody(request));
                    case OFF:
                        return new Passing();
                    default:
                        break;
                }
            }

            return new Buffering(toByteArray(request.getInputStream()));
        }

        private boolean isFormRequest(final ServletRequest request) {
            return Optional.ofNullable(request.getContentType())
                    .filter(FORM_REQUEST)
                    .isPresent();
        }

        private byte[] reconstructFormBody(final ServletRequest request) {
            return request.getParameterMap().entrySet().stream()
                    .flatMap(entry -> Arrays.stream(entry.getValue())
                            .map(value -> encode(entry.getKey()) + "=" + encode(value)))
                    .collect(joining("&"))
                    .getBytes(UTF_8);
        }

        private static String encode(final String s) {
            return RemoteRequest.encode(s, "UTF-8");
        }

    }

    @AllArgsConstructor
    private static abstract class Streaming implements State {

        @Getter(PROTECTED)
        private final ByteArrayInputStream stream;

        @Override
        public ServletInputStream getInputStream(final ServletRequest request) {
            return new ServletInputStreamAdapter(stream);
        }

    }

    private static final class Buffering extends Streaming {

        private final byte[] body;

        Buffering(final byte[] body) {
            this(body, new ByteArrayInputStream(body));
        }

        Buffering(final byte[] body, final ByteArrayInputStream stream) {
            super(stream);
            this.body = body;
        }

        @Override
        public State without() {
            return new Ignoring(body);
        }

        @Override
        public byte[] getBody() {
            return body;
        }

    }

    private static final class Ignoring extends Streaming {

        private byte[] body;

        Ignoring(final byte[] body) {
            super(new ByteArrayInputStream(body));
            this.body = body;
        }

        @Override
        public State with() {
            return new Buffering(body, getStream());
        }

    }

    private static final class Passing implements State {

    }

    RemoteRequest(final HttpServletRequest request, final FormRequestMode formRequestMode) {
        super(request);
        this.state = new AtomicReference<>(new Unbuffered(formRequestMode));
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
    public HttpHeaders getHeaders() {
        HttpHeaders headers = HttpHeaders.empty();
        final Enumeration<String> names = getHeaderNames();

        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            headers = headers.update(name, list(getHeaders(name)));
        }

        return headers;
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(getCharacterEncoding()).map(Charset::forName).orElse(UTF_8);
    }

    @Override
    public HttpRequest withBody() {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public HttpRequest withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return buffer().getInputStream(getRequest());
    }

    @Override
    public BufferedReader getReader() throws IOException {
        final InputStream stream = getInputStream();
        final Reader reader = new InputStreamReader(stream, getCharset());
        return new BufferedReader(reader);
    }

    @Override
    public byte[] getBody() {
        return buffer().getBody();
    }

    private State buffer() {
        return state.updateAndGet(throwingUnaryOperator(state ->
                state.buffer(getRequest())));
    }

    @SneakyThrows
    static String encode(final String s, final String charset) {
        return URLEncoder.encode(s, charset);
    }

}
