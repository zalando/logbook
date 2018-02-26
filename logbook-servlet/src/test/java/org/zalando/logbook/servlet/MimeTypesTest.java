package org.zalando.logbook.servlet;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MimeTypesTest {

    @Test
    void shouldFailToParse() {
        assertEquals(MimeTypes.parse(""), Optional.empty());
    }
}
