package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public final class BodyFilterTest {

    @Test
    void noneShouldDefaultToNoOp() {
        final BodyFilter unit = BodyFilter.none();

        assertThat(unit.filter("text/plain", "Hello, world!"), is(equalTo("Hello, world!")));
    }

}
