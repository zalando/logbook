package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

final class PathFilterTest {

    @Test
    void noneShouldDefaultToNoOp() {
        final PathFilter unit = PathFilter.none();

        assertThat(unit.filter("/a/b/c"), is(equalTo("/a/b/c")));
    }

    @Test
    void merge() {
        final PathFilter unit = PathFilter.merge(a -> "a", b -> "b");

        assertThat(unit.filter("/a/b/c"), is(equalTo("a")));
    }
    
}
