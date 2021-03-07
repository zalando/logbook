package org.zalando.logbook.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.DefaultSink;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.TestStrategy;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class LogbookClientHttpRequestInterceptorTest {

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
    private MockRestServiceServer serviceServer;
    private Logbook logbook;

    private LogbookClientHttpRequestInterceptor interceptor;

    @BeforeEach
    void setup() {
        when(writer.isActive()).thenReturn(true);
        logbook = Logbook.builder()
                .strategy(new TestStrategy())
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
                .build();
        interceptor = new LogbookClientHttpRequestInterceptor(logbook);
        restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(interceptor);
        serviceServer = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterEach
    void mockHttpVerify() {
        serviceServer.verify();
    }

    @Test
    void get200() throws IOException {
        serviceServer.expect(once(), requestTo("/test/get")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess().body("response"));
        restTemplate.getForObject("/test/get", String.class);

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
        serviceServer.expect(once(), requestTo("/test/get")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        restTemplate.getForObject("/test/get", Void.class);
    }

    @Test
    void get200WithNonEmptyResponseBody() {
        String expectedResponseBody = "response";
        serviceServer.expect(once(), requestTo("/test/get")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess().body(expectedResponseBody));

        String actualResponseBody = restTemplate.getForObject("/test/get", String.class);

        assertNotNull(actualResponseBody);
        assertEquals(expectedResponseBody, actualResponseBody);
    }

    @Test
    void post400() throws IOException {
        serviceServer.expect(once(), requestTo("/test/post")).andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest().body("response"));
        assertThrows(HttpClientErrorException.class, () -> restTemplate.postForObject("/test/post", "request", Void.class));

        verify(writer).write(precorrelationCaptor.capture(), requestCaptor.capture());
        verify(writer).write(correlationCaptor.capture(), responseCaptor.capture());

        assertTrue(requestCaptor.getValue().contains("/test/post"));
        assertTrue(requestCaptor.getValue().contains("POST"));
        assertTrue(requestCaptor.getValue().contains("Remote: localhost"));
        assertTrue(requestCaptor.getValue().contains(precorrelationCaptor.getValue().getId()));
        assertTrue(requestCaptor.getValue().contains("request"));

        assertEquals(precorrelationCaptor.getValue().getId(), correlationCaptor.getValue().getId());
        assertTrue(responseCaptor.getValue().contains(precorrelationCaptor.getValue().getId()));
        assertTrue(responseCaptor.getValue().contains("400 Bad Request"));
        assertTrue(responseCaptor.getValue().contains("response"));
    }
}
