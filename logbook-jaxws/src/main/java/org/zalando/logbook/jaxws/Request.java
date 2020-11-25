package org.zalando.logbook.jaxws;

import lombok.AllArgsConstructor;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.soap.SOAPMessage.CHARACTER_SET_ENCODING;
import static javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;

@AllArgsConstructor
final class Request implements HttpRequest {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());

    private final HttpMessageContext context;
    private final Origin origin;

    @Override
    public String getProtocolVersion() {
        return context.getProtocolVersion();
    }

    @Override
    public Origin getOrigin() {
        return origin;
    }

    @Override
    public String getRemote() {
        return context.getRemote();
    }

    @Override
    public String getMethod() {
        return context.getMethod();
    }

    @Override
    public String getScheme() {
        return context.getScheme();
    }

    @Override
    public String getHost() {
        return context.getHost();
    }

    @Override
    public Optional<Integer> getPort() {
        return context.getPort();
    }

    @Override
    public String getPath() {
        return context.getPath();
    }

    @Override
    public String getQuery() {
        return context.getQuery();
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.of(getRawHeaders());
    }

    @Override
    @Nullable
    public String getContentType() {
        return Optional.ofNullable(getRawHeaders().get("Content-Type"))
                .map(Iterable::iterator)
                .filter(Iterator::hasNext)
                .map(Iterator::next)
                .orElse(null);
    }

    private Map<String, List<String>> getRawHeaders() {
        @SuppressWarnings("unchecked") @Nullable final Map<String, List<String>> headers =
                (Map<String, List<String>>) context.get(HTTP_REQUEST_HEADERS);

        return Optional.ofNullable(headers).orElse(Collections.emptyMap());
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(context.get(CHARACTER_SET_ENCODING))
                .map(Object::toString)
                .map(Charset::forName)
                .orElse(UTF_8);
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
    public byte[] getBody() {
        return state.updateAndGet(throwingUnaryOperator(state ->
                state.buffer(context.getMessage()))).getBody();
    }

}