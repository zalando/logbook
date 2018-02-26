package org.zalando.logbook.servlet;

import org.junit.jupiter.api.Test;

import javax.activation.MimeType;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MimeTypesTest {

    @Test
    void shouldParse() {
        final MimeType mimeType = MimeTypes.parse("text/plain; charset=UTF-8").orElseThrow(AssertionError::new);

        assertEquals("text", mimeType.getPrimaryType());
        assertEquals("plain", mimeType.getSubType());
        assertEquals("UTF-8", mimeType.getParameter("charset"));
    }

    @Test
    void shouldFailToParse() {
        assertEquals(Optional.empty(), MimeTypes.parse(""));
    }

}
