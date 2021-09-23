package org.zalando.logbook.spring.webflux;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@SuppressWarnings({"NullableProblems"})
class BufferingServerHttpResponse extends ServerHttpResponseDecorator {
    private final ServerResponse serverResponse;
    private final Consumer<byte[]> bufferHook;

    BufferingServerHttpResponse(ServerHttpResponse delegate, ServerResponse serverResponse, Consumer<byte[]> bufferHook) {
        super(delegate);
        this.serverResponse = serverResponse;
        this.bufferHook = bufferHook;
    }
    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return super.writeWith(bufferingWrap(body));
    }

    private Publisher<? extends DataBuffer> bufferingWrap(Publisher<? extends DataBuffer> body) {
        if (serverResponse.shouldBuffer()) {
            return DataBufferCopyUtils.wrapAndBuffer(body, bytes -> {
                serverResponse.buffer(bytes);
                bufferHook.accept(bytes);
            });
        } else {
            return body;
        }
    }
}
