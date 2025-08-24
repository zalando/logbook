package org.zalando.logbook.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.test.TestStrategy;

import javax.annotation.Nullable;
import java.io.IOException;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;

public final class LogbookHttpInterceptorsTest extends AbstractHttpTest {

    private final Logbook logbook = Logbook.builder()
            .strategy(new TestStrategy())
            .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
            .build();

    private final CloseableHttpClient client = HttpClientBuilder.create()
            .addInterceptorFirst(new LogbookHttpRequestInterceptor(logbook))
            .addInterceptorFirst(new LogbookHttpResponseInterceptor())
            .build();

    @AfterEach
    void stop() throws IOException {
        client.close();
    }

    @Override
    protected HttpResponse sendAndReceive(@Nullable final String body) throws IOException {
        if (body == null) {
            return client.execute(new HttpGet(server.baseUrl()));
        } else {
            final HttpPost post = new HttpPost(server.baseUrl());
            post.setEntity(new StringEntity(body));
            post.setHeader(CONTENT_TYPE, "text/plain");
            return client.execute(post);
        }
    }

}
