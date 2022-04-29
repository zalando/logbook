package org.zalando.logbook.httpclient5;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.*;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.Origin;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.*;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;

@RequiredArgsConstructor
final class RemoteResponse implements org.zalando.logbook.HttpResponse {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());
    private final HttpResponse response;
    private final EntityDetails entityDetails;
    private final ByteBuffer body;

    RemoteResponse(HttpResponse response) {
        this.response = response;
        this.body = null;
        this.entityDetails = null;
    }

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

        default State buffer(final EntityDetails entity, final ByteBuffer body) {
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
            if (response instanceof ClassicHttpResponse) {
                ClassicHttpResponse classicResponse = (ClassicHttpResponse) response;
                HttpEntity entity = (classicResponse).getEntity();
                if (entity == null) {
                    return new Passing();
                } else {
                    final HttpEntities.Copy copy = HttpEntities.copy(entity);
                    classicResponse.setEntity(copy);
                    return new Buffering(copy.getBody());
                }
            } else {
                return new Passing();
            }
        }

        @Override
        public State buffer(EntityDetails entityDetails, ByteBuffer body) {
            byte[] buffer = new byte[(int) entityDetails.getContentLength()];
            ByteBufferUtils.fixedSizeCopy(body, buffer);
            return new Buffering(buffer);
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
        ProtocolVersion version = response.getVersion();
        if (version != null) {
            return version.toString();
        } else {
            return HttpVersion.HTTP_1_1.toString();
        }
    }

    @Override
    public int getStatus() {
        return response.getCode();
    }

    @Override
    public String getReasonPhrase(){
        return response.getReasonPhrase();
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders headers = HttpHeaders.empty();

        final Set<Map.Entry<String, List<String>>> entries =
                Stream.of(response.getHeaders())
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
        if (entityDetails != null) {
            return entityDetails.getContentType();
        } else {
            return Optional.of(response)
                    .map(response -> response.getFirstHeader(CONTENT_TYPE))
                    .map(Header::getValue)
                    .orElse("");
        }
    }

    @Override
    public Charset getCharset() {
        return Optional.of(response)
                .map(response -> response.getFirstHeader(CONTENT_TYPE))
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
        return state.updateAndGet(throwingUnaryOperator(state -> (body != null) ? state.buffer(Objects.requireNonNull(entityDetails), body) : state.buffer(response))).getBody();
    }

}
