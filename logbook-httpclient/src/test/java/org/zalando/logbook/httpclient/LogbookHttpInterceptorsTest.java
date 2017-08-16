package org.zalando.logbook.httpclient;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.zalando.logbook.Logbook;

import java.io.IOException;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.util.EntityUtils.toByteArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class LogbookHttpInterceptorsTest extends AbstractHttpTest {

    private final Logbook logbook = Logbook.builder()
            .writer(writer)
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
    protected void sendAndReceive() throws IOException {
        driver.addExpectation(onRequestTo("/"),
                giveResponse("Hello, world!", "text/plain"));

        try (CloseableHttpResponse response = client.execute(new HttpGet(driver.getBaseUrl()))) {
            assertThat(response.getStatusLine().getStatusCode(), is(200));
            assertThat(new String(toByteArray(response.getEntity()), UTF_8), is("Hello, world!"));
        }
    }

}
