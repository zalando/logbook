package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zalando.logbook.MediaTypeQuery.compile;

public final class MediaTypeQueryTest {

    @Test
    void shouldMatchAllMatch() {
        deny("*/*", null);
        deny("*/*", "");
        allow("*/*", "text/plain");
        allow("text/*", "text/plain");
        allow("text/plain", "text/plain");
        allow("text/plain", "text/plain;charset=UTF-8");
        allow("text/plain;charset=UTF-8", "text/plain"); // TODO should deny
    }

    private void allow(final String pattern, @Nullable final String mediaType) {
        assertThat(pattern + " doesn't match " + mediaType, compile(pattern).test(mediaType), is(true));
    }

    private void deny(final String pattern, @Nullable final String mediaType) {
        assertThat(pattern + " matches " + mediaType + " but shouldn't", compile(pattern).test(mediaType), is(false));
    }

}
