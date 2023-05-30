package org.zalando.logbook.internal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonMediaTypeTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "application/json",
            "application/abc+json;charset=utf-8",
            "application/json;charset=utf-8",
            "application/abc+json;charset=utf-8"
    })
    public void testJsonTypes(final String mediaType) {
        assertTrue(JsonMediaType.JSON.test(mediaType));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "application/notjson",
            "application/abc+notjson;charset=utf-8",
            "application/notjson;charset=utf-8",
            "application/abc+notjson;charset=utf-8",
            "text/json",
            "text/abc+json;charset=utf-8",
            "text/json;charset=utf-8",
            "text/abc+json;charset=utf-8",
            "image/json",
            "image/abc+json;charset=utf-8",
            "image/json;charset=utf-8",
            "image/abc+json;charset=utf-8"
    })
    @NullSource
    public void testNonJsonTypes(final String mediaType) {
        assertFalse(JsonMediaType.JSON.test(mediaType));
    }

}
