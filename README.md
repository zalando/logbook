# Logbook: HTTP request and response logging

[![Logbook](docs/logbook.jpg)](#attributions)

[![Build Status](https://img.shields.io/travis/zalando/logbook/master.svg)](https://travis-ci.org/zalando/logbook)
[![Coverage Status](https://img.shields.io/coveralls/zalando/logbook/master.svg)](https://coveralls.io/r/zalando/logbook)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/org.zalando/logbook-core/badge.svg)](http://www.javadoc.io/doc/org.zalando/logbook-core)
[![Release](https://img.shields.io/github/release/zalando/logbook.svg)](https://github.com/zalando/logbook/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/logbook-parent.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/logbook-parent)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/zalando/logbook/master/LICENSE)


> **Logbook** noun, /lɑɡ bʊk/: A book in which measurements from the ship's log are recorded, along with other salient details of the voyage.

**Logbook** is an extensible Java library to enable complete request and response logging for different client- and server-side technologies. It satisfies a special need by a) allowing web application developers to log any HTTP traffic that an application receives or sends b) in a way that makes it easy to persist and analyze it later. This can be useful for traditional log analysis, meeting audit requirements or investigating individual historic traffic issues. 

Logbook is ready to use out of the box for most common setups. Even for uncommon applications and technologies, it should be simple to implement the necessary interfaces to connect a library/framework/etc. to it.

## Features

- **Logging**: of HTTP requests and responses, including the body; partial logging (no body) for unauthorized requests
- **Customization**: of logging format, logging destination, and conditions that request to log
- **Support**: for Servlet containers, Apache’s HTTP client, and (via its elegant API) other frameworks
- Optional obfuscation of sensitive data
- [Spring Boot](http://projects.spring.io/spring-boot/) Auto Configuration
- [Scalyr](docs/scalyr.md) compatible
- Sensible defaults

## Dependencies

- Java 8
- Any build tool using Maven Central, or direct download
- Servlet Container (optional)
- Apache HTTP Client (optional)
- Spring Boot (optional)

## Installation

Selectively add the following dependencies to your project:

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-core</artifactId>
    <version>${logbook.version}</version>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-servlet</artifactId>
    <version>${logbook.version}</version>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-httpclient</artifactId>
    <version>${logbook.version}</version>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-spring-boot-starter</artifactId>
    <version>${logbook.version}</version>
</dependency>
```

## Usage

All integrations require an instance of `Logbook` which holds all configuration and wires all necessary parts together. 
You can either create one using all the defaults:

```java
Logbook logbook = Logbook.create();
```
or create a customized version using the `LogbookBuilder`:

```java
Logbook logbook = Logbook.builder()
    .condition(new CustomCondition())
    .rawRequestFilter(new CustomRawRequestFilter())
    .rawResponseFilter(new CustomRawResponseFilter())
    .queryFilter(new CustomQueryFilter())
    .headerFilter(new CustomHeaderFilter())
    .bodyFilter(new CustomBodyFilter())
    .requestFilter(new CustomRequestFilter())
    .responseFilter(new CustomResponseFilter())
    .formatter(new CustomHttpLogFormatter())
    .writer(new CustomHttpLogWriter())
    .build();
```

### Phases

Logbook works in several different phases:

1. [Conditional](#conditional),
2. [Filtering](#filtering),
3. [Formatting](#formatting) and
4. [Writing](#writing)

Each phase is represented by one or more interfaces that can be used for customization. Every phase has a sensible default.

#### Conditional

Logging HTTP messages and including their bodies is a rather expensive task, so it makes a lot of sense to disable logging for certain requests. A common use case would be to ignore *health check* requests from a load balancer, or any request to management endpoints typically issued by developers.

Defining a condition is as easy as writing a special `Predicate` that decides whether a request (and its corresponding response) should be logged or not. Alternatively you can use and combine predefined predicates:

```java
Logbook logbook = Logbook.builder()
    .condition(exclude(
        requestTo("/health"),
        requestTo("/admin/**"),
        contentType("application/octet-stream"),
        header("X-Secret", newHashSet("1", "true")::contains)))
    .build();
```

Exclusion patterns, e.g. `/admin/**`, are loosely following [Ant's style of path patterns](https://ant.apache.org/manual/dirtasks.html#patterns)
without taking the the query string of the URL into consideration.

#### Filtering

The goal of *Filtering* is to prevent the logging of certain sensitive parts of HTTP requests and responses. This
usually includes the *Authorization* header, but could also apply to certain plaintext query or form parameters — 
e.g. *password*.

Logbook supports different types of filters:

| Type                | Operates on                    | Applies to | Default         |
|---------------------|--------------------------------|------------|-----------------|
| `RawRequestFilter`  | `RawHttpRequest`               | request    | binary/streams  |
| `RawResponseFilter` | `RawHttpResponse`              | response   | binary/streams  |
| `QueryFilter`       | Query string                   | request    | `access_token`  |
| `HeaderFilter`      | Header (single key-value pair) | both       | `Authorization` |
| `BodyFilter`        | Content-Type and body          | both       | n/a             |
| `RequestFilter`     | `HttpRequest`                  | request    | n/a             |
| `ResponseFilter`    | `HttpResponse`                 | response   | n/a             |

`QueryFilter`, `HeaderFilter` and `BodyFilter` are relatively high-level and should cover all needs in ~90% of all
cases. For more complicated setups one should fallback to the low-level variants, i.e. `RawRequestFilter` and
`RawResponseFilter` as well as `RequestFilter` and `ResponseFilter` respectively (in conjunction with 
`ForwardingRawHttpRequest`/`ForwardingRawHttpResponse` and `ForwardingHttpRequest`/`ForwardingHttpResponse`).

You can configure filters like this:

```java
Logbook logbook = Logbook.builder()
    .rawRequestFilter(replaceBody(contentType("audio/*"), "mmh mmh mmh mmh"))
    .rawResponseFilter(replaceBody(contentType("*/*-stream"), "It just keeps going and going..."))
    .queryFilter(accessToken())
    .queryFilter(replaceQuery("password", "<secret>"))
    .headerFilter(authorization()) 
    .headerFilter(eachHeader("X-Secret"::equalsIgnoreCase, "<secret>"))
    .build();
```

You can configure as many filters as you want - they will run consecutively.

#### Correlation

Logbook uses a *correlation id* to correlate requests and responses. This allows match-related requests and responses that would usually be located in different places in the log file.

#### Formatting

*Formatting* defines how requests and responses will be transformed to strings basically. Formatters do **not** specify where requests and responses are logged to — writers do that work.

Logbook comes with two different default formatters: *HTTP* and *JSON*.

##### HTTP

*HTTP* is the default formatting style, provided by the `DefaultHttpLogFormatter`. It is primarily designed to be used for local development and debugging, not for production use. This is because it’s not as readily machine-readable as JSON.

###### Request

```http
Incoming Request: 2d66e4bc-9a0d-11e5-a84c-1f39510f0d6b
GET http://example.org/test HTTP/1.1
Accept: application/json
Host: localhost
Content-Type: text/plain

Hello world!
```

###### Response

```http
Outgoing Response: 2d66e4bc-9a0d-11e5-a84c-1f39510f0d6b
Duration: 25 ms
HTTP/1.1 200
Content-Type: application/json

{"value":"Hello world!"}
```

##### JSON

*JSON* is an alternative formatting style, provided by the `JsonHttpLogFormatter`. Unlike HTTP, it is primarily designed for production use — parsers and log consumers can easily consume it. 

###### Request

```json
{
  "origin": "remote",
  "type": "request",
  "correlation": "2d66e4bc-9a0d-11e5-a84c-1f39510f0d6b",
  "protocol": "HTTP/1.1",
  "sender": "127.0.0.1",
  "method": "GET",
  "path": "http://example.org/test",
  "headers": {
    "Accept": ["application/json"],
    "Content-Type": ["text/plain"]
  },
  "body": "Hello world!"
}
```

###### Response

```json
{
  "origin": "local",
  "type": "response",
  "correlation": "2d66e4bc-9a0d-11e5-a84c-1f39510f0d6b",
  "duration": 25,
  "protocol": "HTTP/1.1",
  "status": 200,
  "headers": {
    "Content-Type": ["text/plain"]
  },
  "body": "Hello world!"
}
```

Note: Bodies of type `application/json` (and `application/*+json`) will be *inlined* into the resulting JSON tree. I.e.,
a JSON response body will **not** be escaped and represented as a string:

```json
{
  "origin": "local",
  "type": "response",
  "correlation": "2d66e4bc-9a0d-11e5-a84c-1f39510f0d6b",
  "duration": 25,
  "protocol": "HTTP/1.1",
  "status": 200,
  "headers": {
    "Content-Type": ["application/json"]
  },
  "body": {
    "greeting": "Hello, world!"
  }
}
```

##### cURL

*cURL* is an alternative formatting style, provided by the `CurlHttpLogFormatter` which will render requests as 
executable [`cURL`](https://curl.haxx.se/) commands. Unlike JSON, it is primarily designed for humans. 


###### Request

```bash
curl -v -X GET 'http://localhost/test' -H 'Accept: application/json'
```

###### Response

See [HTTP](#http) or provide own fallback for responses:

```java
new CurlHttpLogFormatter(new JsonHttpLogFormatter());
```

#### Writing

Writing defines where formatted requests and responses are written to. Logbook comes with three implementations: 
Logger, Stream and Chunking.

##### Logger

By default, requests and responses are logged with an *slf4j* logger that uses the `org.zalando.logbook.Logbook` category and the log level `trace`. This can be customized:

```java
Logbook logbook = Logbook.builder()
    .writer(new DefaultHttpLogWriter(
        LoggerFactory.getLogger("http.wire-log"), 
        Level.DEBUG))
    .build();
```

##### Stream

An alternative implementation is to log requests and responses to a `PrintStream`, e.g. `System.out` or `System.err`. This is usually a bad choice for running in production, but can sometimes be useful for short-term local development and/or investigation.

```java
Logbook logbook = Logbook.builder()
    .writer(new StreamHttpLogWriter(System.err))
    .build();
```

##### Chunking

The `ChunkingHttpLogWriter` will split long messages into smaller chunks and will write them individually while delegating to another writer:

```java
Logbook logbook = Logbook.builder()
    .writer(new ChunkingHttpLogWriter(1000, new DefaultHttpLogWriter()))
    .build();

```

### Servlet

You’ll have to register the `LogbookFilter` as a `Filter` in your filter chain — either in your `web.xml` file (please note that the xml approach will use all the defaults and is not configurable):

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

or programmatically, via the `ServletContext`:

```java
context.addFilter("LogbookFilter", new LogbookFilter(logbook))
    .addMappingForUrlPatterns(EnumSet.of(REQUEST, ASYNC, ERROR), true, "/*"); 
```

#### Security

Secure applications usually need a slightly different setup. You should generally avoid logging unauthorized requests, especially the body, because it quickly allows attackers to flood your logfile — and, consequently, your precious disk space. Assuming that your application handles authorization inside another filter, you have two choices:

- Don't log unauthorized requests
- Log unauthorized requests without the request body

You can easily achieve the former setup by placing the `LogbookFilter` after your security filter. The latter is a little bit more sophisticated. You’ll need two `LogbookFilter` instances — one before your security filter, and one after it:

```java
context.addFilter("unauthorizedLogbookFilter", new LogbookFilter(logbook, Strategy.SECURITY))
    .addMappingForUrlPatterns(EnumSet.of(REQUEST, ASYNC, ERROR), true, "/*");
context.addFilter("securityFilter", new SecurityFilter())
    .addMappingForUrlPatterns(EnumSet.of(REQUEST), true, "/*");
context.addFilter("authorizedLogbookFilter", new LogbookFilter(logbook))
    .addMappingForUrlPatterns(EnumSet.of(REQUEST, ASYNC, ERROR), true, "/*");
```

The first logbook filter will log unauthorized requests **only**. The second filter will log authorized requests, as always.

### HTTP Client

The `logbook-httpclient` module contains both an `HttpRequestInterceptor` and an `HttpResponseInterceptor` to use with the `HttpClient`:

```java
CloseableHttpClient client = HttpClientBuilder.create()
        .addInterceptorFirst(new LogbookHttpRequestInterceptor(logbook))
        .addInterceptorFirst(new LogbookHttpResponseInterceptor())
        .build();
```

Since the `LogbookHttpResponseInterceptor` is incompatible with the `HttpAsyncClient` there is another way to log responses:

```java
CloseableHttpAsyncClient client = HttpAsyncClientBuilder.create()
        .addInterceptorFirst(new LogbookHttpRequestInterceptor(logbook))
        .build();
        
// and then wrap your response consumer
client.execute(producer, new LogbookHttpAsyncResponseConsumer<>(consumer), callback)
```

### Spring Boot Starter

Logbook comes with a convenient auto configuration for Spring Boot users. It sets up all of the following parts automatically with sensible defaults:
    
- Servlet filter
- Second Servlet filter for unauthorized requests (if Spring Security is detected)
- Header-/Parameter-/Body-Filters
- HTTP-/JSON-style formatter
- Logging writer

| Type                        | Name                        | Default                                             |
|-----------------------------|-----------------------------|-----------------------------------------------------|
| `FilterRegistrationBean`    | `unauthorizedLogbookFilter` | Based on `LogbookFilter`                            |
| `FilterRegistrationBean`    | `authorizedLogbookFilter`   | Based on `LogbookFilter`                            |
| `Logbook`                   |                             | Based on condition, filters, formatter and writer   |
| `Predicate<RawHttpRequest>` | `requestCondition`          | No filter; is later combined with `logbook.exclude` |
| `RawRequestFilter`          |                             | `RawRequestFilters.defaultValue()`                  |
| `RawResponseFilter`         |                             | `RawResponseFilters.defaultValue()`                 |
| `HeaderFilter`              |                             | Based on `logbook.obfuscate.headers`                |
| `QueryFilter`               |                             | Based on `logbook.obfuscate.parameters`             |
| `BodyFilter`                |                             | `BodyFilters.defaultValue()`                        |
| `RequestFilter`             |                             | `RequestFilter.none()`                              |
| `ResponseFilter`            |                             | `ResponseFilter.none()`                             |
| `HttpLogFormatter`          |                             | `JsonHttpLogFormatter`                              |
| `HttpLogWriter`             |                             | `DefaultHttpLogWriter`                              |

Multiple filters are merged into one.

#### Configuration

The following tables show the available configuration:

| Configuration                  | Description                                                      | Default                       |
|--------------------------------|------------------------------------------------------------------|-------------------------------|
| `logbook.exclude`              | Exclude certain URLs                                             | `[]`                          |
| `logbook.filter.enabled`       | Enable the [`LogbookFilter(s)`](#servlet)                        | `true`                        |
| `logbook.format.style`         | [Formatting style](#formatting) (`http`, `json` or `curl`)       | `json`                        |
| `logbook.obfuscate.headers`    | List of header names that need obfuscation                       | `[Authorization]`             |
| `logbook.obfuscate.parameters` | List of parameter names that need obfuscation                    | `[access_token]`              |
| `logbook.write.category`       | Changes the category of the [`DefaultHttpLogWriter`](#logger)    | `org.zalando.logbook.Logbook` |
| `logbook.write.level`          | Changes the level of the [`DefaultHttpLogWriter`](#logger)       | `TRACE`                       |
| `logbook.write.chunk-size`     | Splits log lines into smaller chunks of size up-to `chunk-size`. | `0` (disabled)                |

##### Example configuration

```yaml
logbook:
    exclude:
        - /health
        - /admin/**
    filter.enabled: true
    format.style: http
    obfuscate:
        headers:
            - Authorization
            - X-Secret
        parameters:
            - access_token
            - password
    write:
        category: http.wire-log
        level: INFO
        chunk-size: 1000
```

## Known Issues

The Logbook Servlet integration is **incompatible with incoming POST requests that use `application/x-www-form-urlencoded`** form parameters and use any of the `HttpServletRequest.getParameter*(..)` methods. See issue [#94](../../issues/94) for details.

The Logbook HTTP Client integration is handling gzip-compressed response entities incorrectly if the interceptor runs before a decompressing interceptor. Since logging compressed contents is not really helpful it's advised to register the logbook interceptor as the last interceptor in the chain.

## Getting Help with Logbook

If you have questions, concerns, bug reports, etc., please file an issue in this repository's [Issue Tracker](https://github.com/zalando/logbook/issues).

## Getting Involved/Contributing

To contribute, simply make a pull request and add a brief description (1-2 sentences) of your addition or change. For
more details, check the [contribution guidelines](CONTRIBUTING.md).

## Alternatives

Logbook puts a big emphasis on logging the actual request/response body that was sent over the wire. The Apache
HttpClient, among the following alternatives, is the only technology to support that.

- [Apache HttpClient Wire Logging](http://hc.apache.org/httpcomponents-client-4.5.x/logging.html)
- [Spring Boot Access Logging](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-configure-accesslogs)
- [Tomcat Request Dumper Filter](https://tomcat.apache.org/tomcat-7.0-doc/config/filter.html#Request_Dumper_Filter)

## Credits and References

![Creative Commons (Attribution-Share Alike 3.0 Unported](https://licensebuttons.net/l/by-sa/3.0/80x15.png)
[*Grand Turk, a replica of a three-masted 6th rate frigate from Nelson's days - logbook and charts*](https://commons.wikimedia.org/wiki/File:Grand_Turk(34).jpg)
by [JoJan](https://commons.wikimedia.org/wiki/User:JoJan) is licensed under a
[Creative Commons (Attribution-Share Alike 3.0 Unported)](http://creativecommons.org/licenses/by-sa/3.0/).
