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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.MockRawHttpRequest;
import org.zalando.logbook.RawHttpRequest;

import java.io.IOException;
import java.util.function.Predicate;

import static java.util.Optional.empty;
import static java.util.regex.Pattern.compile;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.MockRawHttpRequest.builder;
import static org.zalando.logbook.RequestPredicates.exclude;
import static org.zalando.logbook.RequestPredicates.requestTo;

@ContextConfiguration
@TestPropertySource(properties = "spring.config.name = exclude")
public final class ExcludeTest extends AbstractTest {

    @Configuration
    public static class TestConfiguration {

        @Bean
        public Logger httpLogger() {
            final Logger logger = mock(Logger.class);
            when(logger.isTraceEnabled()).thenReturn(true);
            return logger;
        }
        
        @Bean
        public Predicate<RawHttpRequest> requestPredicate() {
            return exclude(requestTo("/health"));
        }

    }

    @Autowired
    private Logbook logbook;

    @Test
    public void shouldExcludeHealth() throws IOException {
        assertThat(logbook.write(request("/health")), is(empty()));
    }

    @Test
    public void shouldExcludeAdmin() throws IOException {
        assertThat(logbook.write(request("/admin")), is(empty()));
    }

    @Test
    public void shouldExcludeAdminWithPath() throws IOException {
        assertThat(logbook.write(request("/admin/users")), is(empty()));
    }

    @Test
    public void shouldNotExcludeAdminWithQueryParameters() throws IOException {
        assertThat(logbook.write(builder()
                .path("/admin")
                .query("debug=true")
                .build()), is(empty()));
    }

    @Test
    public void shouldNotExcludeApi() throws IOException {
        assertThat(logbook.write(request("/api")), is(not(empty())));
    }

    private MockRawHttpRequest request(final String path) {
        return builder()
                .path(path)
                .build();
    }

}