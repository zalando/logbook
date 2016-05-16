package org.zalando.logbook.spring;

/*
 * #%L
 * Logbook: Spring
 * %%
 * Copyright (C) 2015 - 2016 Zalando SE
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.web.SecurityFilterChain;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.servlet.LogbookFilter;
import org.zalando.logbook.servlet.Strategy;

import javax.servlet.Filter;

import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.ERROR;
import static javax.servlet.DispatcherType.REQUEST;

@Configuration
@ConditionalOnClass(SecurityFilterChain.class)
public class SecurityLogbookAutoConfiguration {

    public static final String UNAUTHORIZED = "unauthorizedLogbookFilter";

    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(name = "logbook.filter.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = UNAUTHORIZED)
    public FilterRegistrationBean unauthorizedLogbookFilter(final Logbook logbook) {
        final Filter filter = new LogbookFilter(logbook, Strategy.SECURITY);
        final FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setName(UNAUTHORIZED);
        registration.setDispatcherTypes(REQUEST, ASYNC, ERROR);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }

}
