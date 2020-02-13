package org.zalando.logbook.json;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.logbook.common.MediaTypeQuery;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonMediaTypeTest {

    static final Predicate<String> JSON = MediaTypeQuery.compile("application/json", "application/*+json");

    @ParameterizedTest
    @ValueSource(strings = { 
            "application/json",
            "application/abc+json;charset=utf-8",
            "application/json;charset=utf-8",
            "application/abc+json;charset=utf-8"
            })
    public void testJsonTypes(String mediaType) {
        assertTrue(JsonMediaType.JSON.test(mediaType));
        assertEquals(JsonMediaType.JSON.test(mediaType), JSON.test(mediaType));
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
    public void testNonJsonTypes(String mediaType) {
        assertFalse(JsonMediaType.JSON.test(mediaType));
        assertEquals(JsonMediaType.JSON.test(mediaType), JSON.test(mediaType));
    }

}
