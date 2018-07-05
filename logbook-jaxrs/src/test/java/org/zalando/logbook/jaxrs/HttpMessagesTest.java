package org.zalando.logbook.jaxrs;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.zalando.logbook.jaxrs.HttpMessages.getPort;

public class HttpMessagesTest {

    @Test
    public void shouldReturnPort() throws Exception {
        final URI uri = new URI("http://localhost:99999");
        assertEquals(Optional.of(99999), getPort(uri));
    }

    @Test
    public void shouldReturnEmptyForAbsentPort() throws Exception {
        final URI uri = new URI("http://localhost");
        assertEquals(Optional.empty(), getPort(uri));
    }

}
