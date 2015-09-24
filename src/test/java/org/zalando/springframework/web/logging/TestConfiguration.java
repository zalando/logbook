package org.zalando.springframework.web.logging;

/*
 * #%L
 * spring-web-logging
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

import static org.mockito.Mockito.mock;

@Configuration
@EnableWebMvc
@Import(ExampleController.class)
public class TestConfiguration {

    @Bean
    public MockMvc mockMvc(final WebApplicationContext context, final LoggingFilter filter) {
        return MockMvcBuilders
                .webAppContextSetup(context)
                .addFilter(filter)
                .build();
    }

    @Bean
    public LoggingFilter loggingFilter(final HttpLogger httpLogger) {
        return new LoggingFilter(httpLogger);
    }

    @Bean
    public HttpLogger httpLogger() {
        return mock(HttpLogger.class);
    }

}
