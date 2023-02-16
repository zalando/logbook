package org.zalando.logbook.servlet;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoteRequestTest {

    private final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    private final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    private final AsyncContext asyncContext = mock(AsyncContext.class);
    private final AsyncListener asyncListener = mock(AsyncListener.class);

    private final RemoteRequest remoteRequest = new RemoteRequest(httpServletRequest, FormRequestMode.OFF);

    @BeforeEach
    void setUp() {
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

}
