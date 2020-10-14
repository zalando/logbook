package org.zalando.logbook.jmh;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
public class TestDefaultCorrelation {

    @Test
    public void testGetters() {
        final DefaultPrecorrelation precorrelation = new DefaultPrecorrelation("id", null);
        assertThat(precorrelation.getId(), is("id"));
    }
}
