package org.zalando.logbook.httpclient;

import lombok.AllArgsConstructor;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;

@AllArgsConstructor
final class RemoteResponse implements org.zalando.logbook.HttpResponse {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());
    private final HttpResponse response;

    private interface State {

        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer(final HttpResponse response) throws IOException {
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
        public State buffer(final HttpResponse response) throws IOException {
            @Nullable final HttpEntity entity = response.getEntity();

            if (entity == null) {
                return new Passing();
            } else {
                final HttpEntities.Copy copy = HttpEntities.copy(entity);
                response.setEntity(copy);
                return new Buffering(copy.getBody());
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

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public String getProtocolVersion() {
        return response.getStatusLine().getProtocolVersion().toString();
    }

    @Override
    public int getStatus() {
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders headers = HttpHeaders.empty();

        final Set<Map.Entry<String, List<String>>> entries =
                Stream.of(response.getAllHeaders())
                        .collect(groupingBy(
                                Header::getName,
                                mapping(Header::getValue, toList())))
                        .entrySet();

        for (final Map.Entry<String, List<String>> entry : entries) {
            final String name = entry.getKey();
            final List<String> values = entry.getValue();

            headers = headers.update(name, values);
        }

        return headers;
    }

    @Override
    public String getContentType() {
        return Optional.of(response)
                .map(response -> response.getFirstHeader("Content-Type"))
                .map(Header::getValue)
                .orElse("");
    }

    @Override
    public Charset getCharset() {
        return Optional.of(response)
                .map(response -> response.getFirstHeader("Content-Type"))
                .map(Header::getValue)
                .map(ContentType::parse)
                .map(ContentType::getCharset)
                .orElse(UTF_8);
    }

    @Override
    public org.zalando.logbook.HttpResponse withBody() throws IOException {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public RemoteResponse withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    @Override
    public byte[] getBody() {
        return state.updateAndGet(throwingUnaryOperator(state ->
                state.buffer(response))).getBody();
    }

}
