package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockRawHttpRequest;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        classes = {Application.class, WriteLevelTest.TestConfiguration.class},
        properties = "logbook.write.level = WARN")
public class WriteLevelTest extends AbstractTest {

    @Configuration
    public static class TestConfiguration {

        @Bean
        public Logger httpLogger() {
            final Logger logger = spy(LoggerFactory.getLogger(Logbook.class));
            doReturn(true).when(logger).isWarnEnabled();
            return logger;
        }

    }

    @Autowired
    private Logbook logbook;

    @Autowired
    private Logger logger;

    @Test
    public void shouldUseConfiguredLevel() throws IOException {
        logbook.write(MockRawHttpRequest.create());

        verify(logger).warn(anyString());
    }

}
