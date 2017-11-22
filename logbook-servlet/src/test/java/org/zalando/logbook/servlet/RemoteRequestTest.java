package org.zalando.logbook.servlet;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RemoteRequestTest {

    @Test
    void shouldThrow() {
        assertThrows(UnsupportedEncodingException.class, () -> RemoteRequest.encode("", "FOO"));
    }

}
