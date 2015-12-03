package org.zalando.logbook.spring;

/*
 * #%L
 * Logbook: Spring
 * %%
 * Copyright (C) 2015 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration
public class CustomFormatterTest extends AbstractTest {

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

    @Autowired
    private Logbook logbook;

    @Autowired
    private HttpLogFormatter formatter;

    @Test
    public void shouldUseCustomFormatter() throws IOException {
        logbook.write(MockRawHttpRequest.create());

        verify(formatter).format(anyPrecorrelation());
    }

    @SuppressWarnings("unchecked")
    private <T> Precorrelation<T> anyPrecorrelation() {
        return any(Precorrelation.class);
    }

}