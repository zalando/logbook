package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.util.unit.DataSize;

import static org.assertj.core.api.Assertions.assertThat;

@LogbookTest(properties = "logbook.filter.max-buffer-size = 1MB")
class FilterMaxBufferSizeTest {

    @Autowired
    private LogbookProperties properties;

    @Autowired
    @Qualifier("logbookFilter")
    private FilterRegistrationBean logbookFilter;

    @Autowired
    @Qualifier("secureLogbookFilter")
    private FilterRegistrationBean secureLogbookFilter;

    @Test
    void shouldBindMaxBufferSizeProperty() {
        assertThat(properties.getFilter().getMaxBufferSize()).isEqualTo(DataSize.ofMegabytes(1));
        assertThat(logbookFilter).isNotNull();
        assertThat(secureLogbookFilter).isNotNull();
    }

}
