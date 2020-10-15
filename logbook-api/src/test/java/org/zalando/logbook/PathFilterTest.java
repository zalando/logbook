package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class PathFilterTest {

    @Test
    void noneShouldDefaultToNoOp() {
        final PathFilter unit = PathFilter.none();

        assertThat(unit.filter("/a/b/c")).isEqualTo("/a/b/c");
    }

    @Test
    void merge() {
        final PathFilter unit = PathFilter.merge(a -> "a", b -> "b");

        assertThat(unit.filter("/a/b/c")).isEqualTo("a");
    }
    
}
