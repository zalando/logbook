package org.zalando.logbook.servlet;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import static java.util.Collections.emptyEnumeration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoteRequestTest {

    private final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    private final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    private final AsyncContext asyncContext = mock(AsyncContext.class);
    private final AsyncListener asyncListener = mock(AsyncListener.class);

    private RemoteRequest remoteRequest;

    @BeforeEach
    void setUp() {
        when(httpServletRequest.getHeaderNames()).thenReturn(emptyEnumeration());
        remoteRequest = new RemoteRequest(httpServletRequest, FormRequestMode.OFF);
        remoteRequest.setAsyncListener(Optional.of(asyncListener));
    }

    @Test
    void startAsync_noargs() {
        when(httpServletRequest.startAsync()).thenReturn(asyncContext);

        assertEquals(asyncContext, remoteRequest.startAsync());
        verify(asyncContext).addListener(asyncListener);
    }

    @Test
    void startAsync_twoargs() {
        when(httpServletRequest.startAsync(httpServletRequest, httpServletResponse)).thenReturn(asyncContext);

        assertEquals(asyncContext, remoteRequest.startAsync(httpServletRequest, httpServletResponse));
        verify(asyncContext).addListener(asyncListener);
    }

    @Test
    void shouldThrow() {
        assertThrows(UnsupportedEncodingException.class, () -> RemoteRequest.encode("", "FOO"));
    }

    @Test
    void withBody_eagerlyBuffersBody() throws Exception {
        final byte[] bodyContent = "test body content".getBytes();
        final var inputStream = mock(jakarta.servlet.ServletInputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(httpServletRequest.getInputStream()).thenReturn(inputStream);
        when(httpServletRequest.getContentType()).thenReturn("application/json");

        remoteRequest.withBody();

        assertEquals(17, remoteRequest.getBody().length);
        final var secondInputStream = mock(jakarta.servlet.ServletInputStream.class);
        when(secondInputStream.read((byte[]) any())).thenThrow(new java.io.IOException("Stream closed"));
        when(httpServletRequest.getInputStream()).thenReturn(secondInputStream);

        assertEquals(17, remoteRequest.getBody().length);
    }

    @Test
    void withoutBody() throws Exception {
        when(httpServletRequest.getInputStream()).thenReturn(mock(jakarta.servlet.ServletInputStream.class));

        final var result = remoteRequest.withoutBody();

        assertEquals(remoteRequest, result);
        assertEquals(0, remoteRequest.getBody().length);
    }
}
