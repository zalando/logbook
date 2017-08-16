package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

@TestPropertySource(properties = "logbook.write.category = http.wire-log")
public final class WriteCategoryTest extends AbstractTest {

    @Autowired
    private Logger logger;

    @Test
    void shouldUseConfiguredCategory() throws IOException {
        assertThat(logger, hasFeature("name", Logger::getName, is("http.wire-log")));
    }

}
