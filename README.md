# Logbook: HTTP request and response logging

[![Logbook](docs/logbook.jpg)](#attributions)

[![Stability: Active](https://masterminds.github.io/stability/active.svg)](https://masterminds.github.io/stability/active.html)
![Build Status](https://github.com/zalando/logbook/workflows/build/badge.svg)
[![Coverage Status](https://img.shields.io/coveralls/zalando/logbook/main.svg)](https://coveralls.io/r/zalando/logbook)
[![Javadoc](http://javadoc.io/badge/org.zalando/logbook-core.svg)](http://www.javadoc.io/doc/org.zalando/logbook-core)
[![Release](https://img.shields.io/github/release/zalando/logbook.svg)](https://github.com/zalando/logbook/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/logbook-parent.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/logbook-parent)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/zalando/logbook/main/LICENSE)

> **Logbook** noun, /lɑɡ bʊk/: A book in which measurements from the ship's log are recorded, along with other salient details of the voyage.

**Logbook** is an extensible Java library to enable complete request and response logging for different client- and server-side technologies. It satisfies a special need by a) allowing web application developers to log any HTTP traffic that an application receives or sends b) in a way that makes it easy to persist and analyze it later. This can be useful for traditional log analysis, meeting audit requirements or investigating individual historic traffic issues. 

Logbook is ready to use out of the box for most common setups. Even for uncommon applications and technologies, it should be simple to implement the necessary interfaces to connect a library/framework/etc. to it.

## Features

- **Logging**: of HTTP requests and responses, including the body; partial logging (no body) for unauthorized requests
- **Customization**: of logging format, logging destination, and conditions that request to log
- **Support**: for Servlet containers, Apache’s HTTP client, Square's OkHttp, and (via its elegant API) other frameworks
- Optional obfuscation of sensitive data
- [Spring Boot](http://projects.spring.io/spring-boot/) Auto Configuration
- [Scalyr](docs/scalyr.md) compatible
- Sensible defaults

## Dependencies

- Java 8
- Any build tool using Maven Central, or direct download
- Servlet Container (optional)
- Apache HTTP Client 4.x **or 5.x** (optional)  
- JAX-RS 2.x Client and Server (optional)
- Netty 4.x (optional)
- OkHttp 2.x **or 3.x** (optional)
- Spring 5.x** (optional)
- Spring Boot 2.x** (optional)
- Ktor (optional)
- logstash-logback-encoder 5.x (optional)

## Installation

Add the following dependency to your project:

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-core</artifactId>
    <version>${logbook.version}</version>
</dependency>
```

Additional modules/artifacts of Logbook always share the same version number.

Alternatively, you can import our *bill of materials*...

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.zalando</groupId>
      <artifactId>logbook-bom</artifactId>
      <version>${logbook.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

<details>
  <summary>... which allows you to omit versions:</summary>

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-core</artifactId>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-httpclient</artifactId>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-jaxrs</artifactId>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-json</artifactId>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-netty</artifactId>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-okhttp</artifactId>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-okhttp2</artifactId>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-servlet</artifactId>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-ktor-common</artifactId>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-ktor-client</artifactId>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-ktor-server</artifactId>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-ktor</artifactId>
</dependency>
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-logstash</artifactId>
</dependency>
```
</details>

The logbook logger must be configured to trace level in order to log the requests and responses. With Spring Boot 2 (using Logback) this can be accomplised by adding the following line to your `application.properties`

```
logging.level.org.zalando.logbook: TRACE
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
    .queryFilter(new CustomQueryFilter())
    .pathFilter(new CustomPathFilter())
    .headerFilter(new CustomHeaderFilter())
    .bodyFilter(new CustomBodyFilter())
    .requestFilter(new CustomRequestFilter())
    .responseFilter(new CustomResponseFilter())
    .sink(new DefaultSink(
            new CustomHttpLogFormatter(),
            new CustomHttpLogWriter()
    ))
    .build();
```

### Strategy

Logbook used to have a very rigid strategy how to do request/response logging:

- Requests/responses are logged separately
- Requests/responses are logged soon as possible
- Requests/responses are logged as a pair or not logged at all  
  (i.e. no partial logging of traffic)

Some of those restrictions could be mitigated with custom [`HttpLogWriter`](#writing)
implementations, but they were never ideal.

Starting with version 2.0 Logbook now comes with a [Strategy pattern](https://en.wikipedia.org/wiki/Strategy_pattern)
at its core. Make sure you read the documentation of the [`Strategy`](logbook-api/src/main/java/org/zalando/logbook/Strategy.java)
interface to understand the implications.

Logbook comes with some built-in strategies:

- [`BodyOnlyIfStatusAtLeastStrategy`](logbook-core/src/main/java/org/zalando/logbook/BodyOnlyIfStatusAtLeastStrategy.java)
- [`StatusAtLeastStrategy`](logbook-core/src/main/java/org/zalando/logbook/StatusAtLeastStrategy.java)
- [`WithoutBodyStrategy`](logbook-core/src/main/java/org/zalando/logbook/WithoutBodyStrategy.java)

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

| Type             | Operates on                    | Applies to | Default                                                                           |
|------------------|--------------------------------|------------|-----------------------------------------------------------------------------------|
| `QueryFilter`    | Query string                   | request    | `access_token`                                                                    |
| `PathFilter`     | Path                           | request    | n/a                                                                               |
| `HeaderFilter`   | Header (single key-value pair) | both       | `Authorization`                                                                   |
| `BodyFilter`     | Content-Type and body          | both       | json: `access_token` and `refresh_token`<br> form: `client_secret` and `password` |
| `RequestFilter`  | `HttpRequest`                  | request    | Replace binary, multipart and stream bodies.                                      |
| `ResponseFilter` | `HttpResponse`                 | response   | Replace binary, multipart and stream bodies.                                      |

`QueryFilter`, `PathFilter`, `HeaderFilter` and `BodyFilter` are relatively high-level and should cover all needs in ~90% of all
cases. For more complicated setups one should fallback to the low-level variants, i.e. `RequestFilter` and `ResponseFilter` 
respectively (in conjunction with `ForwardingHttpRequest`/`ForwardingHttpResponse`).

You can configure filters like this:

```java
import static org.zalando.logbook.HeaderFilters.authorization;
import static org.zalando.logbook.HeaderFilters.eachHeader;
import static org.zalando.logbook.QueryFilters.accessToken;
import static org.zalando.logbook.QueryFilters.replaceQuery;

Logbook logbook = Logbook.builder()
    .requestFilter(RequestFilters.replaceBody(message -> contentType("audio/*").test(message) ? "mmh mmh mmh mmh" : null))
    .responseFilter(ResponseFilters.replaceBody(message -> contentType("*/*-stream").test(message) ? "It just keeps going and going..." : null))
    .queryFilter(accessToken())
    .queryFilter(replaceQuery("password", "<secret>"))
    .headerFilter(authorization()) 
    .headerFilter(eachHeader("X-Secret"::equalsIgnoreCase, "<secret>"))
    .build();
```

You can configure as many filters as you want - they will run consecutively.

##### JsonPath body filtering (experimental)

You can apply [JSON Path](https://github.com/json-path/JsonPath) filtering to JSON bodies.
Here are some examples:

```java
import static org.zalando.logbook.json.JsonPathBodyFilters.jsonPath;
import static java.util.regex.Pattern.compile;

Logbook logbook = Logbook.builder()
        .bodyFilter(jsonPath("$.password").delete())
        .bodyFilter(jsonPath("$.active").replace("unknown"))
        .bodyFilter(jsonPath("$.address").replace("X"))
        .bodyFilter(jsonPath("$.name").replace(compile("^(\\w).+"), "$1."))
        .bodyFilter(jsonPath("$.friends.*.name").replace(compile("^(\\w).+"), "$1."))
        .bodyFilter(jsonPath("$.grades.*").replace(1.0))
        .build();
```

Take a look at the following example, before and after filtering was applied:

<details>
  <summary>Before</summary>

```json
{
  "id": 1,
  "name": "Alice",
  "password": "s3cr3t",
  "active": true,
  "address": "Anhalter Straße 17 13, 67278 Bockenheim an der Weinstraße",
  "friends": [
    {
      "id": 2,
      "name": "Bob"
    },
    {
      "id": 3,
      "name": "Charlie"
    }
  ],
  "grades": {
    "Math": 1.0,
    "English": 2.2,
    "Science": 1.9,
    "PE": 4.0
  }
}
```
</details>

<details>
  <summary>After</summary>

```json
{
  "id": 1,
  "name": "Alice",
  "active": "unknown",
  "address": "XXX",
  "friends": [
    {
      "id": 2,
      "name": "B."
    },
    {
      "id": 3,
      "name": "C."
    }
  ],
  "grades": {
    "Math": 1.0,
    "English": 1.0,
    "Science": 1.0,
    "PE": 1.0
  }
}
```
</details>

#### Correlation

Logbook uses a *correlation id* to correlate requests and responses. This allows match-related requests and responses that would usually be located in different places in the log file.

If the default implementation of the correlation id is insufficient for your use case, you may provide a custom implementation:

```java
Logbook logbook = Logbook.builder()
    .correlationId(new CustomCorrelationId())
    .build();
```

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

Requires the following dependency:

```xml
<dependency>
  <groupId>org.zalando</groupId>
  <artifactId>logbook-json</artifactId>
</dependency>
```

###### Request

```json
{
  "origin": "remote",
  "type": "request",
  "correlation": "2d66e4bc-9a0d-11e5-a84c-1f39510f0d6b",
  "protocol": "HTTP/1.1",
  "sender": "127.0.0.1",
  "method": "GET",
  "uri": "http://example.org/test",
  "host": "example.org",
  "path": "/test",
  "scheme": "http",
  "port": null,
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

##### Common Log Format

The Common Log Format ([CLF](https://httpd.apache.org/docs/trunk/logs.html#common)) is a standardized text file format used by web servers when generating server log files. The format is supported via the `CommonsLogFormatSink`:

```text
185.85.220.253 - - [02/Aug/2019:08:16:41 0000] "GET /search?q=zalando HTTP/1.1" 200 -
```

##### Extended Log Format

The Extended Log Format ([ELF](https://en.wikipedia.org/wiki/Extended_Log_Format)) is a standardised text file format, like Common Log Format (CLF), that is used by web servers when generating log files, but ELF files provide more information and flexibility. The format is supported via the `ExtendedLogFormatSink`.
Also see [W3C](https://www.w3.org/TR/WD-logfile.html) document.

Default fields:

```text
date time c-ip s-dns cs-method cs-uri-stem cs-uri-query sc-status sc-bytes cs-bytes time-taken cs-protocol cs(User-Agent) cs(Cookie) cs(Referrer)
```

Default log output example:

```text
2019-08-02 08:16:41 185.85.220.253 localhost POST /search ?q=zalando 200 21 20 0.125 HTTP/1.1 "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0" "name=value" "https://example.com/page?q=123"
```

Users may override default fields with their custom fields through the constructor of `ExtendedLogFormatSink`:

```java
new ExtendedLogFormatSink(new DefaultHttpLogWriter(), "date time cs(Custom-Request-Header) sc(Custom-Response-Header)")
```

For Http header fields: `cs(Any-Header)` and `sc(Any-Header)`, users could specify any headers they want to extract from the request.

Other supported fields are listed in the value of `ExtendedLogFormatSink.Field`, which can be put in the custom field expression.

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

##### Splunk

*Splunk* is an alternative formatting style, provided by the `SplunkHttpLogFormatter` which will render 
requests and response as key-value pairs.

###### Request

```text
origin=remote type=request correlation=2d66e4bc-9a0d-11e5-a84c-1f39510f0d6b protocol=HTTP/1.1 sender=127.0.0.1 method=POST uri=http://example.org/test host=example.org scheme=http port=null path=/test headers={Accept=[application/json], Content-Type=[text/plain]} body=Hello world!

```

###### Response

```text
origin=local type=response correlation=2d66e4bc-9a0d-11e5-a84c-1f39510f0d6b duration=25 protocol=HTTP/1.1 status=200 headers={Content-Type=[text/plain]} body=Hello world!
```

#### Writing

Writing defines where formatted requests and responses are written to. Logbook comes with three implementations: 
Logger, Stream and Chunking.

##### Logger

By default, requests and responses are logged with an *slf4j* logger that uses the `org.zalando.logbook.Logbook` category and the log level `trace`. This can be customized:

```java
Logbook logbook = Logbook.builder()
    .sink(new DefaultSink(
            new DefaultHttpLogFormatter(),
            new DefaultHttpLogWriter()
    ))
    .build();
```

##### Stream

An alternative implementation is to log requests and responses to a `PrintStream`, e.g. `System.out` or `System.err`. This is usually a bad choice for running in production, but can sometimes be useful for short-term local development and/or investigation.

```java
Logbook logbook = Logbook.builder()
    .sink(new DefaultSink(
            new DefaultHttpLogFormatter(),
            new StreamHttpLogWriter(System.err)
    ))
    .build();
```

##### Chunking

The `ChunkingSink` will split long messages into smaller chunks and will write them individually while delegating to another sink:

```java
Logbook logbook = Logbook.builder()
    .sink(new ChunkingSink(sink, 1000))
    .build();

```

#### Sink

The combination of `HttpLogFormatter` and `HttpLogWriter` suits most use cases well, but it has limitations.
Implementing the `Sink` interface directly allows for more sophisticated use cases, e.g. writing requests/responses
to a structured persistent storage like a database.

Multiple sinks can be combined into one using the `CompositeSink`.

### Servlet

You’ll have to register the `LogbookFilter` as a `Filter` in your filter chain — either in your `web.xml` file (please note that the xml approach will use all the defaults and is not configurable):

```xml
<filter>
    <filter-name>LogbookFilter</filter-name>
    <filter-class>org.zalando.logbook.servlet.LogbookFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>LogbookFilter</filter-name>
    <url-pattern>/*</url-pattern>
    <dispatcher>REQUEST</dispatcher>
    <dispatcher>ASYNC</dispatcher>
</filter-mapping>
```

or programmatically, via the `ServletContext`:

```java
context.addFilter("LogbookFilter", new LogbookFilter(logbook))
    .addMappingForUrlPatterns(EnumSet.of(REQUEST, ASYNC), true, "/*"); 
```

**Beware**: The `ERROR` dispatch is not supported. You're strongly advised to produce error responses within the
`REQUEST` or `ASNYC` dispatch.

The `LogbookFilter` will, by default, treat requests with a `application/x-www-form-urlencoded` body not different from
any other request, i.e you will see the request body in the logs. The downside of this approach is that you won't be
able to use any of the `HttpServletRequest.getParameter*(..)` methods. See issue [#94](../../issues/94) for some more
details.

#### Form Requests

As of Logbook 1.5.0, you can now specify one of three strategies that define how Logbook deals with this situation by
using the `logbook.servlet.form-request` system property:

| Value            | Pros                                                                              | Cons                                               |
|------------------|-----------------------------------------------------------------------------------|----------------------------------------------------|
| `body` (default) | Body is logged                                                                    | Downstream code can **not use `getParameter*()`**  |
| `parameter`      | Body is logged (but it's reconstructed from parameters)                           | Downstream code can **not use `getInputStream()`** |
| `off`            | Downstream code can decide whether to use `getInputStream()` or `getParameter*()` | Body is **not logged**                             |

#### Security

Secure applications usually need a slightly different setup. You should generally avoid logging unauthorized requests, especially the body, because it quickly allows attackers to flood your logfile — and, consequently, your precious disk space. Assuming that your application handles authorization inside another filter, you have two choices:

- Don't log unauthorized requests
- Log unauthorized requests without the request body

You can easily achieve the former setup by placing the `LogbookFilter` after your security filter. The latter is a little bit more sophisticated. You’ll need two `LogbookFilter` instances — one before your security filter, and one after it:

```java
context.addFilter("SecureLogbookFilter", new SecureLogbookFilter(logbook))
    .addMappingForUrlPatterns(EnumSet.of(REQUEST, ASYNC), true, "/*");
context.addFilter("securityFilter", new SecurityFilter())
    .addMappingForUrlPatterns(EnumSet.of(REQUEST), true, "/*");
context.addFilter("LogbookFilter", new LogbookFilter(logbook))
    .addMappingForUrlPatterns(EnumSet.of(REQUEST, ASYNC), true, "/*");
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

### HTTP Client 5

The `logbook-httpclient5` module contains an `ExecHandler` to use with the `HttpClient`:
```java
CloseableHttpClient client = HttpClientBuilder.create()
        .addExecInterceptorFirst("Logbook", new LogbookHttpExecHandler(logbook))
        .build();
```
The Handler should be added first, such that a compression is performed after logging and decompression is performed before logging.

To avoid a breaking change, there is also an `HttpRequestInterceptor` and an `HttpResponseInterceptor` to use with the `HttpClient`, which works fine as long as compression (or other ExecHandlers) is not used:

```java
CloseableHttpClient client = HttpClientBuilder.create()
        .addRequestInterceptorFirst(new LogbookHttpRequestInterceptor(logbook))
        .addResponseInterceptorFirst(new LogbookHttpResponseInterceptor())
        .build();
```

Since the `LogbookHttpResponseInterceptor` is incompatible with the `HttpAsyncClient` there is another way to log responses:

```java
CloseableHttpAsyncClient client = HttpAsyncClientBuilder.create()
        .addRequestInterceptorFirst(new LogbookHttpRequestInterceptor(logbook))
        .build();
        
// and then wrap your response consumer
client.execute(producer, new LogbookHttpAsyncResponseConsumer<>(consumer), callback)
```

### JAX-RS

The `logbook-jaxrs` module contains:

 A `LogbookClientFilter` to be used for applications making HTTP requests
 
```java
client.register(new LogbookClientFilter(logbook));
```
   
 A `LogbookServerFilter` for be used with HTTP servers
   
```java
resourceConfig.register(new LogbookServerFilter(logbook));
```

### JDK HTTP Server

The `logbook-jdkserver` module provides support for
[JDK HTTP server](https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html)
and contains:

A `LogbookFilter` to be used with the builtin server

```java
httpServer.createContext(path, handler).getFilters().add(new LogbookFilter(logbook))
```

### Netty

The `logbook-netty` module contains:

A `LogbookClientHandler` to be used with an `HttpClient`:

```java
HttpClient httpClient =
        HttpClient.create()
                .doOnConnected(
                        (connection -> connection.addHandlerLast(new LogbookClientHandler(logbook)))
                );
```

A `LogbookServerHandler` for use used with an `HttpServer`:

```java
HttpServer httpServer =
        HttpServer.create()
                .doOnConnection(
                        connection -> connection.addHandlerLast(new LogbookServerHandler(logbook))
                );
```

#### Spring WebFlux

Users of Spring WebFlux can pick any of the following options:
 
- Programmatically create a `NettyWebServer` (passing an `HttpServer`)
- Register a custom `NettyServerCustomizer`
- Programmatically create a `ReactorClientHttpConnector` (passing an `HttpClient`)
- Register a custom `WebClientCustomizer`
- Use separate connector-independent module `logbook-spring-webflux`

#### Micronaut

Users of Micronaut can follow the [official docs](https://docs.micronaut.io/snapshot/guide/index.html#nettyPipeline) on how to integrate Logbook with Micronaut.

:warning: Even though Quarkus and Vert.x use Netty under the hood, unfortunately neither of them allows accessing or customizing it (yet).

### OkHttp v2.x

The `logbook-okhttp2` module contains an `Interceptor` to use with version 2.x of the `OkHttpClient`:

```java
OkHttpClient client = new OkHttpClient();
client.networkInterceptors().add(new LogbookInterceptor(logbook));
```

If you're expecting gzip-compressed responses you need to register our `GzipInterceptor` in addition.
The transparent gzip support built into OkHttp will run after any network interceptor which forces
logbook to log compressed binary responses.

```java
OkHttpClient client = new OkHttpClient();
client.networkInterceptors().add(new LogbookInterceptor(logbook));
client.networkInterceptors().add(new GzipInterceptor());
```

### OkHttp v3.x

The `logbook-okhttp` module contains an `Interceptor` to use with version 3.x of the `OkHttpClient`:

```java
OkHttpClient client = new OkHttpClient.Builder()
        .addNetworkInterceptor(new LogbookInterceptor(logbook))
        .build();
```

If you're expecting gzip-compressed responses you need to register our `GzipInterceptor` in addition.
The transparent gzip support built into OkHttp will run after any network interceptor which forces
logbook to log compressed binary responses.

```java
OkHttpClient client = new OkHttpClient.Builder()
        .addNetworkInterceptor(new LogbookInterceptor(logbook))
        .addNetworkInterceptor(new GzipInterceptor())
        .build();
```

### Ktor

The `logbook-ktor-client` module contains:

A `LogbookClient` to be used with an `HttpClient`:

```kotlin
private val client = HttpClient(CIO) {
    install(LogbookClient) {
        logbook = logbook
    }
}
```

The `logbook-ktor-server` module contains:

A `LogbookServer` to be used with an `Application`:

```kotlin
private val server = embeddedServer(CIO) {
    install(LogbookServer) {
        logbook = logbook
    }
}
```

Alternatively, you can use `logbook-ktor`, which ships both `logbook-ktor-client` and `logbook-ktor-server` modules.

### Spring
The `logbook-spring` module contains a `ClientHttpRequestInterceptor` to use with `RestTemplate`:

```java
    LogbookClientHttpRequestInterceptor interceptor = new LogbookClientHttpRequestInterceptor(logbook);
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add(interceptor);
```

### Spring Boot Starter

Logbook comes with a convenient auto configuration for Spring Boot users. It sets up all of the following parts automatically with sensible defaults:
    
- Servlet filter
- Second Servlet filter for unauthorized requests (if Spring Security is detected)
- Header-/Parameter-/Body-Filters
- HTTP-/JSON-style formatter
- Logging writer

Instead of declaring a dependency to `logbook-core` declare one to the Spring Boot Starter:

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-spring-boot-starter</artifactId>
    <version>${logbook.version}</version>
</dependency>
```

Every bean can be overridden and customized if needed, e.g. like this:

```java
@Bean
public BodyFilter bodyFilter() {
    return merge(
            defaultValue(), 
            replaceJsonStringProperty(singleton("secret"), "XXX"));
}
```

Please refer to [`LogbookAutoConfiguration`](logbook-spring-boot-autoconfigure/src/main/java/org/zalando/logbook/autoconfigure/LogbookAutoConfiguration.java)
or the following table to see a list of possible integration points:

| Type                     | Name                  | Default                                                                   |
|--------------------------|-----------------------|---------------------------------------------------------------------------|
| `FilterRegistrationBean` | `secureLogbookFilter` | Based on `LogbookFilter`                                                  |
| `FilterRegistrationBean` | `logbookFilter`       | Based on `LogbookFilter`                                                  |
| `Logbook`                |                       | Based on condition, filters, formatter and writer                         |
| `Predicate<HttpRequest>` | `requestCondition`    | No filter; is later combined with `logbook.exclude` and `logbook.exclude` |
| `HeaderFilter`           |                       | Based on `logbook.obfuscate.headers`                                      |
| `PathFilter`             |                       | Based on `logbook.obfuscate.paths`                                   |
| `QueryFilter`            |                       | Based on `logbook.obfuscate.parameters`                                   |
| `BodyFilter`             |                       | `BodyFilters.defaultValue()`, see [filtering](#filtering)                 |
| `RequestFilter`          |                       | `RequestFilters.defaultValue()`, see [filtering](#filtering)              |
| `ResponseFilter`         |                       | `ResponseFilters.defaultValue()`, see [filtering](#filtering)             |
| `Strategy`               |                       | `DefaultStrategy`                                                         |
| `Sink`                   |                       | `DefaultSink`                                                             |
| `HttpLogFormatter`       |                       | `JsonHttpLogFormatter`                                                    |
| `HttpLogWriter`          |                       | `DefaultHttpLogWriter`                                                    |

Multiple filters are merged into one.

#### Autoconfigured beans from `logbook-spring`
Some classes from `logbook-spring` are included in the auto configuration. 

You can autowire `LogbookClientHttpRequestInterceptor` with code like:
```java
private final RestTemplate restTemplate;
MyClient(RestTemplateBuilder builder, LogbookClientHttpRequestInterceptor interceptor){
  this.restTemplate = builder
    .additionalInterceptors(interceptor)
    .build();
}
```

#### Configuration

The following tables show the available configuration:

| Configuration                      | Description                                                                                          | Default                       |
|------------------------------------|------------------------------------------------------------------------------------------------------|-------------------------------|
| `logbook.include`                  | Include only certain URLs (if defined)                                                               | `[]`                          |
| `logbook.exclude`                  | Exclude certain URLs (overrides `logbook.include`)                                                   | `[]`                          |
| `logbook.filter.enabled`           | Enable the [`LogbookFilter`](#servlet)                                                               | `true`                        |
| `logbook.filter.form-request-mode` | Determines how [form requests](#form-requests) are handled                                           | `body`                        |
| `logbook.secure-filter.enabled`    | Enable the [`SecureLogbookFilter`](#servlet)                                                         | `true`                        |
| `logbook.format.style`             | [Formatting style](#formatting) (`http`, `json`, `curl` or `splunk`)                                 | `json`                        |
| `logbook.strategy`                 | [Strategy](#strategy) (`default`, `status-at-least`, `body-only-if-status-at-least`, `without-body`) | `default`                     |
| `logbook.minimum-status`           | Minimum status to enable logging (`status-at-least` and `body-only-if-status-at-least`)              | `400`                         |
| `logbook.obfuscate.headers`        | List of header names that need obfuscation                                                           | `[Authorization]`             |
| `logbook.obfuscate.paths`          | List of paths that need obfuscation. Check [Filtering](#filtering) for syntax.                       | `[]`                          |
| `logbook.obfuscate.parameters`     | List of parameter names that need obfuscation                                                        | `[access_token]`              |
| `logbook.write.chunk-size`         | Splits log lines into smaller chunks of size up-to `chunk-size`.                                     | `0` (disabled)                |
| `logbook.write.max-body-size`      | Truncates the body up to `max-body-size` and appends `...`.                                          | `-1` (disabled)               |

##### Example configuration

```yaml
logbook:
  include:
    - /api/**
    - /actuator/**
  exclude:
    - /actuator/health
    - /api/admin/**
  filter.enabled: true
  secure-filter.enabled: true
  format.style: http
  strategy: body-only-if-status-at-least
  minimum-status: 400
  obfuscate:
    headers:
      - Authorization
      - X-Secret
    parameters:
      - access_token
      - password
  write:
    chunk-size: 1000
```

### logstash-logback-encoder
For basic Logback configuraton

```
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>
```

configure Logbook with a `LogstashLogbackSink`

```
HttpLogFormatter formatter = new JsonHttpLogFormatter();
LogstashLogbackSink sink = new LogstashLogbackSink(formatter);
```

for outputs like
```
{
  "@timestamp" : "2019-03-08T09:37:46.239+01:00",
  "@version" : "1",
  "message" : "GET http://localhost/test?limit=1",
  "logger_name" : "org.zalando.logbook.Logbook",
  "thread_name" : "main",
  "level" : "TRACE",
  "level_value" : 5000,
  "http" : {
     // logbook request/response contents
  }
}
```

## Known Issues

1. The Logbook Servlet Filter interferes with downstream code using `getWriter` and/or `getParameter*()`. See [Servlet](#servlet) for more details.
2. The Logbook Servlet Filter does **NOT** support `ERROR` dispatch. You're strongly encouraged to not use it to produce error responses.
2. The Logbook HTTP Client integration is handling gzip-compressed response entities incorrectly if the interceptor runs before a decompressing interceptor. Since logging compressed contents is not really helpful it's advised to register the logbook interceptor as the last interceptor in the chain.

## Getting Help with Logbook

If you have questions, concerns, bug reports, etc., please file an issue in this repository's [Issue Tracker](https://github.com/zalando/logbook/issues).

## Getting Involved/Contributing

To contribute, simply make a pull request and add a brief description (1-2 sentences) of your addition or change. For
more details, check the [contribution guidelines](.github/CONTRIBUTING.md).

## Alternatives

- [Apache HttpClient Wire Logging](http://hc.apache.org/httpcomponents-client-4.5.x/logging.html)
  - Client-side only
  - Apache HttpClient exclusive
  - Support for HTTP bodies
- [Spring Boot Access Logging](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-configure-accesslogs)
  - Spring application only
  - Server-side only
  - Tomcat/Undertow/Jetty exclusive
  - **No** support for HTTP bodies
- [Tomcat Request Dumper Filter](https://tomcat.apache.org/tomcat-7.0-doc/config/filter.html#Request_Dumper_Filter)
  - Server-side only
  - Tomcat exclusive
  - **No** support for HTTP bodies
- [logback-access](http://logback.qos.ch/access.html)
  - Server-side only
  - Any servlet container
  - Support for HTTP bodies

## Credits and References

![Creative Commons (Attribution-Share Alike 3.0 Unported](https://licensebuttons.net/l/by-sa/3.0/80x15.png)
[*Grand Turk, a replica of a three-masted 6th rate frigate from Nelson's days - logbook and charts*](https://commons.wikimedia.org/wiki/File:Grand_Turk(34).jpg)
by [JoJan](https://commons.wikimedia.org/wiki/User:JoJan) is licensed under a
[Creative Commons (Attribution-Share Alike 3.0 Unported)](http://creativecommons.org/licenses/by-sa/3.0/).
