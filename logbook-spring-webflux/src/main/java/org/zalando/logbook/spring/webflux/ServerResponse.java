package org.zalando.logbook.spring.webflux;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MimeType;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
final class ServerResponse implements HttpResponse {

    private final ServerHttpResponse response;

    private final AtomicReference<State> state = new AtomicReference<>(new State.Unbuffered());

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public HttpHeaders getHeaders() {
        Map<String, List<String>> headersAsMultiValueMap = HttpHeadersConverter.of(response.getHeaders()).toMultiValueMap();
        return HttpHeaders.of(headersAsMultiValueMap);
    }

    @Override
    public int getStatus() {
        return Optional.ofNullable(response.getStatusCode())
                .orElse(HttpStatus.OK)
                .value();
    }

    @Nullable
    @Override
    public String getContentType() {
        return Optional.ofNullable(response.getHeaders().getContentType())
                .map(MimeType::toString)
                .orElse(null);
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(response.getHeaders().getContentType())
                .map(MimeType::getCharset)
                .orElse(UTF_8);
    }

    @Override
    public HttpResponse withBody() {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public HttpResponse withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    boolean shouldBuffer() {
        return state.get() instanceof State.Offering;
    }

    void buffer(byte[] message) {
        state.updateAndGet(s -> s.buffer(message));
    }

    @Override
    public byte[] getBody() {
        return state.get().getBody();
    }
}
