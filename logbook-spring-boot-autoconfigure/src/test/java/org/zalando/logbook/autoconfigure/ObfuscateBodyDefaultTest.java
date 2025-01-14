package org.zalando.logbook.autoconfigure;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.zalando.logbook.BodyFilter;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@LogbookTest
class ObfuscateBodyDefaultTest {

    @Autowired
    @Qualifier("jsonBodyFieldsFilter")
    private BodyFilter jsonBodyFieldsFilter;

    @Test
    void shouldNotFilterJsonBodiesIfEmptyObfuscateJsonBodyFieldNames() throws IOException, JSONException {
        assertThat(jsonBodyFieldsFilter).isSameAs(BodyFilter.none());
    }

}
