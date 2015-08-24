# Spring Web MVC Logging

[![Build Status](https://img.shields.io/travis/zalando/spring-web-logging.svg)](https://travis-ci.org/zalando/spring-web-logging)
[![Coverage Status](https://img.shields.io/coveralls/zalando/spring-web-logging.svg)](https://coveralls.io/r/zalando/spring-web-logging)

Spring Web MVC request and response logging (including payload).

# Usage

    @Bean
    public FilterRegistrationBean loggingFilter() {
        final LoggingFilter filter = new LoggingFilter();
        final FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(filter.getOrder());
        return registration;
    }