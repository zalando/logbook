package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.BodyFilter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@LogbookTest
class WriteNoBodyMaxSizeTest {

    @Autowired
    private BodyFilter bodyFilter;

    @Test
    void shouldNotUseBodyMaxSizeFilter() {
        assertThat(bodyFilter.filter("application/json", "{\"open_id\":\"someLongSecret\",\"foo\":\"bar\"}"),
                is("{\"open_id\":\"XXX\",\"foo\":\"bar\"}"));
    }
}
