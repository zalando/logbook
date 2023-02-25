package org.zalando.logbook.httpclient5;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.Correlation;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.POST;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponseAsBytes;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static org.apache.hc.core5.http.ContentType.TEXT_PLAIN;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class LogbookHttpExecHandlerTest extends AbstractHttpTest {
    private final CloseableHttpClient client = HttpClientBuilder.create()
            .addExecInterceptorFirst("Logbook", new LogbookHttpExecHandler(logbook))
            .build();

    @AfterEach
    void stop() throws IOException {
        client.close();
    }

    @Override
    protected ClassicHttpResponse sendAndReceive(@Nullable final String body) throws IOException {
        driver.addExpectation(onRequestTo("/"),
                giveResponse("Hello, world!", "text/plain"));

        if (body == null) {
            return client.execute(new HttpGet(driver.getBaseUrl()));
        } else {
            final HttpPost post = new HttpPost(driver.getBaseUrl());
            post.setEntity(new StringEntity(body));
            post.setHeader(CONTENT_TYPE, TEXT_PLAIN.toString());
            return client.execute(post);
        }
    }

    @Test
    void shouldLogCompressedResponseWithBody() throws IOException, ParseException {
        byte[] compressedResponse = new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1, -13, 72, -51, -55, -55, -41, 81,
                72, -50, -49, 45, 40, 74, 45, 46, 78, 77, 81, 40, -49, 47, -54, 73, 81, 4, 0, 5, -67, 83, 110, 24, 0, 0, 0};
        driver.addExpectation(onRequestTo("/").withMethod(POST),
                giveResponseAsBytes(new ByteArrayInputStream(compressedResponse), "text/plain")
                        .withHeader("Content-Encoding", "gzip"));

        final HttpPost post = new HttpPost(driver.getBaseUrl());
        post.setEntity(new StringEntity("Hello, world!"));
        post.setHeader(CONTENT_TYPE, TEXT_PLAIN.toString());
        final CloseableHttpResponse response = client.execute(post);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getEntity()).isNotNull();
        assertThat(EntityUtils.toString(response.getEntity())).isEqualTo("Hello, compressed world!");

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Correlation.class), captor.capture());
        final String message = captor.getValue();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK", "Content-Type: text/plain", "Hello, compressed world!");
    }
}