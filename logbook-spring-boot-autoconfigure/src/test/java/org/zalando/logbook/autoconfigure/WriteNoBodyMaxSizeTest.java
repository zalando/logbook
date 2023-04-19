package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.api.BodyFilter;

import static org.assertj.core.api.Assertions.assertThat;

@LogbookTest
class WriteNoBodyMaxSizeTest {

    @Autowired
    private BodyFilter bodyFilter;

    @Test
    void shouldNotUseBodyMaxSizeFilter() {
        final String body = "{\"open_id\":\"someLongSecret\",\"foo\":\"bar\"}";
        final String filtered = bodyFilter.filter("application/json", body);
        assertThat(filtered).isEqualTo("{\"open_id\":\"XXX\",\"foo\":\"bar\"}");
    }

}
