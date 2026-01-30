package org.zalando.logbook.spring.webflux;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.TRACE;

@SuppressWarnings({"NullableProblems"})
final class BufferingServerHttpRequest extends ServerHttpRequestDecorator {

    private static final Set<HttpMethod> METHODS_WITHOUT_BODY = Set.of(GET, DELETE, TRACE, OPTIONS, HEAD);

    private final ServerRequest serverRequest;
    private final Runnable writeHook;

    BufferingServerHttpRequest(ServerHttpRequest delegate, ServerRequest serverRequest, Runnable writeHook) {
        super(delegate);
        this.serverRequest = serverRequest;
        this.writeHook = writeHook;
        if (METHODS_WITHOUT_BODY.contains(super.getMethod()) || !serverRequest.shouldBuffer()) {
            writeHook.run();
        }
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return Flux.from(bufferingWrap(super.getBody()));
    }

    private Publisher<? extends DataBuffer> bufferingWrap(Publisher<? extends DataBuffer> body) {
        if (serverRequest.shouldBuffer()) {
            return Flux
                .from(DataBufferCopyUtils.wrapAndBuffer(body, serverRequest::buffer))
                .doOnComplete(writeHook);
        } else {
            return Mono.fromRunnable(writeHook)
                .thenMany(body);
        }
    }
}
