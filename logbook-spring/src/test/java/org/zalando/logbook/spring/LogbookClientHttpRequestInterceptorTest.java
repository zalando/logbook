package org.zalando.logbook.spring;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.DefaultSink;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.TestStrategy;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogbookClientHttpRequestInterceptorTest {

    private final WireMockServer server = new WireMockServer(options().dynamicPort());

    @Mock
    private HttpLogWriter writer;

    @Captor
    private ArgumentCaptor<String> requestCaptor;

    @Captor
    private ArgumentCaptor<String> responseCaptor;

    @Captor
    private ArgumentCaptor<Precorrelation> precorrelationCaptor;

    @Captor
    private ArgumentCaptor<Correlation> correlationCaptor;

    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        server.start();
        when(writer.isActive()).thenReturn(true);
        Logbook logbook = Logbook.builder()
                .strategy(new TestStrategy())
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
                .build();
        LogbookClientHttpRequestInterceptor interceptor = new LogbookClientHttpRequestInterceptor(logbook);
        restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getInterceptors().add(interceptor);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void get200() throws IOException {
        server.stubFor(get("/test/get/withcontent").willReturn(aResponse().withStatus(200).withBody("response")));

        restTemplate.getForObject(server.baseUrl() + "/test/get/withcontent", String.class);

        verify(writer).write(precorrelationCaptor.capture(), requestCaptor.capture());
        verify(writer).write(correlationCaptor.capture(), responseCaptor.capture());

        assertTrue(requestCaptor.getValue().contains("/test/get"));
        assertTrue(requestCaptor.getValue().contains("GET"));
        assertTrue(requestCaptor.getValue().contains("Remote: localhost"));
        assertTrue(requestCaptor.getValue().contains(precorrelationCaptor.getValue().getId()));

        assertEquals(precorrelationCaptor.getValue().getId(), correlationCaptor.getValue().getId());
        assertTrue(responseCaptor.getValue().contains(precorrelationCaptor.getValue().getId()));
        assertTrue(responseCaptor.getValue().contains("200 OK"));
        assertTrue(responseCaptor.getValue().contains("response"));
    }

    @Test
    void get200WithEmptyResponseBody(){
        server.stubFor(get("/test/get/withoutcontent").willReturn(aResponse().withStatus(200)));
        restTemplate.getForObject(server.baseUrl() + "/test/get/withoutcontent", Void.class);
    }

    @Test
    void get200WithNonEmptyResponseBody() {
        server.stubFor(get("/test/get/withcontent").willReturn(aResponse().withStatus(200).withBody("response")));
        String actualResponseBody = restTemplate.getForObject(server.baseUrl() + "/test/get/withcontent", String.class);

        assertNotNull(actualResponseBody);
        assertEquals("response", actualResponseBody);
    }

    @Test
    void post400() throws IOException {
        server.stubFor(post("/test/post/withcontent").willReturn(aResponse().withStatus(400).withBody("response")));
        assertThrows(HttpClientErrorException.class, () -> restTemplate.postForObject(server.baseUrl() + "/test/post/withcontent", "request", String.class));

        verify(writer).write(precorrelationCaptor.capture(), requestCaptor.capture());
        verify(writer).write(correlationCaptor.capture(), responseCaptor.capture());

        assertTrue(requestCaptor.getValue().contains("/test/post/withcontent"));
        assertTrue(requestCaptor.getValue().contains("POST"));
        assertTrue(requestCaptor.getValue().contains("Remote: localhost"));
        assertTrue(requestCaptor.getValue().contains(precorrelationCaptor.getValue().getId()));
        assertTrue(requestCaptor.getValue().contains("request"));

        assertEquals(precorrelationCaptor.getValue().getId(), correlationCaptor.getValue().getId());
        assertTrue(responseCaptor.getValue().contains(precorrelationCaptor.getValue().getId()));
        assertTrue(responseCaptor.getValue().contains("400 Bad Request"));
        assertTrue(responseCaptor.getValue().contains("response"));

        server.verify(postRequestedFor(urlEqualTo("/test/post/withcontent")).withRequestBody(equalTo("request")));
    }
}
