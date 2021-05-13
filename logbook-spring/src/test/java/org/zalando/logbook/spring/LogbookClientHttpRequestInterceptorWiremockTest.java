package org.zalando.logbook.spring;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.*;
import ru.lanwen.wiremock.config.WiremockCustomizer;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockUriResolver;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({
        WiremockResolver.class,
        WiremockUriResolver.class,
        MockitoExtension.class
})
public class LogbookClientHttpRequestInterceptorWiremockTest {

    @Mock
    private HttpLogWriter writer;
    @Captor
    private ArgumentCaptor<Correlation> correlationCaptor;
    @Captor
    private ArgumentCaptor<String> responseCaptor;

    private Logbook logbook;
    private LogbookClientHttpRequestInterceptor interceptor;

    public static class Endpoint implements WiremockCustomizer {

        static String URI = "/ping";

        @Override
        public void customize(com.github.tomakehurst.wiremock.WireMockServer server) throws Exception {
            server.stubFor(WireMock.get(urlEqualTo(URI))
                    .willReturn(aResponse()
                            .withBody("pong"))
            );
        }
    }

    @BeforeEach
    void setup() {
        when(writer.isActive()).thenReturn(true);
        logbook = Logbook.builder()
                .strategy(new TestStrategy())
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
                .build();
        interceptor = new LogbookClientHttpRequestInterceptor(logbook);
    }

    @Test
    void ping(@WiremockResolver.Wiremock(customizer = Endpoint.class) WireMockServer server,
              @WiremockUriResolver.WiremockUri String uri) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(restTemplate.getRequestFactory()));
        restTemplate.getInterceptors().add(interceptor);

        String out = restTemplate.getForObject(uri + Endpoint.URI, String.class);

        verify(writer).write(correlationCaptor.capture(), responseCaptor.capture());
        assertThat(out).isEqualTo("pong");
        assertThat(responseCaptor.getValue()).contains("pong");
    }
}
