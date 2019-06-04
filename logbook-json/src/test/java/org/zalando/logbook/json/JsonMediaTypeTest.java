package org.zalando.logbook.json;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class JsonMediaTypeTest {

    @Test
    public void testApplicationJson1() {
        assertTrue(JsonMediaType.JSON.test("application/json"));
    }
    
    @Test
    public void testApplicationJson2() {
        assertTrue(JsonMediaType.JSON.test("application/abc+json;charset=utf-8"));
    }

    @Test
    public void testApplicationJson1WithCharset() {
        assertTrue(JsonMediaType.JSON.test("application/json;charset=utf-8"));
    }
    
    @Test
    public void testApplicationJson2WithCharset() {
        assertTrue(JsonMediaType.JSON.test("application/abc+json;charset=utf-8"));
    }
}
