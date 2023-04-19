package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.api.Logbook;

import static org.assertj.core.api.Assertions.assertThat;

@LogbookTest
class StandardTest {

    @Autowired
    private Logbook logbook;

    @Test
    void shouldBeAutowired() {
        assertThat(logbook).isNotNull();
    }

}
