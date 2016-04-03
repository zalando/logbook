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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zalando.logbook.servlet.LogbookFilter;

import javax.servlet.Filter;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@WebAppConfiguration
public class SecurityFilterTest extends AbstractTest {

    @Autowired
    @Qualifier("unauthorizedLogbookFilter")
    private FilterRegistrationBean unauthorizedLogbookFilter;

    @Autowired
    @Qualifier("authorizedLogbookFilter")
    private FilterRegistrationBean authorizedLogbookFilter;

    @Test
    public void shouldInitializeFilters() {
        assertThat(unauthorizedLogbookFilter, is(notNullValue()));
        assertThat(authorizedLogbookFilter, is(notNullValue()));
    }

}
