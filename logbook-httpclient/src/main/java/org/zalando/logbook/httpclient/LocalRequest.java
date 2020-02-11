package org.zalando.logbook.httpclient;

import lombok.AllArgsConstructor;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.zalando.logbook.Headers;
import org.zalando.logbook.Origin;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.apache.http.util.EntityUtils.toByteArray;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;

final class LocalRequest implements org.zalando.logbook.HttpRequest {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());

    private final HttpRequest request;
    private final URI originalRequestUri;

    private interface State {

        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer(final HttpRequest request) throws IOException {
            return this;
        }

        default byte[] getBody() {
            return new byte[0];
        }

    }

    private static final class Unbuffered implements State {

        @Override
        public State with() {
            return new Offering();
        }

    }

    private static final class Offering implements State {

        @Override
        public State without() {
            return new Unbuffered();
        }

        @Override
        public State buffer(final HttpRequest request) throws IOException {
            if (request instanceof HttpEntityEnclosingRequest) {
                final HttpEntityEnclosingRequest original = (HttpEntityEnclosingRequest) request;
                if (original.getEntity() == null) {
                    return new Passing();
                } else {
                    final byte[] body = toByteArray(original.getEntity());
                    original.setEntity(new ByteArrayEntity(body));
                    return new Buffering(body);
                }
            } else {
                return new Passing();
            }
        }

    }

    @AllArgsConstructor
    private static final class Buffering implements State {

        private final byte[] body;

        @Override
        public State without() {
            return new Ignoring(this);
        }

        @Override
        public byte[] getBody() {
            return body;
        }

    }

    @AllArgsConstructor
    private static final class Ignoring implements State {

        private final Buffering buffering;

        @Override
        public State with() {
            return buffering;
        }

    }

    private static final class Passing implements State {

    }

    LocalRequest(final HttpRequest request) {
        this.request = request;
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
        return "localhost";
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
        final Map<String, List<String>> headers = Headers.empty();

        Stream.of(request.getAllHeaders())
                .collect(groupingBy(Header::getName, mapping(Header::getValue, toList())))
                .forEach(headers::put);

        // TODO immutable?
        return headers;
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
    public org.zalando.logbook.HttpRequest withBody() {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public org.zalando.logbook.HttpRequest withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    @Override
    public byte[] getBody() {
        return state.updateAndGet(throwingUnaryOperator(state ->
                state.buffer(request))).getBody();
    }

}
