package org.zalando.logbook.spring.webflux;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ClientRequestUnitTest {

    @Test
    void shouldBeEmptyIfPortIsNegative() {
        org.springframework.web.reactive.function.client.ClientRequest mock = mock(org.springframework.web.reactive.function.client.ClientRequest.class);
        when(mock.url()).thenReturn(URI.create("https://example.com:-1"));

        ClientRequest request = new ClientRequest(mock);
        assertThat(request.getPort()).isEmpty();
    }
}