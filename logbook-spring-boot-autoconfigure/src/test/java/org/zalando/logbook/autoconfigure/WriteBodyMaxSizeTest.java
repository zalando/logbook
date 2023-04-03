package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.api.BodyFilter;

import static org.assertj.core.api.Assertions.assertThat;

@LogbookTest(properties = "logbook.write.max-body-size = 20")
class WriteBodyMaxSizeTest {

    @Autowired
    private BodyFilter bodyFilter;

    @Test
    void shouldUseBodyMaxSizeFilter() {
        final String body = "{\"foo\":\"someLongMessage\"}";
        final String filtered = bodyFilter.filter("application/json", body);

        assertThat(filtered).isEqualTo("{\"foo\":\"someLongMess...");
    }

    @Test
    void shouldUseBodyMaxSizeOverDefaultFilter() {
        final String body = "{\"open_id\":\"someLongSecret\",\"foo\":\"bar\"}";
        final String filtered = bodyFilter.filter("application/json", body);

        assertThat(filtered).isEqualTo("{\"open_id\":\"XXX\",\"fo...");
    }
}
