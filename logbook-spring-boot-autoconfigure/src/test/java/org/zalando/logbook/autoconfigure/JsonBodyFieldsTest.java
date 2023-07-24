package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.BodyFilter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@LogbookTest(properties = "logbook.obfuscate.json-body-fields = name,city")
class JsonBodyFieldsTest {

    @Autowired
    private List<BodyFilter> bodyFilters;

    private BodyFilter bodyFilter;

    @BeforeEach
    void setUp() {
        bodyFilter = bodyFilters.stream().reduce(BodyFilter.none(), BodyFilter::merge);
    }

    @Test
    void shouldObfuscateConfiguredJsonFields() {
        final String body = "{\"name\":\"John\", \"city\":\"Berlin\", \"color\":\"blue\"}";
        final String filtered = bodyFilter.filter("application/json", body);

        assertThat(filtered).isEqualTo("{\"name\":\"XXX\",\"city\":\"XXX\",\"color\":\"blue\"}");
    }

}
