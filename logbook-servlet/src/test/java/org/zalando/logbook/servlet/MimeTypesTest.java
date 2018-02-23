package org.zalando.logbook.servlet;

import org.junit.jupiter.api.Test;

import javax.activation.MimeTypeParseException;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class MimeTypesTest {

    @Test
    void shouldFailToParse() {
        assertThrows(MimeTypeParseException.class, () -> MimeTypes.parse(""));
    }
}
