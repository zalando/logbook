package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.BodyFilter;

import static org.assertj.core.api.Assertions.assertThat;

class TruncatingBodyFilterTest {

    @Test
    void shouldNotTruncateIfMaxLengthSetToLessThanZero() {
        final BodyFilter unit = new TruncatingBodyFilter(-1);


        final String resul = unit.filter("application/json", "{\"foo\":\"secret\"}");

        assertThat(resul).isEqualTo("{\"foo\":\"secret\"}");
    }

    @Test
    void shouldTruncateBodyIfTooLong() {
        final BodyFilter unit = new TruncatingBodyFilter(5);

        final String actual = unit.filter("application/json", "{\"foo\":\"secret\"}");

        assertThat(actual).isEqualTo("{\"foo...");
    }

    @Test
    void shouldNotTruncateBodyIfTooShort() {
        final BodyFilter unit = new TruncatingBodyFilter(50);

        final String actual = unit.filter("application/json", "{\"foo\":\"secret\"}");

        assertThat(actual).isEqualTo("{\"foo\":\"secret\"}");
    }
}
