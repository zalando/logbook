package org.zalando.logbook.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.test.TestStrategy;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.apache.http.HttpHeaders.CONTENT_ENCODING;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.TEXT_PLAIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public final class LogbookHttpInterceptorsGzipTest extends AbstractHttpTest {

    private final Logbook logbook = Logbook.builder()
                                           .strategy(new TestStrategy())
                                           .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
                                           .build();

    private final CloseableHttpClient client = HttpClientBuilder.create()
                                                                .addInterceptorFirst(new LogbookHttpRequestInterceptor(logbook))
                                                                .addInterceptorFirst(new LogbookHttpResponseInterceptor(true))
                                                                .build();

    @AfterEach
    void stop() throws IOException {
        client.close();
    }

    @Override
    protected HttpResponse sendAndReceive(@Nullable final String body) throws IOException {
        return sendAndReceiveWithGzipEncoding(body, "gzip");
    }

    private CloseableHttpResponse sendAndReceiveWithGzipEncoding(String body, String encoding) throws IOException {
        server.resetMappings();
        if (body == null) {
            server.stubFor(get("/").willReturn(aResponse().withStatus(204)));
        } else {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
                gzipOutputStream.write(body.getBytes(StandardCharsets.UTF_8));
            }

            byte[] compressedBytes = outputStream.toByteArray();
            server.stubFor(post("/").willReturn(aResponse()
                    .withStatus(200)
                    .withBody(compressedBytes)
                    .withHeader(CONTENT_ENCODING, encoding)
                    .withHeader(CONTENT_TYPE, TEXT_PLAIN.toString())));
        }
        if (body == null) {
            return client.execute(new HttpGet(server.baseUrl()));
        } else {
            final HttpPost post = new HttpPost(server.baseUrl());
            post.setEntity(new StringEntity(body));
            post.setHeader(CONTENT_TYPE, TEXT_PLAIN.toString());

            return client.execute(post);
        }
    }

    @Test
    void shouldLogResponseWithBodyGzipHeaderVariant1() throws IOException, ExecutionException, InterruptedException {
        extracted("GziP");
    }

    @Test
    void shouldLogResponseWithBodyGzipHeaderVariant2() throws IOException, ExecutionException, InterruptedException {
        extracted("GZIP");
    }

    @Test
    void shouldLogResponseWithBodyGzipHeaderVariant3() throws IOException, ExecutionException, InterruptedException {
        extracted("x-gzip");
    }

    @Test
    void shouldLogResponseWithBodyGzipHeaderVariant4() throws IOException, ExecutionException, InterruptedException {
        extracted("X-GZIP");
    }

    @Test
    void shouldLogResponseWithBodyGzipHeaderVariant5() throws IOException, ExecutionException, InterruptedException {
        extracted("x-GziP");
    }

    private void extracted(String encoding) throws IOException {
        server.stubFor(get("/").willReturn(aResponse().withStatus(200).withBody("Hello, world!")));

        final HttpResponse response = sendAndReceiveWithGzipEncoding("Hello, world!", encoding);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        assertThat(EntityUtils.toString(response.getEntity())).isEqualTo("Hello, world!");

        final String message = captureResponse();

        assertThat(message)
            .startsWith("Incoming Response:")
            .contains("HTTP/1.1 200 OK", "Content-Type: text/plain", "Hello, world!");
    }

    private String captureResponse() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Correlation.class), captor.capture());
        return captor.getValue();
    }
}
