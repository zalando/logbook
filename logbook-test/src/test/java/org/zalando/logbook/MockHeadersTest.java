package org.zalando.logbook;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class MockHeadersTest {

    @Test
    public void satisfyCoverage() {
        new MockHeaders();
    }

    @Test
    public void testOf1() {
        final Map<String, List<String>> m = MockHeaders.of("x", "y");

        assertEquals(1, m.size());
        assertEquals(m.get("x"), singletonList("y"));
    }

    @Test
    public void testOf2() {
        final Map<String, List<String>> m = MockHeaders.of("x", "y", "a", "b");

        assertEquals(2, m.size());
        assertEquals(m.get("x"), singletonList("y"));
        assertEquals(m.get("a"), singletonList("b"));
    }

    @Test
    public void testOf3() {
        final Map<String, List<String>> m = MockHeaders.of("x", "y", "a", "b", "1", "2");

        assertEquals(3, m.size());
        assertEquals(m.get("x"), singletonList("y"));
        assertEquals(m.get("a"), singletonList("b"));
        assertEquals(m.get("1"), singletonList("2"));
    }
}
