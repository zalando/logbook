package org.zalando.logbook.openfeign;

import com.sun.net.httpserver.HttpServer;
import feign.Feign;
import feign.FeignException;
import feign.Logger;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.logbook.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeignLogbookLoggerTest {
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

    private FeignClient client;
    private HttpServer server;

    @BeforeEach
    void setup() {
        when(writer.isActive()).thenReturn(true);
        Logbook logbook = Logbook.builder()
                .strategy(new TestStrategy())
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
                .build();

        FeignLogbookLogger interceptor = new FeignLogbookLogger(logbook);

        client = Feign.builder()
                .logger(interceptor)
                .logLevel(Logger.Level.FULL)
                .target(FeignClient.class, "http://localhost:8080");

        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/get/string", exchange -> {
                        String response = "response";
                        exchange.sendResponseHeaders(200, response.length());
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    }
            );
            server.createContext("/post/bad-request", exchange -> {
                        String response = "response";
                        exchange.sendResponseHeaders(400, response.length());
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    }
            );
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @AfterEach
    void destroy() {
        server.stop(1);
    }

    @Test
    void get200() throws IOException {
        client.getString();

        verify(writer).write(precorrelationCaptor.capture(), requestCaptor.capture());
        verify(writer).write(correlationCaptor.capture(), responseCaptor.capture());

        assertTrue(requestCaptor.getValue().contains("/get/string"));
        assertTrue(requestCaptor.getValue().contains("GET"));
        assertTrue(requestCaptor.getValue().contains("Remote: localhost"));
        assertTrue(requestCaptor.getValue().contains(precorrelationCaptor.getValue().getId()));

        assertEquals(precorrelationCaptor.getValue().getId(), correlationCaptor.getValue().getId());
        assertTrue(responseCaptor.getValue().contains(precorrelationCaptor.getValue().getId()));
        assertTrue(responseCaptor.getValue().contains("200 OK"));
        assertTrue(responseCaptor.getValue().contains("response"));
    }

    @Test
    void get200WithEmptyResponseBody() {
        client.getVoid();
    }

    @Test
    void get200WithNonEmptyResponseBody() {
        String response = "response";
        String actualResponseBody = client.getString();

        assertEquals(response, actualResponseBody);
    }

    @Test
    void post400() throws IOException {
        assertThrows(FeignException.BadRequest.class, () -> client.postBadRequest("request"));

        verify(writer).write(precorrelationCaptor.capture(), requestCaptor.capture());
        verify(writer).write(correlationCaptor.capture(), responseCaptor.capture());

        assertTrue(requestCaptor.getValue().contains("/post/bad-request"));
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
