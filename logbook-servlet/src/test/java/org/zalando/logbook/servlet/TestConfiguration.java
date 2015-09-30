package org.zalando.logbook.servlet;

/*
 * #%L
 * logbook
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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@Configuration
@EnableWebMvc
@Import(ExampleController.class)
public class TestConfiguration {

    @Bean
    public MockMvc mockMvc(final WebApplicationContext context, final LogbookFilter filter) {
        return MockMvcBuilders
                .webAppContextSetup(context)
                .addFilter(filter)
                .build();
    }

    @Bean
    public LogbookFilter filter(final Logbook logbook) {
        return new LogbookFilter(logbook);
    }

    @Bean
    public Logbook logbook(final HttpLogFormatter formatter, final HttpLogWriter writer) {
        return Logbook.builder()
                .withFormatter(formatter)
                .withWriter(writer)
                .build();
    }

    @Bean
    public HttpLogFormatter httpLogFormatter() {
        // otherwise we would need to make DefaultHttpLogFormatter non-final
        return spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    }

    @Bean
    public HttpLogWriter httpLogWriter() {
        return mock(HttpLogWriter.class);
    }

}
