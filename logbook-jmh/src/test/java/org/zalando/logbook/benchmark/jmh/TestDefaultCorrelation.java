package org.zalando.logbook.benchmark.jmh;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDefaultCorrelation {

    @Test
    public void testGetters() {
        final DefaultPrecorrelation precorrelation = new DefaultPrecorrelation("id", null);
        assertThat(precorrelation.getId()).isEqualTo("id");
    }

}
