package org.zalando.logbook.spring.webflux;

import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ServerRequestUnitTest {

    @Test
    void shouldBeEmptyIfPortIsNegative() {
        ServerHttpRequest mock = mock(ServerHttpRequest.class);
        when(mock.getURI()).thenReturn(URI.create("https://example.com:-1"));

        ServerRequest request = new ServerRequest(mock);
        assertThat(request.getPort()).isEmpty();
    }

}
