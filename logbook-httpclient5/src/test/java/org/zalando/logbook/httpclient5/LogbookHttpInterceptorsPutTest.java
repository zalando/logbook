package org.zalando.logbook.httpclient5;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class LogbookHttpInterceptorsPutTest {

    private final WireMockServer server = new WireMockServer(options().dynamicPort());

    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    @BeforeEach
    void defaultBehaviour() {
        server.start();
        when(writer.isActive()).thenReturn(true);
    }

    private final Logbook logbook = Logbook.builder()
            .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
            .build();

    private final CloseableHttpClient client = HttpClientBuilder.create()
            .addRequestInterceptorFirst(new LogbookHttpRequestInterceptor(logbook))
            .addResponseInterceptorFirst(new LogbookHttpResponseInterceptor())
            .build();

    @AfterEach
    void stop() throws IOException {
        server.stop();
        client.close();
    }

    @Test
    @SuppressWarnings("deprecation")
    void shouldLogRequestWithoutBody() throws IOException {
        server.stubFor(put("/").willReturn(aResponse().withStatus(204)));

        server.stubFor(get("/").willReturn(aResponse().withStatus(200).withBody("Hello, world!")));

        final HttpPut request = new HttpPut(server.baseUrl());

        client.execute(request);

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(format("PUT http://localhost:%d/ HTTP/1.1", server.port()))
                .doesNotContain("Content-Type")
                .doesNotContain("Hello, world!");
    }

    private String captureRequest() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        return captor.getValue();
    }

}
