# Logbook

[![Logbook](docs/logbook.jpg)](#attributions)

[![Build Status](https://img.shields.io/travis/zalando/logbook.svg)](https://travis-ci.org/zalando/logbook)
[![Coverage Status](https://img.shields.io/coveralls/zalando/logbook.svg)](https://coveralls.io/r/zalando/logbook)
[![Release](https://img.shields.io/github/release/zalando/logbook.svg)](https://github.com/zalando/logbook/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/logbook-parent.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/logbook-parent)

*Logbook* is an extensible library to enable request and response logging for different client- and server-side 
technologies. It comes with a core module `logbook-core` and specific modules per framework, e.g. 
[`logbook-servlet`](#servlet) for Servlet 3.0 environments and [`logbook-httpclient`](#http-client) for applications 
using Apache's `HttpClient`.

## Dependency

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook</artifactId>
    <version>${logbook.version}</version>
</dependency>
```
## Usage

All integrations require an instance of `Logbook` which holds all configuration and wires all necessary parts together. 
You can either create one using all the defaults:

```java
Logbook logbook = Logbook.create()
```

or create a customized version using the `LogbookBuilder`:

```java
Logbook logbook = Logbook.builder()
    .formatter(new CustomHttpLogFormatter())
    .writer(new CustomHttpLogWriter())
    .build();
```

Logbook works in three phases:

1. [Obfuscation](#obfuscation),
2. [Formatting](#formatting) and
3. [Writing](#writing)

Each phase is represented by one or more interfaces that can be used for customization and every phase has a sensible
default:

## Obfuscation

The goal of *Obfuscation* is to prevent certain sensitive parts of HTTP requests and responses to be logged. This
usually includes the *Authorization* header but could also apply to certain plaintext query or form parameters,
e.g. *password*.

Logbook differentiates between `Obfuscator` (for headers and query parameters) and `BodyObfuscator`. The default
behaviour for all of them is to **not** obfuscate at all.

You can use predefined obfuscators:

```java
Logbook logbook = Logbook.builder()
    // will replace the Authorization header value with XXX
    .headerObfuscator(authorization())
    .build();
```

or create custom ones:

```java
Logbook logbook = Logbook.builder()
    .parameterObfuscator(obfuscate("password"::equals, "XXX"))
    .build();
```

or combine them:

```java
Logbook logbook = Logbook.builder()
    .headerObfuscator(compound(
        authorization(), 
        obfuscate("X-Secret"::equals, "XXX")))
    .build();
```

## Correlation

Requests and responses are correlated using a *correlation id*. This allows to match related requests and responses that would usually be located in different places in the log file.

## Formatting

Formatting defines how requests and responses will be transformed to strings basically. Formatters do **not** specify
where requests and responses are logged to, that's the work of writers.

Logbook comes with two different formatters by default - *HTTP* and *JSON*:

### HTTP

*HTTP* is the default formatting style, is provided by the `DefaultHttpLogFormatter` and is primarily designed to be
used for local development and debugging. Since it's harder to read by machines this is usually not meant to be used
in production.

#### Request

```http
Incoming Request: 2d66e4bc-9a0d-11e5-a84c-1f39510f0d6b
GET http://example.org/test HTTP/1.1
Accept: application/json
Host: localhost
Content-Type: text/plain

Hello world!
```

#### Response

```http
Outgoing Response: 2d66e4bc-9a0d-11e5-a84c-1f39510f0d6b
HTTP/1.1 200
Content-Type: application/json

{"value":"Hello world!"}
```

### JSON

*JSON* is an alternative formatting style, is provided by the `JsonHttpLogFormatter` and is primarily designed to be
used for production since it's easily consumed by parsers and log consumers.

#### Request

```json
{
  "origin": "remote",
  "type": "request",
  "correlation": "2d66e4bc-9a0d-11e5-a84c-1f39510f0d6b",
  "sender": "127.0.0.1",
  "method": "GET",
  "path": "http://example.org/test",
  "headers": {
    "Accept": ["application/json"],
    "Content-Type": ["text/plain"]
  },
  "params": {
    "limit": "1000"
  },
  "body": "Hello world!"
}
```

#### Response

```json
{
  "origin": "local",
  "type": "response",
  "correlation": "2d66e4bc-9a0d-11e5-a84c-1f39510f0d6b",
  "status": 200,
  "headers": {
    "Content-Type": ["text/plain"]
  },
  "body": "Hello world!"
}
```

Note: Bodies of type `application/json` (and `application/*+json`) will be *inlined* into the resulting JSON tree, i.e.
a JSON response body will **not** be escaped and represented as a string:

```json
{
  "origin": "local",
  "type": "response",
  "correlation": "2d66e4bc-9a0d-11e5-a84c-1f39510f0d6b",
  "status": 200,
  "headers": {
    "Content-Type": ["text/plain"]
  },
  "body": {
    "greeting": "Hello, world!"
  }
}
```

## Writing

Writing defines where formatted requests and responses are written to. Logback comes with two implementations:

### Logger

By default requests and responses are logged using a *slf4j* logger that uses the `org.zalando.logbook.Logbook` 
category and the log level `trace`. This can be customized though:

```java
Logbook logbook = Logbook.builder()
    .writer(new DefaultHttpLogWriter(
        LoggerFactory.getLogger("http.wire-log"), 
        Level.DEBUG))
    .build();
```

### Stream

An alternative implementation is logging requests and responses to a `PrintStream`, e.g. `System.out` or `System.err`. 
This is usually a bad choice for running in production, but might be used for short-term local development and/or 
investigations.

```java
Logbook logbook = Logbook.builder()
    .writer(new StreamHttpLogWriter(System.err))
    .build();
```

## Servlet

### Dependency

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-servlet</artifactId>
    <version>${logbook.version}</version>
</dependency>
```

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
    <dispatcher>REQUEST</dispatcher>
    <dispatcher>ASYNC</dispatcher>
    <dispatcher>ERROR</dispatcher>
</filter-mapping>
```
(Please note that the xml approach will use all the defaults and is **not** configurable.)

Or programmatically via the `ServletContext`:

```java
context.addFilter("LogbookFilter", new LogbookFilter(logbook))
    .addMappingForUrlPatterns(EnumSet.of(REQUEST, ASYNC, ERROR), true, "/*"); 
```

### Security

Secure application usually a slightly different setup due to the reason that you should generally avoid logging
unauthorized requests, especially the body, as it allows attackers to flood your logfile, and therefore your precious
disk space, pretty quickly. Assuming that your application handles authorization inside another filter you have two
possible scenarios:

1. You don't log unauthorized requests
2. You log unauthorized requests without the request body

The first scenario can easily be accomplished by placing the `LogbookFilter` after your security filter. The second
setup is a little bit more sophisticated. You need two `LogbookFilter` instances, one before your security filter and
one after it:

```java
context.addFilter("unauthorizedLogbookFilter", new LogbookFilter(logbook, Strategy.SECURITY))
    .addMappingForUrlPatterns(EnumSet.of(REQUEST, ASYNC, ERROR), true, "/*");
context.addFilter("securityFilter", new SecurityFilter())
    .addMappingForUrlPatterns(EnumSet.of(REQUEST), true, "/*");
context.addFilter("authorizedLogbookFilter", new LogbookFilter(logbook))
    .addMappingForUrlPatterns(EnumSet.of(REQUEST, ASYNC, ERROR), true, "/*");
```

The first logbook filter will log unauthorized requests only while the second one will log authorized requests as
always.

## HTTP Client

### Dependency

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-httpclient</artifactId>
    <version>${logbook.version}</version>
</dependency>
```

The `logbook-httpclient` module contains both a `HttpRequestInterceptor` as well as a `HttpResponseInterceptor` to
be used with the `HttpClient`:

```java
CloseableHttpClient client = HttpClientBuilder.create()
        .addInterceptorFirst(new LogbookHttpRequestInterceptor(logbook))
        .addInterceptorFirst(new LogbookHttpResponseInterceptor())
        .build();
```

## Spring Boot Starter

Logbook comes with a convenient auto configuration for Spring Boot users:

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-spring-boot-starter</artifactId>
    <version>${logbook.version}</version>
</dependency>
```

It sets up all of the following parts automatically with sensible defaults:
- Servlet Filter
- 2nd Servlet Filter for unauthorized requests (if Spring Security is detected)
- Header-/Parameter-/Body-Obfuscators
- HTTP-/JSON-style formatter
- Logging writer


| Type                     | Name                        | Default                                    |
|--------------------------|-----------------------------|--------------------------------------------|
| `FilterRegistrationBean` | `unauthorizedLogbookFilter` | Based on `LogbookFilter`                   |
| `FilterRegistrationBean` | `authorizedLogbookFilter`   | Based on `LogbookFilter`                   |
| `Logbook`                |                             | Based on obfuscators, formatter and writer |
| `Obfuscator`             | `headerObfuscator`          | Based on `logbook.obfuscate.headers`       |
| `Obfuscator`             | `parameterObfuscator`       | Based on `logbook.obfuscate.parameters`    |
| `BodyObfuscator`         |                             |`BodyObfuscator.none()`                     |
| `HttpLogFormatter`       |                             | `JsonHttpLogFormatter`                     |
| `HttpLogWriter`          |                             | `DefaultHttpLogWriter`                     |

### Configuration

The following tables shows the available configuration:

| Configuration                  | Description                                                      | Default                       |
|--------------------------------|------------------------------------------------------------------|-------------------------------|
| `logbook.filter.enabled`       | Enable the [`LogbookFilter(s)`](#servlet)                        | `true`                        |
| `logbook.format.style`         | Configure the [formatting style](#formatting) (`http` or `json`) | `json`                        |
| `logbook.write.category`       | Changes the category of the [`DefaultHttpLogWriter`](#logger)    | `org.zalando.logbook.Logbook` |
| `logbook.write.level`          | Changes the level of the [`DefaultHttpLogWriter`](#logger)       | `TRACE`                       |
| `logbook.obfuscate.headers`    | List of header names that need obfuscation                       | `[Authorization]`             |
| `logbook.obfuscate.parameters` | List of parameter names that need obfuscation                    | `[]`                          |

```yaml
logbook:
    filter.enabled: true
    format.style: http
    obfuscate:
        headers:
            - Authorization
            - X-Secret
        parameters:
            - access_token
    write:
        category: http.wire-log
        level: INFO
```

## Attributions

![Creative Commons (Attribution-Share Alike 3.0 Unported](https://licensebuttons.net/l/by-sa/3.0/80x15.png)
[*Grand Turk, a replica of a three-masted 6th rate frigate from Nelson's days - logbook and charts*](https://commons.wikimedia.org/wiki/File:Grand_Turk(34).jpg)
by [JoJan](https://commons.wikimedia.org/wiki/User:JoJan) is licensed under a
[Creative Commons (Attribution-Share Alike 3.0 Unported)](http://creativecommons.org/licenses/by-sa/3.0/).

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
