package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class QueryFilterTest {

    @Test
    void noneShouldDefaultToNoOp() {
        final QueryFilter unit = QueryFilter.none();

        assertThat(unit.filter("a=b&c=d&f=e")).isEqualTo("a=b&c=d&f=e");
    }

}
