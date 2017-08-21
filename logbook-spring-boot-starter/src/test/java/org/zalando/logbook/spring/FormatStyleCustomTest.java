package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockRawHttpRequest;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {Application.class, FormatStyleCustomTest.TestConfiguration.class})
public class FormatStyleCustomTest extends AbstractTest {

    @Configuration
    public static class TestConfiguration {

        @Bean
        public HttpLogFormatter formatter() {
            return mock(HttpLogFormatter.class);
        }

        @Bean
        public Logger httpLogger() {
            final Logger logger = mock(Logger.class);
            when(logger.isTraceEnabled()).thenReturn(true);
            return logger;
        }

    }

}
