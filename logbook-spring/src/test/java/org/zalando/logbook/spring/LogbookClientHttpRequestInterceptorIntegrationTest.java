package org.zalando.logbook.spring;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.DefaultSink;
import org.zalando.logbook.DefaultStrategy;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.StreamHttpLogWriter;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class LogbookClientHttpRequestInterceptorIntegrationTest {

    static WireMockServer wireMockServer;

    static String baseUrl;

    @BeforeAll
    static void createWireMockServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        baseUrl = wireMockServer.baseUrl();
    }

    @AfterAll
    static void stopWireMockServer() {
        wireMockServer.stop();
    }

    @Test
    @SneakyThrows
    void errorNotLogWhenServerReturnError() {
        HttpLogFormatter httpLogFormatter = Mockito.mock(HttpLogFormatter.class);
        when(httpLogFormatter.format(Mockito.any(Correlation.class), Mockito.<HttpResponse>argThat(response -> {
            try {
                String responseBody = response.getBodyAsString();
                // fail on this
                assertThat(responseBody).isNotBlank();
                return StringUtils.isNotBlank(responseBody);
            } catch (IOException e) {
                return false;
            }
        })))
                .thenReturn("LOG STRING");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(
                new LogbookClientHttpRequestInterceptor(
                        Logbook.builder()
                                .sink(new DefaultSink(httpLogFormatter, new StreamHttpLogWriter()))
                                .strategy(new DefaultStrategy())
                                .build()
                )
        ));

        wireMockServer.stubFor(WireMock.post(WireMock.urlPathEqualTo("/error-result"))
                .withRequestBody(WireMock.equalToJson("{\"field\": 1}"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withHeader("content-type", "application/json")
                        .withBody("{\"error\": \"error message\"}")));
        ImmutableMap<String, Integer> body = ImmutableMap.of("field", 1);

        //
        assertThatThrownBy(() -> restTemplate.postForEntity(baseUrl + "/error-result", body, Object.class))
                .isInstanceOf(HttpServerErrorException.class)
                .satisfies(e -> {
                    HttpServerErrorException httpServerErrorException = (HttpServerErrorException) e;
                    assertThat(httpServerErrorException.getResponseBodyAsString()).isNotBlank();
                });
    }

    @Test
    @SneakyThrows
    void errorNotLogWhenServerReturnError4xx() {
        HttpLogFormatter httpLogFormatter = Mockito.mock(HttpLogFormatter.class);
        when(httpLogFormatter.format(Mockito.any(Correlation.class), Mockito.<HttpResponse>argThat(response -> {
            try {
                String responseBody = response.getBodyAsString();
                // fail on this
                assertThat(responseBody).isNotBlank();
                return StringUtils.isNotBlank(responseBody);
            } catch (IOException e) {
                return false;
            }
        })))
                .thenReturn("LOG STRING");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(
                new LogbookClientHttpRequestInterceptor(
                        Logbook.builder()
                                .sink(new DefaultSink(httpLogFormatter, new StreamHttpLogWriter()))
                                .strategy(new DefaultStrategy())
                                .build()
                )
        ));

        wireMockServer.stubFor(WireMock.post(WireMock.urlPathEqualTo("/client-error-result"))
                .withRequestBody(WireMock.equalToJson("{\"field\": 1}"))
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withHeader("content-type", "application/json")
                        .withBody("{\"error\": \"error message\"}")));
        ImmutableMap<String, Integer> body = ImmutableMap.of("field", 1);

        //
        assertThatThrownBy(() -> restTemplate.postForEntity(baseUrl + "/client-error-result", body, Object.class))
                .isInstanceOf(HttpClientErrorException.class)
                .satisfies(e -> {
                    HttpClientErrorException httpServerErrorException = (HttpClientErrorException) e;
                    assertThat(httpServerErrorException.getResponseBodyAsString()).isNotBlank();
                });
    }
}

