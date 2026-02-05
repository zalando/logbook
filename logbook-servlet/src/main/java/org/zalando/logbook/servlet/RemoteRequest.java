package org.zalando.logbook.servlet;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.zalando.logbook.ContentType;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;
import org.zalando.logbook.common.MediaTypeQuery;

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

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.Collections.list;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PROTECTED;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;
import static org.zalando.logbook.ContentType.CONTENT_TYPE_HEADER;
import static org.zalando.logbook.servlet.ByteStreams.toByteArray;

final class RemoteRequest extends HttpServletRequestWrapper implements HttpRequest {

    private final AtomicReference<State> state;
    private Optional<AsyncListener> asyncListener = Optional.empty();

    /**
     * Manages the lifecycle of HTTP request body buffering for servlet requests.
     *
     * <h3>State Machine Overview</h3>
     *
     * The state machine controls when and how the request body is read and buffered,
     * with special handling for form-encoded requests:
     *
     * <pre>
     *     Unbuffered
     *       ↙     ↘
     *    with()  without()
     *     ↙         ↘
     *  Offering   Withouted
     *     ↓           ↑
     *  buffer()      with()
     *     ↓           |
     *  Buffering     (back to Offering)
     *  or Passing
     * (for form OFF)
     *     ↓
     *  without()
     *     ↓
     *  Ignoring ⇄ Buffering (via with())
     * </pre>
     *
     * <h3>State Descriptions</h3>
     *
     * <ul>
     * <li><b>Unbuffered</b>: Initial state. Body has not been read yet. Contains FormRequestMode
     * configuration to determine how form bodies should be handled. Transitions occur when
     * logging preference is set:
     *   <ul>
     *   <li>{@code with()} → Offering: Body reading will be offered/logged</li>
     *   <li>{@code without()} → Withouted: Body reading explicitly rejected</li>
     *   <li>{@code buffer()} → Buffering or Passing: Body is buffered based on FormRequestMode</li>
     *   </ul>
     * </li>
     *
     * <li><b>Offering</b>: Body logging is desired but not yet buffered. Waits for eager buffering:
     *   <ul>
     *   <li>{@code buffer()} → Buffering or Passing: Body is read and cached (or skipped for
     *   form OFF mode)</li>
     *   </ul>
     * </li>
     *
     * <li><b>Withouted</b>: Body logging was explicitly rejected without buffering. Can return
     * to Offering if needed later:
     *   <ul>
     *   <li>{@code with()} → Offering: Body logging is now desired</li>
     *   </ul>
     * </li>
     *
     * <li><b>Buffering</b>: Body has been read from the stream and cached in memory. Provides
     * buffered body and allows toggling visibility. For form requests, body may be reconstructed
     * from parameters based on FormRequestMode:
     *   <ul>
     *   <li>{@code without()} → Ignoring: Switch to hiding the buffered body from consumers</li>
     *   </ul>
     * </li>
     *
     * <li><b>Passing</b>: Special state for form-encoded requests when FormRequestMode is OFF.
     * Body is not buffered; the original stream is passed through. Used to bypass buffering
     * for performance when form bodies are not needed.
     * </li>
     *
     * <li><b>Ignoring</b>: Body was buffered but is being hidden from downstream consumers.
     * The body remains cached internally but appears empty to callers. Can switch back:
     *   <ul>
     *   <li>{@code with()} → Buffering: Reveal the buffered body again</li>
     *   </ul>
     * </li>
     * </ul>
     *
     * <h3>Form Request Handling (FormRequestMode)</h3>
     *
     * For requests with Content-Type {@code application/x-www-form-urlencoded}:
     * <ul>
     * <li><b>PARAMETER</b>: Body is reconstructed from servlet parameters after parsing</li>
     * <li><b>BODY</b>: Body is buffered as-is from the stream</li>
     * <li><b>OFF</b>: No buffering; uses Passing state to avoid memory overhead</li>
     * </ul>
     *
     * <h3>Key Design Points</h3>
     *
     * <ul>
     * <li><b>Lazy Buffering</b>: The body is not buffered until actually needed (when
     * {@code buffer()} is called), allowing efficient handling of requests where logging
     * is configured but the body is never read.</li>
     *
     * <li><b>State Reusability</b>: Once buffered, the same body can be shown/hidden multiple
     * times by transitioning between Buffering and Ignoring states.</li>
     *
     * <li><b>Eager Call on with()</b>: When {@code with()} is called (via
     * {@link RemoteRequest#withBody()}), buffering is eagerly triggered to ensure the body is
     * captured before it can be consumed elsewhere.</li>
     *
     * <li><b>Stream Replayability</b>: By caching the body bytes, the stream can be
     * "replayed" multiple times without re-reading from the network.</li>
     *
     * <li><b>Form Optimization</b>: The Passing state allows bypassing buffering entirely
     * for form requests when not needed, reducing memory usage in high-traffic scenarios.</li>
     * </ul>
     */
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
        private final Charset charset;

        @Override
        public State with() {
            return new Offering(formRequestMode, charset);
        }

        @Override
        public State without() {
            return new Withouted(formRequestMode, charset);
        }

        @Override
        public State buffer(final ServletRequest request) throws IOException {
            return doBuffer(request, formRequestMode, charset);
        }

    }

    @AllArgsConstructor
    private static final class Withouted implements State {

        private final FormRequestMode formRequestMode;
        private final Charset charset;

        @Override
        public State with() {
            return new Offering(formRequestMode, charset);
        }

    }

    @AllArgsConstructor
    private static final class Offering implements State {

        private final FormRequestMode formRequestMode;
        private final Charset charset;

        @Override
        public State buffer(final ServletRequest request) throws IOException {
            return doBuffer(request, formRequestMode, charset);
        }
    }

    private static State doBuffer(final ServletRequest request, final FormRequestMode formRequestMode, final Charset charset) throws IOException {
        if (isFormRequest(request)) {
            switch (formRequestMode) {
                case PARAMETER:
                    return new Buffering(reconstructFormBody(request, charset));
                case OFF:
                case BODY:
                    return new Passing();
                default:
                    break;
            }
        }

        return new Buffering(toByteArray(request.getInputStream()));
    }

    private static boolean isFormRequest(final ServletRequest request) {
        final Predicate<String> FORM_REQUEST = MediaTypeQuery.compile("application/x-www-form-urlencoded", "multipart/*");
        return Optional.ofNullable(request.getContentType())
                .filter(FORM_REQUEST)
                .isPresent();
    }

    private static byte[] reconstructFormBody(final ServletRequest request, final Charset charset) {
        return request.getParameterMap().entrySet().stream()
                .flatMap(entry -> Arrays.stream(entry.getValue())
                        .map(value -> encode(entry.getKey(), "UTF-8") + "=" + encode(value, "UTF-8")))
                .collect(joining("&"))
                .getBytes(charset);
    }

    @AllArgsConstructor
    private static abstract class Streaming implements State {

        @Getter(PROTECTED)
        private final ByteArrayInputStream stream;

    }

    private static abstract class WithBody extends Streaming {

        protected final byte[] body;

        protected WithBody(final byte[] body, final ByteArrayInputStream stream) {
            super(stream);
            this.body = body;
        }

        @Override
        public byte[] getBody() {
            return body;
        }

    }

    private static final class Buffering extends WithBody {

        Buffering(final byte[] body) {
            this(body, new ByteArrayInputStream(body));
        }

        Buffering(final byte[] body, final ByteArrayInputStream stream) {
            super(body, stream);
        }

        @Override
        public ServletInputStream getInputStream(final ServletRequest request) {
            return new ServletInputStreamAdapter(new ByteArrayInputStream(body));
        }

        @Override
        public State without() {
            return new Ignoring(body);
        }

    }

    private static final class Ignoring extends WithBody {

        Ignoring(final byte[] body) {
            super(body, new ByteArrayInputStream(body));
        }

        @Override
        public ServletInputStream getInputStream(final ServletRequest request) {
            return new ServletInputStreamAdapter(new ByteArrayInputStream(new byte[0]));
        }

        @Override
        public byte[] getBody() {
            return new byte[0];
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
        this.state = new AtomicReference<>(new Unbuffered(formRequestMode, getCharset()));
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
        final String contentTypeHeader = getHeaders().getFirst(CONTENT_TYPE_HEADER);

        return Optional
                .ofNullable(contentTypeHeader)
                .map(ContentType::parseCharset)
                /*
                 * Servlet Spec, 3.12 Request data encoding
                 *
                 * [..] the default encoding of a request the container uses to create the request reader and
                 * parse POST data must be ISO-8859-1
                 */
                .orElse(ISO_8859_1);
    }

    @Override
    public HttpRequest withBody() {
        state.updateAndGet(State::with);
        buffer();
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

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        final AsyncContext asyncContext = super.startAsync();
        asyncListener.ifPresent(asyncContext::addListener);
        return asyncContext;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        final AsyncContext asyncContext = super.startAsync(servletRequest, servletResponse);
        asyncListener.ifPresent(asyncContext::addListener);
        return asyncContext;
    }

    public void setAsyncListener(Optional<AsyncListener> asyncListener) {
        this.asyncListener = asyncListener;
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
