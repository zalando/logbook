package org.zalando.logbook.spring.webflux;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import reactor.core.publisher.Mono;

@SuppressWarnings({"NullableProblems"})
final class BufferingClientHttpRequest extends ClientHttpRequestDecorator {
    private final ClientRequest clientRequest;

    BufferingClientHttpRequest(ClientHttpRequest delegate, ClientRequest clientRequest) {
        super(delegate);
        this.clientRequest = clientRequest;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return super.writeWith(bufferingWrap(body));
    }

    private Publisher<? extends DataBuffer> bufferingWrap(Publisher<? extends DataBuffer> body) {
        if (clientRequest.shouldBuffer()) {
            return DataBufferCopyUtils.wrapAndBuffer(body, clientRequest::buffer);
        } else {
            return body;
        }
    }
}
