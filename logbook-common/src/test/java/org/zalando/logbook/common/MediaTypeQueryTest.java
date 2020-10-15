package org.zalando.logbook.common;

import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.common.MediaTypeQuery.compile;

final class MediaTypeQueryTest {

    @Test
    void shouldMatchAllMatch() {
        deny("*/*", null);
        deny("*/*", "");
        deny("text/*", "application/json");
        deny("text/plain", "application/json");
        allow("*/*", "text/plain");
        allow("text/*", "text/plain");
        allow("*/plain", "text/plain");
        allow("text/plain", "text/plain");
        allow("text/plain", "text/plain;charset=UTF-8");
        allow("text/plain;charset=UTF-8", "text/plain"); // TODO should deny
    }

    private void allow(final String pattern, @Nullable final String mediaType) {
        assertThat(compile(pattern).test(mediaType))
                .as("Media type query %s match media type %s", pattern, mediaType)
                .isTrue();
    }

    private void deny(final String pattern, @Nullable final String mediaType) {
        assertThat(compile(pattern).test(mediaType))
                .as("Media type query %s match media type %s", pattern, mediaType)
                .isFalse();
    }

}
