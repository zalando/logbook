package org.zalando.logbook.spring.webflux;


import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BufferingServerHttpResponseUnitTest {

    @Test
    void shouldNotBufferIfNotNeeded() {
        ServerHttpResponse serverHttpResponse = mock(ServerHttpResponse.class);
        ServerResponse serverResponse = new ServerResponse(serverHttpResponse);
        serverResponse.withoutBody();

        when(serverHttpResponse.writeWith(any())).thenReturn(Mono.empty());

        BufferingServerHttpResponse response = new BufferingServerHttpResponse(serverHttpResponse, serverResponse, bytes -> {});
        Exception ex = null;
        try {
            response.writeWith(Mono.empty()).block();
        } catch (Exception e) {
            ex = e;
        }

        assertThat(ex).isNull();
    }
}