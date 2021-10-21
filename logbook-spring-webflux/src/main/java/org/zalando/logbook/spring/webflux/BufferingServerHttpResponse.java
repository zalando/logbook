package org.zalando.logbook.spring.webflux;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Mono;


@SuppressWarnings({"NullableProblems"})
class BufferingServerHttpResponse extends ServerHttpResponseDecorator {
    private final ServerResponse serverResponse;

    BufferingServerHttpResponse(ServerHttpResponse delegate, ServerResponse serverResponse, Runnable writeHook) {
        super(delegate);
        this.serverResponse = serverResponse;
        beforeCommit(() -> {
            writeHook.run();
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return super.writeWith(bufferingWrap(body));
    }

    private Publisher<? extends DataBuffer> bufferingWrap(Publisher<? extends DataBuffer> body) {
        if (serverResponse.shouldBuffer()) {
            return DataBufferCopyUtils.wrapAndBuffer(body, serverResponse::buffer);
        } else {
            return body;
        }
    }
}
