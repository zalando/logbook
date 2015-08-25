# Spring Web MVC Logging

[![Build Status](https://img.shields.io/travis/zalando/spring-web-logging.svg)](https://travis-ci.org/zalando/spring-web-logging)
[![Coverage Status](https://img.shields.io/coveralls/zalando/spring-web-logging.svg)](https://coveralls.io/r/zalando/spring-web-logging)

Spring Web MVC request and response logging (including payload).

# Usage

You have to register the ``LoggingFilter`` as a ``Filter` of your dispatcher filter chain.

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

# Customization

If you want to override the log output of the ``LoggingFilter`` you have to implement your own ``HttpLogger``.

    public class AsJsonHttpLogger implements HttpLogger {

        private final ObjectMapper objectMapper;

        public AsJsonHttpLogger(final ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public void logRequest(final RequestData request) {
            LOG.trace("Incoming: [{}]", objectMapper.writeValueAsString(request));
        }

        @Override
        public void logResponse(final ResponseData response) {
            LOG.trace("Outgoing: [{}]", objectMapper.writeValueAsString(response));
        }
    }