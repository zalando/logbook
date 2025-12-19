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

}

