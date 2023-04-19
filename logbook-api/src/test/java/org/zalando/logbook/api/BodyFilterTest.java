package org.zalando.logbook.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class BodyFilterTest {

    @Test
    void noneShouldDefaultToNoOp() {
        final BodyFilter unit = BodyFilter.none();

        assertThat(unit.filter("text/plain", "Hello, world!")).isEqualTo("Hello, world!");
    }

}
