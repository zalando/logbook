package org.zalando.logbook.jdkhttpclient;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.Logbook;

import java.net.http.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;

class LogbookHttpClientFactoryTest {

    @Test
    void wrapsJdkHttpClients() {
        final HttpClient client = new LogbookHttpClientFactory(Logbook.create(), false)
                .wrap(HttpClient.newHttpClient());

        assertThat(client).isInstanceOf(LogbookHttpClient.class);
    }

}
