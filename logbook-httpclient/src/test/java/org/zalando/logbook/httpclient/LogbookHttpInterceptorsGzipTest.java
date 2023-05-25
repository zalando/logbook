package org.zalando.logbook.httpclient;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.POST;
import static com.github.restdriver.clientdriver.RestClientDriver.giveEmptyResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponseAsBytes;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static org.apache.http.HttpHeaders.CONTENT_ENCODING;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.TEXT_PLAIN;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
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

public final class LogbookHttpInterceptorsGzipTest extends AbstractHttpTest {

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
        driver.reset();
        if (body == null) {
            driver.addExpectation(onRequestTo("/").withMethod(GET), giveEmptyResponse());
        } else {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
                gzipOutputStream.write(body.getBytes(StandardCharsets.UTF_8));
            }

            byte[] compressedBytes = outputStream.toByteArray();
            driver.addExpectation(onRequestTo("/").withMethod(POST),
                                  giveResponseAsBytes(new ByteArrayInputStream(compressedBytes), "text/plain").withHeader(CONTENT_ENCODING, "gzip"));
        }
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
