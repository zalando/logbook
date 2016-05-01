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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.zalando.logbook.BodyObfuscator;
import org.zalando.logbook.Conditions;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.DefaultHttpLogWriter;
import org.zalando.logbook.DefaultHttpLogWriter.Level;
import org.zalando.logbook.HeaderObfuscator;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.JsonHttpLogFormatter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Obfuscators;
import org.zalando.logbook.QueryObfuscator;
import org.zalando.logbook.RawHttpRequest;
import org.zalando.logbook.servlet.LogbookFilter;

import javax.servlet.Filter;
import java.util.List;
import java.util.function.Predicate;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.stream.Collectors.toList;
import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.ERROR;
import static javax.servlet.DispatcherType.REQUEST;

@Configuration
@ConditionalOnClass(Logbook.class)
@EnableConfigurationProperties(LogbookProperties.class)
@AutoConfigureAfter({WebMvcAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@Import(SecurityLogbookAutoConfiguration.class)
public class LogbookAutoConfiguration {

    public static final String AUTHORIZED = "authorizedLogbookFilter";

    @Autowired
    // IDEA doesn't support @EnableConfigurationProperties
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private LogbookProperties properties;

    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(name = "logbook.filter.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = AUTHORIZED)
    public FilterRegistrationBean authorizedLogbookFilter(final Logbook logbook) {
        final Filter filter = new LogbookFilter(logbook);
        final FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setName(AUTHORIZED);
        registration.setDispatcherTypes(REQUEST, ASYNC, ERROR);
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(Logbook.class)
    public Logbook logbook(
            final Predicate<RawHttpRequest> condition,
            final HeaderObfuscator headerObfuscator,
            final QueryObfuscator queryObfuscator,
            final BodyObfuscator bodyObfuscator,
            @SuppressWarnings("SpringJavaAutowiringInspection") final HttpLogFormatter formatter,
            final HttpLogWriter writer
    ) {
        return Logbook.builder()
                .condition(mergeWithExcludes(condition))
                .headerObfuscator(headerObfuscator)
                .queryObfuscator(queryObfuscator)
                .bodyObfuscator(bodyObfuscator)
                .formatter(formatter)
                .writer(writer)
                .build();
    }

    private Predicate<RawHttpRequest> mergeWithExcludes(final Predicate<RawHttpRequest> predicate) {
        return properties.getExclude().stream()
                .map(Conditions::requestTo)
                .map(Predicate::negate)
                .reduce(predicate, Predicate::and);
    }

    @Bean
    @ConditionalOnMissingBean(name = "condition")
    public Predicate<RawHttpRequest> condition() {
        return $ -> true;
    }

    @Bean
    @ConditionalOnMissingBean(QueryObfuscator.class)
    public QueryObfuscator queryObfuscator() {
        final List<String> parameters = properties.getObfuscate().getParameters();
        return parameters.isEmpty() ?
                Obfuscators.accessToken() :
                parameters.stream()
                        .map(parameter -> Obfuscators.obfuscate(parameter, "XXX"))
                        .collect(toList()).stream()
                        .reduce(QueryObfuscator.none(), QueryObfuscator::merge);
    }

    @Bean
    @ConditionalOnMissingBean(HeaderObfuscator.class)
    public HeaderObfuscator headerObfuscator() {
        final List<String> headers = properties.getObfuscate().getHeaders();
        return headers.isEmpty() ?
                Obfuscators.authorization() :
                headers.stream()
                        .map(header -> Obfuscators.obfuscate(header::equalsIgnoreCase, "XXX"))
                        .collect(toList()).stream()
                        .reduce(HeaderObfuscator.none(), HeaderObfuscator::merge);
    }

    @Bean
    @ConditionalOnMissingBean(BodyObfuscator.class)
    public BodyObfuscator bodyObfuscator() {
        return BodyObfuscator.none();
    }

    @Bean
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    @ConditionalOnProperty(name = "logbook.format.style", havingValue = "http")
    public HttpLogFormatter httpFormatter() {
        return new DefaultHttpLogFormatter();
    }

    @Bean
    @ConditionalOnBean(ObjectMapper.class)
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    public HttpLogFormatter jsonFormatter(
            @SuppressWarnings("SpringJavaAutowiringInspection") final ObjectMapper mapper) {
        return new JsonHttpLogFormatter(mapper);
    }

    @Bean
    @ConditionalOnMissingBean(HttpLogWriter.class)
    public HttpLogWriter writer(final Logger httpLogger) {
        final Level level = properties.getWrite().getLevel();

        return level == null ?
                new DefaultHttpLogWriter(httpLogger) :
                new DefaultHttpLogWriter(httpLogger, level);
    }

    @Bean
    @ConditionalOnMissingBean(name = "httpLogger")
    public Logger httpLogger() {
        final String category = properties.getWrite().getCategory();
        return LoggerFactory.getLogger(firstNonNull(category, Logbook.class.getName()));
    }

}
