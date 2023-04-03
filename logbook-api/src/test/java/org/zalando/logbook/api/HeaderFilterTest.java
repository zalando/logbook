package org.zalando.logbook.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class HeaderFilterTest {

    @Test
    void noneShouldDefaultToNoOp() {
        final HeaderFilter unit = HeaderFilter.none();

        final HttpHeaders headers = HttpHeaders.of(
                "Authorization", "Bearer s3cr3t");

        assertThat(unit.filter(headers)).isSameAs(headers);
    }

}
