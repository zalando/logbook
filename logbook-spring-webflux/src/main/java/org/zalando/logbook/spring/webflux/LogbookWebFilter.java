package org.zalando.logbook.spring.webflux;

import lombok.RequiredArgsConstructor;
import org.apiguardian.api.API;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.zalando.logbook.Logbook;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.zalando.fauxpas.FauxPas.*;


@RequiredArgsConstructor
@API(status = EXPERIMENTAL)
@SuppressWarnings({"NullableProblems"})
public class LogbookWebFilter implements WebFilter {

    private final Logbook logbook;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerRequest serverRequest = new ServerRequest(exchange.getRequest());
        ServerResponse serverResponse = new ServerResponse(exchange.getResponse());

        AtomicReference<Object> stage = new AtomicReference<>(logbook);

        return Mono
                .just(exchange)
                .doOnNext((e) -> triggerStageChange(stage, serverRequest, serverResponse))
                .map(e -> exchange
                        .mutate()
                        .request(new BufferingServerHttpRequest(e.getRequest(), serverRequest, bytes -> triggerStageChange(stage, serverRequest, serverResponse)))
                        .response(new BufferingServerHttpResponse(e.getResponse(), serverResponse, bytes -> triggerStageChange(stage, serverRequest, serverResponse)))
                        .build()
                )
                .flatMap(chain::filter)
                .doOnSuccess(it -> triggerStageChange(stage, serverRequest, serverResponse)) // ensure response has been written
                .then();
    }

    private void triggerStageChange(AtomicReference<Object> stage, ServerRequest request, ServerResponse response) {
        stage.updateAndGet(throwingUnaryOperator(s -> {
            if (s instanceof Logbook) return ((Logbook) s).process(request);
            if (s instanceof Logbook.RequestWritingStage) return ((Logbook.RequestWritingStage) s).write().process(response);
            if (s instanceof Logbook.ResponseWritingStage) ((Logbook.ResponseWritingStage) s).write();
            return null;
        }));
    }
}
