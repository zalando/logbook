package org.zalando.logbook.spring.webflux;

import lombok.RequiredArgsConstructor;
import org.apiguardian.api.API;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.zalando.logbook.api.Logbook;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;


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
                .doOnNext((e) -> stage.updateAndGet(throwingUnaryOperator(s -> ((Logbook) s).process(serverRequest))))
                .map(e -> e
                        .mutate()
                        .request(new BufferingServerHttpRequest(e.getRequest(), serverRequest, () -> stage.updateAndGet(throwingUnaryOperator(s -> {
                            if (s instanceof Logbook.RequestWritingStage) return ((Logbook.RequestWritingStage) s).write().process(serverResponse);
                            return s;
                        }))))
                        .response(new BufferingServerHttpResponse(e.getResponse(), serverResponse, () -> stage.updateAndGet(throwingUnaryOperator(s -> {
                            if (s instanceof Logbook.ResponseWritingStage) ((Logbook.ResponseWritingStage) s).write();
                            return s;
                        }))))
                        .build()
                )
                .flatMap(chain::filter)
                .then();
    }
}
