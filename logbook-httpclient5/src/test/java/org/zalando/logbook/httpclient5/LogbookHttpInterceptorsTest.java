package org.zalando.logbook.httpclient5;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.AfterEach;

import jakarta.annotation.Nullable;
import java.io.IOException;

import static org.apache.hc.core5.http.ContentType.TEXT_PLAIN;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;

public final class LogbookHttpInterceptorsTest extends AbstractHttpTest {

    private final CloseableHttpClient client = HttpClientBuilder.create()
            .addRequestInterceptorFirst(new LogbookHttpRequestInterceptor(logbook))
            .addResponseInterceptorFirst(new LogbookHttpResponseInterceptor())
            .build();

    @AfterEach
    void stop() throws IOException {
        client.close();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected ClassicHttpResponse sendAndReceive(@Nullable final String body) throws IOException {
        if (body == null) {
            return client.execute(new HttpGet(server.baseUrl()));
        } else {
            final HttpPost post = new HttpPost(server.baseUrl());
            post.setEntity(new StringEntity(body));
            post.setHeader(CONTENT_TYPE, TEXT_PLAIN.toString());
            return client.execute(post);
        }
    }

}
