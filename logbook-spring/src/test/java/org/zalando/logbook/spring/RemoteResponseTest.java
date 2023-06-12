package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.zalando.logbook.HttpResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RemoteResponseTest {

    @Test
    void statusCanThrow() throws IOException {
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenThrow(new IOException("io exception"));
        assertThatThrownBy(() -> new RemoteResponse(response).getStatus()).hasMessageContaining("io exception");
    }

    @Test
    @SuppressWarnings("deprecation")
    void statusSpring2() throws IOException {
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenThrow(new NoSuchMethodError());
        when(response.getRawStatusCode()).thenReturn(200);
        assertEquals(new RemoteResponse(response).getStatus(), 200);
    }

    @Test
    @SuppressWarnings("deprecation")
    void getRawStatusThrows() throws IOException {
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenThrow(new NoSuchMethodError());
        when(response.getRawStatusCode()).thenThrow(new IOException("io exception"));
        assertThatThrownBy(() -> new RemoteResponse(response).getStatus()).hasMessageContaining("io exception");
    }

    @Test
    void defaultBody() throws IOException {
        assertThat(unit(helloWorld()).getBody()).asString().isEqualTo("");
    }

    @Test
    void withBody() throws IOException {
        assertThat(unit(helloWorld()).withBody().getBody()).asString().isEqualTo("hello world");
    }

    private MockClientHttpResponse helloWorld() {
        return new MockClientHttpResponse("hello world".getBytes(), HttpStatus.OK);
    }

    private HttpResponse unit(ClientHttpResponse response) {
        return new RemoteResponse(response);
    }
}
