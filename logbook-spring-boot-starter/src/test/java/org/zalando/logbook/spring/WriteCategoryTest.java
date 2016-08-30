package org.zalando.logbook.spring;

import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.Assert.assertThat;

@TestPropertySource(properties = "logbook.write.category = http.wire-log")
public final class WriteCategoryTest extends AbstractTest {

    @Autowired
    private Logger logger;

    @Test
    public void shouldUseConfiguredCategory() throws IOException {
        assertThat(logger, hasFeature("name", Logger::getName, is("http.wire-log")));
    }

}
