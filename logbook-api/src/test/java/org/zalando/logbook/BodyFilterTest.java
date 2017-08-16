package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class BodyFilterTest {

    @Test
    public void noneShouldDefaultToNoOp() {
        final BodyFilter unit = BodyFilter.none();

        assertThat(unit.filter("text/plain", "Hello, world!"), is(equalTo("Hello, world!")));
    }

}
