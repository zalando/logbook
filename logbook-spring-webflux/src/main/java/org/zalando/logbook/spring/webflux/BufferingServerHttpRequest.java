package org.zalando.logbook.spring.webflux;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@SuppressWarnings({"NullableProblems"})
final class BufferingServerHttpRequest extends ServerHttpRequestDecorator {

    private final ServerRequest serverRequest;
    private final Consumer<byte[]> bufferHook;

    BufferingServerHttpRequest(ServerHttpRequest delegate, ServerRequest serverRequest, Consumer<byte[]> bufferHook) {
        super(delegate);
        this.serverRequest = serverRequest;
        this.bufferHook = bufferHook;
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return Flux.from(bufferingWrap(super.getBody()));
    }

    private Publisher<? extends DataBuffer> bufferingWrap(Publisher<? extends DataBuffer> body) {
        if (serverRequest.shouldBuffer()) {
            return DataBufferCopyUtils.wrapAndBuffer(body, bytes -> {
                serverRequest.buffer(bytes);
                bufferHook.accept(bytes);
            });
        } else {
            return body;
        }
    }
}