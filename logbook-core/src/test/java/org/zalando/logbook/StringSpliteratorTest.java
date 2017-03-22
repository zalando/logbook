package org.zalando.logbook;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class StringSpliteratorTest {

    @Test
    public void shouldEstimateSizeWithoutTrailingPart() {
        assertThat(new StringSpliterator("Hello", 5).estimateSize(), is(1L));
    }

    @Test
    public void shouldEstimateSizeWithTrailingPart() {
        assertThat(new StringSpliterator("Hello World", 5).estimateSize(), is(3L));
    }

    @Test
    public void shouldNotSupportPartitions() {
        assertThat(new StringSpliterator("", 0).trySplit(), is(nullValue()));
    }
}
