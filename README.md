# Spring Web MVC Logging

[![Build Status](https://img.shields.io/travis/zalando/spring-web-logging.svg)](https://travis-ci.org/zalando/spring-web-logging)
[![Coverage Status](https://img.shields.io/coveralls/zalando/spring-web-logging.svg)](https://coveralls.io/r/zalando/spring-web-logging)
[![Release](https://img.shields.io/github/release/zalando/spring-web-logging.svg)](https://github.com/zalando/spring-web-logging/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/spring-web-logging.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/spring-web-logging)

Spring Web MVC request and response logging (including payload).

# Usage

You have to register the ``LoggingFilter`` as a ``Filter`` of your dispatcher filter chain.

```java
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
```

# Customization

If you want to override the log output of the ``LoggingFilter`` you have to implement your own ``HttpLogger``.

```java
public class CustomerHttpLogger implements HttpLogger {

    @Override
    boolean shouldLog(final HttpServletRequest request, final HttpServletResponse response) {
        return true;
    }

    @Override
    public void logRequest(final RequestData request) {
        // TODO log
    }

    @Override
    public void logResponse(final ResponseData response) {
        // TODO log
    }

}

(...)

filter = new LoggingFilter(new CustomerHttpLogger());
```

*Spring Web Logging* comes with two default implementations:

- `DefaultHttpLogger` logs multiline log messages, comparable to Apache HTTP's wire log
- `JsonHttpLogger` logs requests and responses as single line JSON objects

## License

Copyright [2015] Zalando SE

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
