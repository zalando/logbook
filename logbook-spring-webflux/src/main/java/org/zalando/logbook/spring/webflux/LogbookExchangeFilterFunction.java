package org.zalando.logbook.spring.webflux;

import lombok.RequiredArgsConstructor;
import org.apiguardian.api.API;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.zalando.logbook.Logbook;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.springframework.http.HttpHeaders.TRANSFER_ENCODING;
import static org.zalando.fauxpas.FauxPas.throwingConsumer;
import static org.zalando.fauxpas.FauxPas.throwingFunction;
import static org.zalando.fauxpas.FauxPas.throwingSupplier;

@RequiredArgsConstructor
@API(status = EXPERIMENTAL)
@SuppressWarnings({"NullableProblems"})
public class LogbookExchangeFilterFunction implements ExchangeFilterFunction {

    private final Logbook logbook;

    @Override
    public Mono<org.springframework.web.reactive.function.client.ClientResponse> filter(org.springframework.web.reactive.function.client.ClientRequest request, ExchangeFunction next) {
        ClientRequest clientRequest = new ClientRequest(request);
        Logbook.RequestWritingStage requestWritingStage = throwingSupplier(() -> logbook.process(clientRequest)).get();

        return next
                .exchange(org.springframework.web.reactive.function.client.ClientRequest
                        .from(request)
                        .body((outputMessage, context) -> request.body().insert(new BufferingClientHttpRequest(outputMessage, clientRequest), context))
                        .build()
                )
                .doOnError(throwingConsumer(throwable -> requestWritingStage.write()))
                .flatMap(throwingFunction(response -> {
                    Logbook.ResponseProcessingStage responseProcessingStage = requestWritingStage.write();

                    ClientResponse clientResponse = new ClientResponse(response);
                    Logbook.ResponseWritingStage responseWritingStage = responseProcessingStage.process(clientResponse);

                    return Mono
                            .just(response)
                            .flatMap(it -> {
                                HttpHeaders responseHeaders = response.headers().asHttpHeaders();
                                if (clientResponse.shouldBuffer() && (responseHeaders.getContentLength() > 0 || !CollectionUtils.isEmpty(responseHeaders.get(TRANSFER_ENCODING)))) {
                                    return it
                                            .bodyToMono(byte[].class)
                                            .doOnNext(clientResponse::buffer)
                                            .map(b -> response.mutate().body(Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(b))).build())
                                            .switchIfEmpty(Mono.just(it));
                                } else {
                                    return Mono.just(it);
                                }
                            })
                            .doOnNext(throwingConsumer(b -> responseWritingStage.write()));
                }));
    }
}
