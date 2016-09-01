package org.zalando.logbook;

import org.junit.Test;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;

public final class MockHttpMessageTest {

    private final MockHttpMessage unit = spy(MockHttpMessage.class);

    @Test
    public void shouldSelectNonEmptyMmap() {
        final Map<Object, Object> expected = singletonMap("foo", "bar");
        final Map<Object, Object> actual = unit.firstNonNullNorEmpty(expected, emptyMap());
        assertThat(actual, is(expected));
    }

}
