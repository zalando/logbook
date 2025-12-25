package org.zalando.logbook.jaxrs;

import jakarta.ws.rs.container.ContainerRequestContext;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoteRequestTest {

    @Test
    void withBody_eagerlyBuffersBody() throws Exception {
        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        final byte[] bodyContent = "test body content".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(context.getEntityStream()).thenReturn(inputStream);
        when(context.getMediaType()).thenReturn(null);

        RemoteRequest remoteRequest = new RemoteRequest(context);
        remoteRequest.withBody();

        assertEquals(17, remoteRequest.getBody().length);
        final InputStream closedStream = mock(InputStream.class);
        when(closedStream.read((byte[]) any())).thenThrow(new IOException("Stream closed"));
        when(context.getEntityStream()).thenReturn(closedStream);

        assertEquals(17, remoteRequest.getBody().length);

        verify(context).setEntityStream(any(ByteArrayInputStream.class));
    }

    @Test
    void withoutBody() {
        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getEntityStream()).thenReturn(mock(InputStream.class));

        RemoteRequest remoteRequest = new RemoteRequest(context);
        final var result = remoteRequest.withoutBody();

        assertEquals(remoteRequest, result);
        assertEquals(0, remoteRequest.getBody().length);
    }

    @Test
    void withBody_thenWithoutBody() throws Exception {
        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        final byte[] bodyContent = "test body content".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(context.getEntityStream()).thenReturn(inputStream);
        when(context.getMediaType()).thenReturn(null);

        RemoteRequest remoteRequest = new RemoteRequest(context);
        remoteRequest.withBody();
        assertEquals(17, remoteRequest.getBody().length);

        final var result = remoteRequest.withoutBody();
        assertEquals(remoteRequest, result);
        assertEquals(0, remoteRequest.getBody().length);
    }

    @Test
    void offered_withoutBody() throws Exception {
        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        final byte[] bodyContent = "data".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(context.getEntityStream()).thenReturn(inputStream);
        when(context.getMediaType()).thenReturn(null);

        RemoteRequest remoteRequest = new RemoteRequest(context);
        remoteRequest.withBody();
        byte[] body = remoteRequest.getBody();
        assertEquals(4, body.length);

        remoteRequest.withoutBody();
        assertEquals(0, remoteRequest.getBody().length);

        remoteRequest.withBody();
        assertEquals(4, remoteRequest.getBody().length);
    }

    @Test
    void unbuffered_getBody_returnsEmpty() throws Exception {
        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        final byte[] bodyContent = "direct".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(context.getEntityStream()).thenReturn(inputStream);
        when(context.getMediaType()).thenReturn(null);

        RemoteRequest remoteRequest = new RemoteRequest(context);
        // In jaxrs, getBody() without withBody() returns empty array (doesn't auto-buffer)
        byte[] body = remoteRequest.getBody();
        assertEquals(0, body.length);
    }

    @Test
    void offered_withBody_twice() throws Exception {
        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        final byte[] bodyContent = "data".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(context.getEntityStream()).thenReturn(inputStream);
        when(context.getMediaType()).thenReturn(null);

        RemoteRequest remoteRequest = new RemoteRequest(context);
        remoteRequest.withBody();
        byte[] body = remoteRequest.getBody();
        assertEquals(4, body.length);

        remoteRequest.withBody();
        assertEquals(4, remoteRequest.getBody().length);
    }

    @Test
    void offered_without_beforeBuffer() throws Exception {
        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        final byte[] bodyContent = "data".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(context.getEntityStream()).thenReturn(inputStream);
        when(context.getMediaType()).thenReturn(null);

        RemoteRequest remoteRequest = new RemoteRequest(context);
        remoteRequest.withBody();  // Unbuffered -> Offered

        // Call withoutBody BEFORE getBody, so it's called on Offered state itself
        remoteRequest.withoutBody();  // Offered.without() -> Withouted

        // Now getBody should return empty since we're in Withouted state
        assertEquals(0, remoteRequest.getBody().length);
    }

    @Test
    void withoutBody_thenWithBody_stateTransitions() throws Exception {
        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        final byte[] bodyContent = "new body".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(context.getEntityStream()).thenReturn(inputStream);
        when(context.getMediaType()).thenReturn(null);

        RemoteRequest remoteRequest = new RemoteRequest(context);
        // Start in Unbuffered, transition to Withouted via withoutBody
        remoteRequest.withoutBody();

        // Then transition back to Offered via withBody
        remoteRequest.withBody();

        // getBody should now trigger buffering from Offered
        assertEquals(8, remoteRequest.getBody().length);
    }

    @Test
    void offered_without_returnsWithouted() throws Exception {
        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        final byte[] bodyContent = "test".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(context.getEntityStream()).thenReturn(inputStream);
        when(context.getMediaType()).thenReturn(null);

        RemoteRequest remoteRequest = new RemoteRequest(context);
        remoteRequest.withBody();  // Unbuffered -> Offered
        assertEquals(4, remoteRequest.getBody().length);

        remoteRequest.withoutBody();  // Offered -> Withouted (via Buffering -> Ignoring)
        assertEquals(0, remoteRequest.getBody().length);
    }

    @Test
    void unbuffered_withoutBody_thenWithBody() throws Exception {
        final ContainerRequestContext context = mock(ContainerRequestContext.class);
        final byte[] bodyContent = "test".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(context.getEntityStream()).thenReturn(inputStream);
        when(context.getMediaType()).thenReturn(null);

        RemoteRequest remoteRequest = new RemoteRequest(context);
        // Start Unbuffered, transition to Withouted via withoutBody
        remoteRequest.withoutBody();
        assertEquals(0, remoteRequest.getBody().length);

        // Then back to Offered via withBody, then buffer
        remoteRequest.withBody();
        assertEquals(4, remoteRequest.getBody().length);
    }

}
