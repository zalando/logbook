package org.zalando.logbook.openfeign;

import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.api.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteResponseTest {
    @Test
    void defaultBody() throws IOException {
        assertThat(unit(request()).getBody()).asString().isEqualTo("");
    }

    @Test
    void withBody() throws IOException {
        assertThat(unit(request()).withBody().getBody()).asString().isEqualTo("hello world");
    }

    @Test
    void withoutBody() throws IOException {
        assertThat(unit(request()).withoutBody().getBody()).asString().isEqualTo("");
    }

    @Test
    void requestNullBodyWithBody() throws IOException {
        assertThat(unitNullBody(request()).withBody().getBody()).asString().isEqualTo("");
    }

    @Test
    void requestNullBodyWithoutBody() throws IOException {
        assertThat(unitNullBody(request()).withoutBody().getBody()).asString().isEqualTo("");
    }

    private Response request() {
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

    private Response requestWithHeaders() {
        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put("Content-Type", Collections.singleton("application/json; charset=utf-16be"));

        return Response.builder()
                .status(200)
                .body("hello world", StandardCharsets.UTF_8)
                .request(Request.create(
                        Request.HttpMethod.GET,
                        "https://localhost:8080",
                        headers,
                        Request.Body.create(new byte[0]),
                        new RequestTemplate()
                ))
                .headers(headers)
                .build();
    }

    @Test
    void nullContentType() throws IOException {
        assertThat(unit(request()).getContentType()).isEqualTo(null);
    }

    @Test
    void contentTypeExists() throws IOException {
        assertThat(unit(requestWithHeaders()).getContentType()).isEqualTo("application/json; charset=utf-16be");
    }

    @Test
    void parseCharset() throws IOException {
        Response response = requestWithHeaders();

        assertThat(unit(response).getCharset()).isEqualTo(StandardCharsets.UTF_16BE);
    }

    @Test
    void parseMissingCharset() throws IOException {
        Response response = request();

        assertThat(unit(response).getCharset()).isEqualTo(StandardCharsets.UTF_8);
    }

    private HttpResponse unit(Response response) throws IOException {
        return RemoteResponse.create(response, ByteStreams.toByteArray(response.body().asInputStream()));
    }

    private HttpResponse unitNullBody(Response response) {
        return RemoteResponse.create(response, null);
    }
}
