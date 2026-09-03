package org.zalando.logbook.jdkhttpclient;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.Origin;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class RemoteResponseTest {

    @SuppressWarnings("unchecked")
    private HttpResponse<Void> mockResponse(final int status, final HttpClient.Version version, final Map<String, List<String>> headers) {
        final HttpResponse<Void> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.version()).thenReturn(version);
        when(response.headers()).thenReturn(HttpHeaders.of(headers, (k, v) -> true));
        return response;
    }

    @Test
    void shouldResolveOriginStatusAndReasonPhrase() {
        final HttpResponse<Void> response = mockResponse(200, HttpClient.Version.HTTP_1_1, Map.of());
        final RemoteResponse unit = new RemoteResponse(response, new byte[0]);

        assertThat(unit.getOrigin()).isEqualTo(Origin.REMOTE);
        assertThat(unit.getStatus()).isEqualTo(200);
        assertThat(unit.getReasonPhrase()).isEqualTo("OK");
    }

    @Test
    void shouldResolveProtocolVersion() {
        final HttpResponse<Void> http1 = mockResponse(200, HttpClient.Version.HTTP_1_1, Map.of());
        assertThat(new RemoteResponse(http1, new byte[0]).getProtocolVersion()).isEqualTo("HTTP/1.1");

        final HttpResponse<Void> http2 = mockResponse(200, HttpClient.Version.HTTP_2, Map.of());
        assertThat(new RemoteResponse(http2, new byte[0]).getProtocolVersion()).isEqualTo("HTTP/2");
    }

    @Test
    void shouldResolveHeaders() {
        final HttpResponse<Void> response = mockResponse(200, HttpClient.Version.HTTP_1_1, Map.of(
                "X-Custom", List.of("value1"),
                "X-Multi", List.of("a", "b")
        ));
        final RemoteResponse unit = new RemoteResponse(response, new byte[0]);

        assertThat(unit.getHeaders()).hasSize(2);
        assertThat(unit.getHeaders().get("x-custom")).containsExactly("value1");
        assertThat(unit.getHeaders().get("x-multi")).containsExactly("a", "b");
    }

    @Test
    void shouldResolveContentTypeAndCharset() {
        final HttpResponse<Void> res1 = mockResponse(200, HttpClient.Version.HTTP_1_1, Map.of(
                "Content-Type", List.of("text/plain; charset=ISO-8859-1")
        ));
        final RemoteResponse unit1 = new RemoteResponse(res1, new byte[0]);
        assertThat(unit1.getContentType()).isEqualTo("text/plain; charset=ISO-8859-1");
        assertThat(unit1.getCharset()).isEqualTo(ISO_8859_1);

        final HttpResponse<Void> res2 = mockResponse(200, HttpClient.Version.HTTP_1_1, Map.of(
                "Content-Type", List.of("application/json")
        ));
        final RemoteResponse unit2 = new RemoteResponse(res2, new byte[0]);
        assertThat(unit2.getContentType()).isEqualTo("application/json");
        assertThat(unit2.getCharset()).isEqualTo(UTF_8);

        final HttpResponse<Void> res3 = mockResponse(200, HttpClient.Version.HTTP_1_1, Map.of());
        final RemoteResponse unit3 = new RemoteResponse(res3, new byte[0]);
        assertThat(unit3.getContentType()).isEmpty();
        assertThat(unit3.getCharset()).isEqualTo(UTF_8);
    }

    @Test
    void shouldReturnEmptyBodyUntilCaptured() throws IOException {
        final HttpResponse<Void> response = mockResponse(200, HttpClient.Version.HTTP_1_1, Map.of());
        final byte[] body = "response data".getBytes(UTF_8);
        final RemoteResponse unit = new RemoteResponse(response, body);

        assertThat(unit.getBody()).isEmpty();
        assertThat(unit.withBody().getBody()).isEqualTo(body);
    }

    @Test
    void shouldSupportStateTransitions() throws IOException {
        final HttpResponse<Void> response = mockResponse(200, HttpClient.Version.HTTP_1_1, Map.of());
        final byte[] body = "response data".getBytes(UTF_8);
        final RemoteResponse unit = new RemoteResponse(response, body);

        unit.withBody().withBody();
        assertThat(unit.getBody()).isEqualTo(body);

        unit.withoutBody().withoutBody();
        assertThat(unit.getBody()).isEmpty();

        unit.withBody();
        assertThat(unit.getBody()).isEqualTo(body);
    }
}
