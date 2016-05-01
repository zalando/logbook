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
import org.springframework.test.context.TestPropertySource;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockRawHttpRequest;

import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ContextConfiguration
@TestPropertySource(properties = "logbook.write.level = WARN")
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
