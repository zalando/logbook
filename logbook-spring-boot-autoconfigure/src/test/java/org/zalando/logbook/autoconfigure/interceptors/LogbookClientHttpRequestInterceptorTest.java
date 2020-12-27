package org.zalando.logbook.autoconfigure.interceptors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.*;
import org.zalando.logbook.httpclient.LogbookHttpRequestInterceptor;
import org.zalando.logbook.httpclient.LogbookHttpResponseInterceptor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
@RestClientTest
@AutoConfigureWebClient(registerRestTemplate = true)
class LogbookClientHttpRequestInterceptorTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MockRestServiceServer serviceServer;

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

    private Logbook logbook;

    private LogbookHttpRequestInterceptor requestInterceptor;
    private LogbookHttpResponseInterceptor responseInterceptor;

    private LogbookClientHttpRequestInterceptor interceptor;

    @BeforeEach
    void setup() {
        when(writer.isActive()).thenReturn(true);
        logbook = Logbook.builder()
                .strategy(new TestStrategy())
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
                .build();
        requestInterceptor = new LogbookHttpRequestInterceptor(logbook);
        responseInterceptor = new LogbookHttpResponseInterceptor();
        interceptor = new LogbookClientHttpRequestInterceptor(requestInterceptor, responseInterceptor);
        restTemplate.getInterceptors().add(interceptor);
    }

    @AfterEach
    void mockHttpVerify() {
        serviceServer.verify();
    }

    @Test
    void on200() throws IOException {
        serviceServer.expect(once(), requestTo("/test/get")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());
        restTemplate.getForObject("/test/get", Void.class);

        verify(writer).write(precorrelationCaptor.capture(), requestCaptor.capture());
        verify(writer).write(correlationCaptor.capture(), responseCaptor.capture());

        assertTrue(requestCaptor.getValue().contains("/test/get"));
        assertTrue(requestCaptor.getValue().contains("GET"));
        assertTrue(requestCaptor.getValue().contains("Remote: localhost"));
        assertTrue(requestCaptor.getValue().contains(precorrelationCaptor.getValue().getId()));

        assertEquals(precorrelationCaptor.getValue().getId(), correlationCaptor.getValue().getId());
        assertTrue(responseCaptor.getValue().contains(precorrelationCaptor.getValue().getId()));
        assertTrue(responseCaptor.getValue().contains("200 OK"));
    }

    @Test
    void on400() throws IOException {
        serviceServer.expect(once(), requestTo("/test/post")).andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());
        assertThrows(HttpClientErrorException.class, () -> restTemplate.postForObject("/test/post", null, Void.class));

        verify(writer).write(precorrelationCaptor.capture(), requestCaptor.capture());
        verify(writer).write(correlationCaptor.capture(), responseCaptor.capture());

        assertTrue(requestCaptor.getValue().contains("/test/post"));
        assertTrue(requestCaptor.getValue().contains("POST"));
        assertTrue(requestCaptor.getValue().contains("Remote: localhost"));
        assertTrue(requestCaptor.getValue().contains(precorrelationCaptor.getValue().getId()));

        assertEquals(precorrelationCaptor.getValue().getId(), correlationCaptor.getValue().getId());
        assertTrue(responseCaptor.getValue().contains(precorrelationCaptor.getValue().getId()));
        assertTrue(responseCaptor.getValue().contains("400 Bad Request"));
    }
}
