package org.zalando.logbook.servlet;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    void withBody_thenWithoutBody() throws Exception {
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

        final var result = remoteRequest.withoutBody();
        assertEquals(remoteRequest, result);
        assertEquals(0, remoteRequest.getBody().length);
    }

    @Test
    void offered_withoutBody() throws Exception {
        final byte[] bodyContent = "data".getBytes();
        final var inputStream = mock(jakarta.servlet.ServletInputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(httpServletRequest.getInputStream()).thenReturn(inputStream);
        when(httpServletRequest.getContentType()).thenReturn("application/json");

        remoteRequest.withBody();
        byte[] body = remoteRequest.getBody();
        assertEquals(4, body.length);

        remoteRequest.withoutBody();
        assertEquals(0, remoteRequest.getBody().length);

        remoteRequest.withBody();
        assertEquals(4, remoteRequest.getBody().length);
    }

    @Test
    void unbuffered_buffer_viaGetBody() throws Exception {
        final byte[] bodyContent = "direct".getBytes();
        final var inputStream = mock(jakarta.servlet.ServletInputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(httpServletRequest.getInputStream()).thenReturn(inputStream);
        when(httpServletRequest.getContentType()).thenReturn("application/json");

        byte[] body = remoteRequest.getBody();
        assertEquals(6, body.length);
    }

    @Test
    void offered_withBody_twice() throws Exception {
        final byte[] bodyContent = "data".getBytes();
        final var inputStream = mock(jakarta.servlet.ServletInputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(httpServletRequest.getInputStream()).thenReturn(inputStream);
        when(httpServletRequest.getContentType()).thenReturn("application/json");

        remoteRequest.withBody();
        byte[] body = remoteRequest.getBody();
        assertEquals(4, body.length);

        remoteRequest.withBody();
        assertEquals(4, remoteRequest.getBody().length);
    }

    @Test
    void offered_without_thenGetInputStream() throws Exception {
        final byte[] bodyContent = "test".getBytes();
        final var inputStream = mock(jakarta.servlet.ServletInputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(httpServletRequest.getInputStream()).thenReturn(inputStream);
        when(httpServletRequest.getContentType()).thenReturn("application/json");

        remoteRequest.withBody();
        assertEquals(4, remoteRequest.getBody().length);

        jakarta.servlet.ServletInputStream stream = remoteRequest.getInputStream();
        assertEquals(4, remoteRequest.getBody().length);
    }

    @Test
    void ignoring_getInputStream_returnEmpty() throws Exception {
        final byte[] bodyContent = "test body".getBytes();
        final var inputStream = mock(jakarta.servlet.ServletInputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(httpServletRequest.getInputStream()).thenReturn(inputStream);
        when(httpServletRequest.getContentType()).thenReturn("application/json");

        remoteRequest.withBody();
        assertEquals(9, remoteRequest.getBody().length);

        remoteRequest.withoutBody();

        jakarta.servlet.ServletInputStream stream = remoteRequest.getInputStream();
        byte[] streamData = new byte[10];
        int bytesRead = stream.read(streamData);
        assertEquals(-1, bytesRead);
    }

    @Test
    void withoutBody_thenWithBody_stateTransitions() throws Exception {
        final byte[] bodyContent = "new body".getBytes();
        final var inputStream = mock(jakarta.servlet.ServletInputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(httpServletRequest.getInputStream()).thenReturn(inputStream);
        when(httpServletRequest.getContentType()).thenReturn("application/json");

        // Start in Unbuffered, transition to Withouted via withoutBody
        remoteRequest.withoutBody();

        // Then transition back to Offered via withBody
        remoteRequest.withBody();

        // getBody should now trigger buffering from Offered
        assertEquals(8, remoteRequest.getBody().length);
    }

    @Test
    void offered_without_returnsWithouted() throws Exception {
        final byte[] bodyContent = "test".getBytes();
        final var inputStream = mock(jakarta.servlet.ServletInputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(httpServletRequest.getInputStream()).thenReturn(inputStream);
        when(httpServletRequest.getContentType()).thenReturn("application/json");

        remoteRequest.withBody();  // Unbuffered -> Offered
        assertEquals(4, remoteRequest.getBody().length);

        remoteRequest.withoutBody();  // Offered -> Withouted (via Buffering -> Ignoring)
        assertEquals(0, remoteRequest.getBody().length);
    }

    @Test
    void unbuffered_withoutBody_thenWithBody() throws Exception {
        final byte[] bodyContent = "test".getBytes();
        final var inputStream = mock(jakarta.servlet.ServletInputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(httpServletRequest.getInputStream()).thenReturn(inputStream);
        when(httpServletRequest.getContentType()).thenReturn("application/json");

        // Start Unbuffered, transition to Withouted via withoutBody
        remoteRequest.withoutBody();
        assertEquals(0, remoteRequest.getBody().length);

        // Then back to Offered via withBody, then buffer
        remoteRequest.withBody();
        assertEquals(4, remoteRequest.getBody().length);
    }

    @Test
    void offered_without_beforeBuffer() throws Exception {
        final byte[] bodyContent = "data".getBytes();
        final var inputStream = mock(jakarta.servlet.ServletInputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(httpServletRequest.getInputStream()).thenReturn(inputStream);
        when(httpServletRequest.getContentType()).thenReturn("application/json");

        remoteRequest.withBody();  // Unbuffered -> Offered

        // Call withoutBody BEFORE getBody, so it's called on Offered state itself
        remoteRequest.withoutBody();  // Offered.without() -> Withouted

        // Now getBody should return empty since we're in Withouted state
        assertEquals(0, remoteRequest.getBody().length);
    }

    @Test
    void streaming_getInputStream() throws Exception {
        final byte[] bodyContent = "streaming test".getBytes();
        final var inputStream = mock(jakarta.servlet.ServletInputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(httpServletRequest.getInputStream()).thenReturn(inputStream);
        when(httpServletRequest.getContentType()).thenReturn("application/json");

        remoteRequest.withBody();
        remoteRequest.getInputStream();
        assertEquals(14, remoteRequest.getBody().length);
    }

    @Test
    void multipart_withBody_doesNotBuffer() throws Exception {
        final byte[] bodyContent = "multipart form data".getBytes();
        final var inputStream = mock(jakarta.servlet.ServletInputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(httpServletRequest.getInputStream()).thenReturn(inputStream);
        when(httpServletRequest.getContentType()).thenReturn("multipart/form-data");

        remoteRequest.withBody();

        assertEquals(0, remoteRequest.getBody().length);
    }

    @Test
    void multipart_mixed_doesNotBuffer() throws Exception {
        final byte[] bodyContent = "mixed multipart content".getBytes();
        final var inputStream = mock(jakarta.servlet.ServletInputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(httpServletRequest.getInputStream()).thenReturn(inputStream);
        when(httpServletRequest.getContentType()).thenReturn("multipart/mixed");

        remoteRequest.withBody();

        assertEquals(0, remoteRequest.getBody().length);
    }

}
