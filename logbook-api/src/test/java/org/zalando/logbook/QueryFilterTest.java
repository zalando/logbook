package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public final class QueryFilterTest {

    @Test
    void noneShouldDefaultToNoOp() {
        final QueryFilter unit = QueryFilter.none();

        assertThat(unit.filter("a=b&c=d&f=e"), is(equalTo("a=b&c=d&f=e")));
    }

}
