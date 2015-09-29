# Logbook



[![Build Status](https://img.shields.io/travis/zalando/logbook.svg)](https://travis-ci.org/zalando/logbook)
[![Coverage Status](https://img.shields.io/coveralls/zalando/logbook.svg)](https://coveralls.io/r/zalando/logbook)
[![Release](https://img.shields.io/github/release/zalando/logbook.svg)](https://github.com/zalando/logbook/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/logbook.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/logbook)

Servlet 3.0 filter for request and response logging (including payload).

# Dependency

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook</artifactId>
    <version>${logbook.version}</version>
</dependency>
```

# Usage

You have to register the `LogbookFilter` as a `Filter` in your filter chain.

Either in your `web.xml` file:

```xml
<filter>
    <filter-name>LogbookFilter</filter-name>
    <filter-class>org.zalando.logbook.LogbookFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>LogbookFilter</filter-name>
    <url-pattern>/*</url-pattern>
    <dispatcher>FORWARD</dispatcher>
    <dispatcher>INCLUDE</dispatcher>
    <dispatcher>REQUEST</dispatcher>
    <dispatcher>ASYNC</dispatcher>
    <dispatcher>ERROR</dispatcher>
</filter-mapping>
```

Or programmatically via the `ServletContext`:

```java
context.addFilter("LogbookFilter", new LogbookFilter())
    .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*"); 
```

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
