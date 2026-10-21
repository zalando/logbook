package org.zalando.logbook.openfeign;

import static feign.Util.UTF_8;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feign.Logger.Level;
import feign.Request;
import feign.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.RequestWritingStage;
import org.zalando.logbook.Logbook.ResponseProcessingStage;
import org.zalando.logbook.Logbook.ResponseWritingStage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;

class FeignLogbookLoggerBufferLimitTest {

    private final Logbook logbook = mock(Logbook.class);
    private final ResponseProcessingStage responseStage = mock(ResponseProcessingStage.class);
    private final Request request =
            Request.create(
                    Request.HttpMethod.GET, "/asset", Collections.emptyMap(), null, UTF_8, null);
    private final ArgumentCaptor<HttpResponse> loggedResponse =
            ArgumentCaptor.forClass(HttpResponse.class);
    private FeignLogbookLogger logger;

    @BeforeEach
    void setup() throws IOException {
        final RequestWritingStage requestStage = mock(RequestWritingStage.class);
        when(logbook.process(any())).thenReturn(requestStage);
        when(requestStage.write()).thenReturn(responseStage);
        when(responseStage.process(any())).thenReturn(mock(ResponseWritingStage.class));
        logger = new FeignLogbookLogger(logbook, 4);
    }

    @ParameterizedTest
    @CsvSource({"abc, 3", "abcd, 4", "abc, null", "abcd, null"})
    void buffersBodiesWithinLimit(String content, String length) throws IOException {
        final InputStream input = spy(new ByteArrayInputStream(content.getBytes(UTF_8)));
        final Response original = response(input, length);
        final Response.Body originalBody = spy(original.body());

        try (Response result = log(original.toBuilder().body(originalBody).build())) {
            verify(responseStage).process(loggedResponse.capture());
            assertThat(loggedResponse.getValue().withBody().getBody())
                    .isEqualTo(content.getBytes(UTF_8));
            assertThat(result.body().asInputStream().readAllBytes())
                    .isEqualTo(content.getBytes(UTF_8));
            verify(input, atLeastOnce()).close();
            verify(originalBody).close();
        }
    }

    @ParameterizedTest
    @CsvSource({"null, 5", "10, 0", "2, 5", "4, 5"})
    void omitsOversizedBodiesAndPreservesStream(String length, int bytesRead) throws IOException {
        final byte[] content = "secret-body".getBytes(UTF_8);
        final InputStream input = spy(new ByteArrayInputStream(content));
        final Response original = response(input, length);
        final Response.Body originalBody = spy(original.body());

        try (Response result = log(original.toBuilder().body(originalBody).build())) {
            verify(responseStage).process(loggedResponse.capture());
            assertThat(loggedResponse.getValue().withBody().getBody()).isEmpty();
            assertThat(loggedResponse.getValue().getStatus()).isEqualTo(200);
            assertThat(loggedResponse.getValue().getHeaders().getFirst("Content-Type"))
                    .isEqualTo("application/octet-stream");
            assertThat(input.available()).isEqualTo(content.length - bytesRead);
            verify(input, never()).close();
            verify(originalBody, never()).close();
            assertThat(result.body().length()).isEqualTo(originalBody.length());
            assertThat(result.body().asInputStream().readAllBytes()).isEqualTo(content);
        }
        verify(originalBody).close();
    }

    @Test
    void closingUnreadOverflowResponseClosesOriginalBody() throws IOException {
        final InputStream input = spy(new ByteArrayInputStream("secret-body".getBytes(UTF_8)));
        final Response original = response(input, "null");
        final Response.Body originalBody = spy(original.body());
        final Response result = log(original.toBuilder().body(originalBody).build());

        result.close();

        verify(originalBody).close();
        verify(input, atLeastOnce()).close();
    }

    @ParameterizedTest
    @CsvSource({"null", "10"})
    void zeroLimitDoesNotReadBody(String length) throws IOException {
        logger = new FeignLogbookLogger(logbook, 0);
        final InputStream input = spy(new ByteArrayInputStream("secret-body".getBytes(UTF_8)));
        final Response original = response(input, length);

        try (Response result = log(original)) {
            assertThat(result).isSameAs(original);
            assertThat(input.available()).isEqualTo(11);
            verify(responseStage).process(loggedResponse.capture());
            assertThat(loggedResponse.getValue().withBody().getBody()).isEmpty();
        }
    }

    @Test
    void handlesMissingBody() throws IOException {
        try (Response result = log(response(null, "null"))) {
            assertThat(result.body()).isNull();
            verify(responseStage).process(loggedResponse.capture());
            assertThat(loggedResponse.getValue().withBody().getBody()).isEmpty();
        }
    }

    @Test
    void closesBodyWhenReadingFails() throws IOException {
        final InputStream input = mock(InputStream.class);
        when(input.read(any(byte[].class), anyInt(), anyInt()))
                .thenThrow(new IOException("read failed"));
        final Response original = response(input, "null");
        final Response.Body originalBody = spy(original.body());

        assertThatThrownBy(() -> log(original.toBuilder().body(originalBody).build()))
                .isInstanceOf(UncheckedIOException.class);

        verify(originalBody).close();
    }

    @Test
    void closesBodyWhenLoggingOverflowFails() throws IOException {
        final InputStream input = spy(new ByteArrayInputStream("secret-body".getBytes(UTF_8)));
        final Response original = response(input, "null");
        final Response.Body originalBody = spy(original.body());
        doThrow(new IOException("logging failed")).when(responseStage).process(any());

        assertThatThrownBy(() -> log(original.toBuilder().body(originalBody).build()))
                .isInstanceOf(UncheckedIOException.class);

        verify(originalBody).close();
    }

    @Test
    void rejectsNegativeLimit() {
        assertThatIllegalArgumentException().isThrownBy(() -> new FeignLogbookLogger(logbook, -1));
    }

    private Response response(InputStream input, String length) {
        return Response.builder()
                .status(200)
                .reason("OK")
                .request(request)
                .headers(
                        Collections.singletonMap(
                                "Content-Type",
                                Collections.singletonList("application/octet-stream")))
                .body(input, "null".equals(length) ? null : Integer.valueOf(length))
                .build();
    }

    private Response log(Response response) {
        logger.logRequest("download()", Level.FULL, request);
        return logger.logAndRebufferResponse("download()", Level.FULL, response, 0);
    }
}
