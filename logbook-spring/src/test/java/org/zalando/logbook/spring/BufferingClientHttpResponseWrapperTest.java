package org.zalando.logbook.spring;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BufferingClientHttpResponseWrapperTest {

    @Mock
    private ClientHttpResponse delegate;

    @Mock
    private InputStream inputStream;

    private BufferingClientHttpResponseWrapper wrapper;

    @BeforeEach
    void setUp() throws IOException {
        when(delegate.getBody()).thenReturn(inputStream);
        wrapper = new BufferingClientHttpResponseWrapper(delegate);
    }

    @Test
    void wrapBodyInBufferedInputStreamWhenMarkNotSupported() throws IOException {
        when(inputStream.markSupported()).thenReturn(false);

        assertTrue(new BufferingClientHttpResponseWrapper(delegate).getBody() instanceof BufferedInputStream);
    }

    @Test
    void dontWrapBodyInBufferedInputStreamWhenMarkSupported() throws IOException {
        when(inputStream.markSupported()).thenReturn(true);

        assertEquals(inputStream, new BufferingClientHttpResponseWrapper(delegate).getBody());
    }

    @Test
    void getStatusCode() throws IOException {
        when(delegate.getStatusCode()).thenReturn(HttpStatus.OK);

        assertEquals(HttpStatus.OK, wrapper.getStatusCode());
    }

    @Test
    void getRawStatusCode() throws IOException {
        when(delegate.getRawStatusCode()).thenReturn(200);

        assertEquals(200, wrapper.getRawStatusCode());
    }

    @Test
    void getStatusText() throws IOException {
        when(delegate.getStatusText()).thenReturn("OK");

        assertEquals("OK", wrapper.getStatusText());
    }

    @Test
    void close() {
        wrapper.close();
        verify(delegate).close();
    }

    @Test
    void close_throws() throws IOException {
        doThrow(new IOException()).when(inputStream).close();

        assertThrows(RuntimeException.class, () -> wrapper.close());
    }

    @Test
    void getBody() {
        assertTrue(wrapper.getBody().markSupported());
    }

    @Test
    void getHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        when(delegate.getHeaders()).thenReturn(httpHeaders);

        assertEquals(httpHeaders, wrapper.getHeaders());
    }
}