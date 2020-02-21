package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

final class HeaderFilterTest {

    @Test
    void noneShouldDefaultToNoOp() {
        final HeaderFilter unit = HeaderFilter.none();

        final HttpHeaders headers = HttpHeaders.of(
                "Authorization", "Bearer s3cr3t");
        assertThat(unit.filter(headers), is(sameInstance(headers)));
    }

}
