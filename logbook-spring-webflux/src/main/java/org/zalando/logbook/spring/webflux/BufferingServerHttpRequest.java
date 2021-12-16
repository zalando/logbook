package org.zalando.logbook.spring.webflux;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

import java.util.*;

import static org.springframework.http.HttpMethod.*;

@SuppressWarnings({"NullableProblems"})
final class BufferingServerHttpRequest extends ServerHttpRequestDecorator {

    private static final Set<HttpMethod> METHODS_WITHOUT_BODY = new HashSet<>(Arrays.asList(GET, DELETE, TRACE, OPTIONS, HEAD));

    private final ServerRequest serverRequest;
    private final Runnable writeHook;

    BufferingServerHttpRequest(ServerHttpRequest delegate, ServerRequest serverRequest, Runnable writeHook) {
        super(delegate);
        this.serverRequest = serverRequest;
        this.writeHook = writeHook;
    }

    @Override
    public HttpMethod getMethod() {
        HttpMethod method = super.getMethod();
        // the only way so far to check if request has body or not
        if (METHODS_WITHOUT_BODY.contains(method)) {
            writeHook.run();
        }
        return method;
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
            return body;
        }
    }
}