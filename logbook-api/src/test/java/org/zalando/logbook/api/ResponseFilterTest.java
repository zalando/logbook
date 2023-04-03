package org.zalando.logbook.api;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.api.HttpResponse;
import org.zalando.logbook.api.ResponseFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

final class ResponseFilterTest {

    @Test
    void noneShouldDefaultToNoOp() {
        final ResponseFilter unit = ResponseFilter.none();
        final HttpResponse response = mock(HttpResponse.class);

        assertThat(unit.filter(response)).isSameAs(response);
    }

}
