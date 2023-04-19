package org.zalando.logbook.httpclient5;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.nio.AsyncDataProducer;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.Origin;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;

final class LocalRequest implements org.zalando.logbook.HttpRequest {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());

    private final HttpRequest request;
    private final EntityDetails entity;
    private final URI originalRequestUri;

    private interface State {

        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer(final HttpRequest request, final EntityDetails entity) throws IOException {
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
        public State buffer(final HttpRequest request, final EntityDetails entity) throws IOException {
            if (request instanceof ClassicHttpRequest) {
                final ClassicHttpRequest original = (ClassicHttpRequest) request;
                final HttpEntity httpEntity = original.getEntity();
                if (httpEntity == null) {
                    return new Passing();
                } else {
                    final HttpEntities.Copy copy = HttpEntities.copy(httpEntity);
                    original.setEntity(copy);
                    return new Buffering(copy.getBody());
                }
            } else if (entity instanceof AsyncDataProducer) {
                int contentLength = (int) entity.getContentLength();
                byte[] body = new byte[contentLength];
                BufferingFixedSizeDataStreamChannel channel = new BufferingFixedSizeDataStreamChannel(body);
                ((AsyncDataProducer) entity).produce(channel);
                return new Buffering(channel.getBuffer());
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

    @SneakyThrows
    LocalRequest(final HttpRequest request, final EntityDetails entity) {
        this.request = request;
        this.originalRequestUri = request.getUri();
        this.entity = entity;
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public String getProtocolVersion() {
        ProtocolVersion version = request.getVersion();
        if (version != null) {
            return version.format();
        } else {
            return HttpVersion.HTTP_1_1.toString();
        }
    }

    @Override
    public String getRemote() {
        return "localhost";
    }

    @Override
    public String getMethod() {
        return request.getMethod();
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
    public HttpHeaders getHeaders() {
        HttpHeaders headers = HttpHeaders.empty();

        final Set<Entry<String, List<String>>> entries =
                Stream.of(request.getHeaders())
                        .collect(groupingBy(
                                Header::getName,
                                mapping(Header::getValue, toList())))
                        .entrySet();

        for (final Entry<String, List<String>> entry : entries) {
            final String name = entry.getKey();
            final List<String> values = entry.getValue();

            headers = headers.update(name, values);
        }

        return headers;
    }

    @Override
    public String getContentType() {
        if (entity != null) {
            return entity.getContentType();
        } else {
            return Optional.of(request)
                    .map(request -> request.getFirstHeader(CONTENT_TYPE))
                    .map(Header::getValue)
                    .orElse("");
        }
    }

    @Override
    public Charset getCharset() {
        return Optional.of(request)
                .map(request -> request.getFirstHeader(CONTENT_TYPE))
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
                state.buffer(request, entity))).getBody();
    }
}
