package org.zalando.logbook.spring.webflux;

import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServerRequestUnitTest {

    @Test
    void shouldBeEmptyIfPortIsNegative() {
        ServerHttpRequest mock = mock(ServerHttpRequest.class);
        when(mock.getURI()).thenReturn(URI.create("https://example.com:-1"));

        ServerRequest request = new ServerRequest(mock);
        assertThat(request.getPort()).isEmpty();
    }

    @Test
    void shouldReturnAttributesIfPresent() {
        Map<String, Object> expectedAttributes = new HashMap<>();
        expectedAttributes.put("foo", "bar");
        expectedAttributes.put("userId", 12345);

        ServerHttpRequest mock = mock(ServerHttpRequest.class);
        when(mock.getAttributes()).thenReturn(expectedAttributes);

        ServerRequest request = new ServerRequest(mock);
        assertEquals(expectedAttributes, request.getAttributes());
    }

    @Test
    void shouldReturnEmptyAttributesIfNone() {
        ServerHttpRequest mock = mock(ServerHttpRequest.class);
        when(mock.getAttributes()).thenReturn(new HashMap<>());

        ServerRequest request = new ServerRequest(mock);
        assertThat(request.getAttributes()).isEmpty();
    }
}