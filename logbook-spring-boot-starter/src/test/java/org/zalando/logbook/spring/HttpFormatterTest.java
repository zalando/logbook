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

import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;
import java.util.function.Function;

import static org.hamcrest.Matchers.containsString;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@ContextConfiguration
@TestPropertySource(properties = "logbook.format.style = http")
public final class HttpFormatterTest extends AbstractTest {

    @Configuration
    public static class TestConfiguration {

        @Bean
        public HttpLogWriter writer() {
            return mock(HttpLogWriter.class);
        }

    }

    @Autowired
    private Logbook logbook;

    @Autowired
    private HttpLogWriter writer;

    @Test
    public void shouldUseHttpFormatter() throws IOException {
        when(writer.isActive(any())).thenReturn(true);

        logbook.write(MockRawHttpRequest.create());

        verify(writer).writeRequest(argThat(isHttpFormatted()));
    }

    private Matcher<Precorrelation<String>> isHttpFormatted() {
        final Function<Precorrelation<String>, String> getRequest = Precorrelation::getRequest;
        return hasFeature("request", getRequest, containsString("GET / HTTP/1.1"));
    }

}
