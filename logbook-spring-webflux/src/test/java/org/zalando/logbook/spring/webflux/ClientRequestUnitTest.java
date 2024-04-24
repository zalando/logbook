package org.zalando.logbook.spring.webflux;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


final class ClientRequestUnitTest {

    @Test
    void shouldBeEmptyIfPortIsNegative() {
        org.springframework.web.reactive.function.client.ClientRequest mock = mock(org.springframework.web.reactive.function.client.ClientRequest.class);
        when(mock.url()).thenReturn(URI.create("https://example.com:-1"));

        ClientRequest request = new ClientRequest(mock);
        assertThat(request.getPort()).isEmpty();
    }

    @Test
    void shouldReturnAttributesIfPresent() {
        Map<String, Object> expectedAttributes = new HashMap<>();
        expectedAttributes.put("foo", "bar");
        org.springframework.web.reactive.function.client.ClientRequest mock = mock(org.springframework.web.reactive.function.client.ClientRequest.class);
        when(mock.attributes()).thenReturn(expectedAttributes);

        ClientRequest request = new ClientRequest(mock);
        assertEquals(expectedAttributes, request.getAttributes());
    }
}
