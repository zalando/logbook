package org.zalando.logbook.httpclient;

/*
 * #%L
 * Logbook: HTTP Client
 * %%
 * Copyright (C) 2015 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.zalando.logbook.Logbook;

import java.io.IOException;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.util.EntityUtils.toByteArray;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class LogbookHttpInterceptorsTest extends AbstractHttpTest {

    private final Logbook logbook = Logbook.builder()
            .writer(writer)
            .build();

    private final CloseableHttpClient client = HttpClientBuilder.create()
            .addInterceptorFirst(new LogbookHttpRequestInterceptor(logbook))
            .addInterceptorFirst(new LogbookHttpResponseInterceptor())
            .build();

    @Override
    public void stop() throws IOException {
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
