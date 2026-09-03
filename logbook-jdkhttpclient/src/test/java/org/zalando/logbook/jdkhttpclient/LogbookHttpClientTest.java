package org.zalando.logbook.jdkhttpclient;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Nullable;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LogbookHttpClientTest extends AbstractHttpTest {

    private final HttpClient delegate = HttpClient.newHttpClient();
    private final LogbookHttpClient client = new LogbookHttpClient(logbook, delegate);

    @Override
    protected HttpResponse<String> sendAndReceive(final WireMockServer server, @Nullable final String body)
            throws IOException, InterruptedException {
        final HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(server.baseUrl() + "/"))
                .version(HttpClient.Version.HTTP_1_1);
        if (body == null) {
            builder.GET();
        } else {
            builder.POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "text/plain");
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void shouldSurviveRebuildWithAllAttributes() throws IOException, InterruptedException {
        server.stubFor(post("/test?key=value")
                .withHeader("X-Custom", equalTo("custom-val"))
                .withHeader("X-Multi", equalTo("val1"))
                .withRequestBody(equalTo("payload"))
                .willReturn(aResponse().withStatus(200).withBody("ok")));

        final HttpRequest request = HttpRequest.newBuilder(URI.create(server.baseUrl() + "/test?key=value"))
                .method("POST", HttpRequest.BodyPublishers.ofString("payload"))
                .header("Content-Type", "text/plain")
                .header("X-Custom", "custom-val")
                .header("X-Multi", "val1")
                .header("X-Multi", "val2")
                .timeout(Duration.ofSeconds(15))
                .version(HttpClient.Version.HTTP_1_1)
                .expectContinue(true)
                .build();

        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("ok");

        final String loggedRequest = captureRequest();
        assertThat(loggedRequest)
                .contains("POST http://localhost:" + server.port() + "/test?key=value HTTP/1.1")
                .contains("X-Custom: custom-val")
                .contains("payload");
    }

    @Test
    void shouldHandleMultiChunkRequestBody() throws IOException, InterruptedException {
        server.stubFor(post("/multi")
                .withRequestBody(equalTo("chunk1chunk2chunk3"))
                .willReturn(aResponse().withStatus(200).withBody("received")));

        final List<byte[]> chunks = List.of(
                "chunk1".getBytes(),
                "chunk2".getBytes(),
                "chunk3".getBytes()
        );

        final HttpRequest request = HttpRequest.newBuilder(URI.create(server.baseUrl() + "/multi"))
                .POST(HttpRequest.BodyPublishers.ofByteArrays(chunks))
                .header("Content-Type", "text/plain")
                .build();

        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("received");

        final String logged = captureRequest();
        assertThat(logged).contains("chunk1chunk2chunk3");
    }

    @Test
    void shouldDelegateAllHttpClientMethods() {
        final HttpClient mockDelegate = mock(HttpClient.class);
        final CookieHandler cookieHandler = mock(CookieHandler.class);
        final Duration timeout = Duration.ofSeconds(5);
        final HttpClient.Redirect redirect = HttpClient.Redirect.ALWAYS;
        final ProxySelector proxy = ProxySelector.of(new InetSocketAddress("localhost", 8080));
        final Authenticator authenticator = mock(Authenticator.class);
        final Executor executor = mock(Executor.class);

        when(mockDelegate.cookieHandler()).thenReturn(Optional.of(cookieHandler));
        when(mockDelegate.connectTimeout()).thenReturn(Optional.of(timeout));
        when(mockDelegate.followRedirects()).thenReturn(redirect);
        when(mockDelegate.proxy()).thenReturn(Optional.of(proxy));
        when(mockDelegate.sslContext()).thenReturn(delegate.sslContext());
        when(mockDelegate.sslParameters()).thenReturn(delegate.sslParameters());
        when(mockDelegate.authenticator()).thenReturn(Optional.of(authenticator));
        when(mockDelegate.version()).thenReturn(HttpClient.Version.HTTP_2);
        when(mockDelegate.executor()).thenReturn(Optional.of(executor));
        
        final WebSocket.Builder webSocketBuilder = mock(WebSocket.Builder.class);
        when(mockDelegate.newWebSocketBuilder()).thenReturn(webSocketBuilder);

        final LogbookHttpClient wrapper = new LogbookHttpClient(mockDelegate, logbook);

        assertThat(wrapper.cookieHandler()).contains(cookieHandler);
        assertThat(wrapper.connectTimeout()).contains(timeout);
        assertThat(wrapper.followRedirects()).isEqualTo(redirect);
        assertThat(wrapper.proxy()).contains(proxy);
        assertThat(wrapper.sslContext()).isNotNull();
        assertThat(wrapper.sslParameters()).isNotNull();
        assertThat(wrapper.authenticator()).contains(authenticator);
        assertThat(wrapper.version()).isEqualTo(HttpClient.Version.HTTP_2);
        assertThat(wrapper.executor()).contains(executor);
        assertThat(wrapper.newWebSocketBuilder()).isNotNull();
    }

    @Test
    void shouldThrowUnsupportedOperationExceptionOnSendAsync() {
        final HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost/")).build();

        assertThatThrownBy(() -> client.sendAsync(request, HttpResponse.BodyHandlers.discarding()))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("sendAsync is not supported yet");

        assertThatThrownBy(() -> client.sendAsync(request, HttpResponse.BodyHandlers.discarding(), null))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("sendAsync is not supported yet");
    }

    @Test
    void shouldSupportDefaultClientConstructor() {
        final LogbookHttpClient defaultClient = new LogbookHttpClient(logbook);
        assertThat(defaultClient.version()).isEqualTo(HttpClient.Version.HTTP_2);
    }
}
