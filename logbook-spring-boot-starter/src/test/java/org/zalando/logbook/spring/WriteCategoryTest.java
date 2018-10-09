package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

@LogbookTest(properties = "logbook.write.category = http.wire-log")
class WriteCategoryTest {

    @Autowired
    private Logger logger;

    @Test
    void shouldUseConfiguredCategory() throws IOException {
        assertThat(logger, hasFeature("name", Logger::getName, is("http.wire-log")));
    }

}
