package org.zalando.logbook.httpclient5;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.AfterEach;

import javax.annotation.Nullable;
import java.io.IOException;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
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

}
