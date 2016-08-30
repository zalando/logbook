package org.zalando.logbook.spring;

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
