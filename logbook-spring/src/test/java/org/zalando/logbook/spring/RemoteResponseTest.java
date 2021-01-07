package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RemoteResponseTest {

    @Test
    void statusCanThrow() throws IOException {
        MockClientHttpResponse response = mock(MockClientHttpResponse.class);
        when(response.getRawStatusCode()).thenThrow(new IOException("io exception"));
        assertThatThrownBy(() -> unit(response).getStatus()).hasMessageContaining("io exception");
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

    private org.zalando.logbook.HttpResponse unit(MockClientHttpResponse response) {
        return new RemoteResponse(response);
    }
}
