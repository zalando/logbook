package org.zalando.logbook.openfeign;

import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteResponseTest {
    @Test
    void defaultBody() throws IOException {
        assertThat(unit(helloWorld()).getBody()).asString().isEqualTo("");
    }

    @Test
    void withBody() throws IOException {
        assertThat(unit(helloWorld()).withBody().getBody()).asString().isEqualTo("hello world");
    }

    private Response helloWorld() {
        return Response.builder()
                .status(200)
                .body("hello world", StandardCharsets.UTF_8)
                .request(Request.create(
                        Request.HttpMethod.GET,
                        "https://localhost:8080",
                        Collections.emptyMap(),
                        Request.Body.create(new byte[0]),
                        new RequestTemplate()
                ))
                .build();
    }

    private HttpResponse unit(Response response) throws IOException {
        return RemoteResponse.create(response, ByteStreams.toByteArray(response.body().asInputStream()));
    }
}
