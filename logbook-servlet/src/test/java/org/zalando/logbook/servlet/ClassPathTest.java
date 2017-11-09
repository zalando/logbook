package org.zalando.logbook.servlet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.zalando.logbook.servlet.ClassPath.load;

class ClassPathTest {

    @Test
    void shouldLoadIfPresent() {
        assertEquals("yes", load("java.lang.String", () -> "yes", () -> "no"));
    }

    @Test
    void shouldLoadIfAbsent() {
        assertEquals("no", load("java.lang.Unknown", () -> "yes", () -> "no"));
    }

}
