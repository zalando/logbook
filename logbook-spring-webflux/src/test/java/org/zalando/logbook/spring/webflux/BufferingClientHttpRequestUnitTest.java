package org.zalando.logbook.spring.webflux;


import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ClientHttpRequest;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BufferingClientHttpRequestUnitTest {

    @Test
    void shouldNotBufferIfNotNeeded() {
        ClientHttpRequest clientHttpRequest = mock(ClientHttpRequest.class);
        org.springframework.web.reactive.function.client.ClientRequest springClientRequest = mock(org.springframework.web.reactive.function.client.ClientRequest.class);
        ClientRequest clientRequest = new ClientRequest(springClientRequest);
        clientRequest.withoutBody();

        when(clientHttpRequest.writeWith(any())).thenReturn(Mono.empty());

        BufferingClientHttpRequest request = new BufferingClientHttpRequest(clientHttpRequest, clientRequest);
        Exception ex = null;
        try {
            request.writeWith(Mono.empty()).block();
        } catch (Exception e) {
            ex = e;
        }

        assertThat(ex).isNull();
    }
}