package org.zalando.logbook.openfeign;

import feign.Feign;
import feign.Logger;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.RequestWritingStage;
import org.zalando.logbook.test.TestStrategy;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.Logbook.ResponseProcessingStage;
import static org.zalando.logbook.Logbook.builder;

@ExtendWith(MockitoExtension.class)
class FeignLogbookLoggerExceptionTest extends FeignHttpServerRunner {
    @Mock
    private Logbook logbook;

    @Mock
    private RequestWritingStage requestStage;
    @Mock
    private ResponseProcessingStage responseStage;

    private FeignClient client;
    private FeignLogbookLogger interceptor;

    @BeforeEach
    void setup() {
        logbook = spy(builder().strategy(new TestStrategy()).build());
        interceptor = new FeignLogbookLogger(logbook);
        client = Feign.builder()
                .logger(interceptor)
                .logLevel(Logger.Level.FULL)
                .target(FeignClient.class, "http://localhost:8080");
    }

    @Test
    void requestThrowsIOException() throws IOException {
        doThrow(IOException.class).when(logbook).process(any());
        assertThrows(UncheckedIOException.class, client::getString);
    }

    @Test
    void responseThrowsIOException() throws IOException {
        when(logbook.process(any())).thenReturn(requestStage);
        when(requestStage.write()).thenReturn(responseStage);
        doThrow(IOException.class).when(responseStage).process(any());

        assertThrows(UncheckedIOException.class, client::getString);
    }

    @Test
    void ioExceptionIsLogged() {
        FeignClient invalidClient = Feign.builder()
                .logger(interceptor)
                .logLevel(Logger.Level.FULL)
                .target(FeignClient.class, "http://localhost:9999");

        assertThrows(RetryableException.class, invalidClient::getString);
    }
}
