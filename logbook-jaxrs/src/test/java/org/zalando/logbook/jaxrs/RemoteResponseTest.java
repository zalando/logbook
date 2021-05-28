package org.zalando.logbook.jaxrs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.ws.rs.client.ClientResponseContext;
import org.junit.jupiter.api.Test;

class RemoteResponseTest {

    @Test
    void shouldCloseOriginalResponseContext() {
        ClientResponseContext context = mock(ClientResponseContext.class);
        final AtomicBoolean isStreamClosed = new AtomicBoolean();
        InputStream entityStream = new ByteArrayInputStream("response-body".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public void close() throws IOException {
                super.close();
                isStreamClosed.getAndSet(true);
            }
        };
        when(context.getEntityStream()).thenReturn(entityStream);

        RemoteResponse remoteResponse = new RemoteResponse(context);
        ((RemoteResponse) remoteResponse.withBody()).expose();

        assertTrue(isStreamClosed.get());
    }

}