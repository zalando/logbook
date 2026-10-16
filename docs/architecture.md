# Contributor architecture guide

Logbook is a JVM library that observes HTTP traffic, filters the logged representation, and
delegates output to logging backends. Use this guide to locate a change and follow its effects
through HTTP logging.
It describes the current implementation, not a framework support matrix or a guarantee that every
adapter has the same lifecycle. For contribution rules and validation commands, see
[CONTRIBUTING](../.github/CONTRIBUTING.md); for installation and configuration, see the
[README](../README.md).

## Where a change belongs

The main boundary is between the HTTP logging contracts, their default implementation, framework
adapters, and application wiring. Choose the narrowest owner of the behavior being changed:

| Area | Modules | Ownership and change boundary |
|---|---|---|
| Contracts | [logbook-api](../logbook-api/) | HTTP model, filters, strategies, sinks, attributes, and staged lifecycle; keep framework implementation details outside these public contracts |
| Default implementation | [logbook-core](../logbook-core/) | Shared logging policy, filter composition, strategies, correlation, formatters, sinks, and attribute extractors; not container lifecycle handling |
| Matching | [logbook-common](../logbook-common/) and its callers in core | Path and media-type matching semantics and input-dependent cost |
| JSON handling | [logbook-json](../logbook-json/), [logbook-json-jackson2](../logbook-json-jackson2/) | Formatting and body filtering through different Jackson APIs; check behavior in both implementations |
| Server and dual client/server adapters | [logbook-servlet](../logbook-servlet/), [logbook-jaxrs](../logbook-jaxrs/), [logbook-jdkserver](../logbook-jdkserver/), [logbook-netty](../logbook-netty/) | Framework-specific streams, callbacks, channels, and body replay; not every module provides both client and server hooks |
| Client adapters | [logbook-httpclient](../logbook-httpclient/), [logbook-httpclient5](../logbook-httpclient5/), [logbook-okhttp](../logbook-okhttp/), [logbook-okhttp2](../logbook-okhttp2/), [logbook-openfeign](../logbook-openfeign/) | Host-client interception, entity ownership, and response consumption |
| Spring adapters | [logbook-spring](../logbook-spring/), [logbook-spring-webflux](../logbook-spring-webflux/) | Blocking and reactive Spring request/response handling, separately from Boot wiring |
| Boot and ECS wiring | [base auto-configuration](../logbook-spring-boot-autoconfigure/), [reactive auto-configuration](../logbook-spring-boot-webflux-autoconfigure/), [ECS auto-configuration](../logbook-spring-boot-ecs-autoconfigure/) | Properties, default beans, optional classpaths, user overrides, and ECS structured output |
| Starter | [logbook-spring-boot-starter](../logbook-spring-boot-starter/) | Dependency convenience, not HTTP capture implementation |
| Logstash output | [logbook-logstash](../logbook-logstash/) | Logback/logstash encoder integration, not HTTP capture |
| Ktor family | [logbook-ktor/](../logbook-ktor/) | Nested common, client, server, and convenience modules; plugin/channel lifecycle belongs in the client or server module |
| Test support | [test-modules/](../test-modules/) | Shared fixtures and backend integration tests, not runtime features |
| Benchmarks | [logbook-jmh](../logbook-jmh/) | Performance workloads rather than production logging behavior |
| Build and alignment | [root POM](../pom.xml), [logbook-parent](../logbook-parent/), [logbook-bom](../logbook-bom/) | Reactor aggregation, inherited build/dependency configuration, and consumer artifact alignment |

Adapters depend on the API and their host libraries; some also depend on core. Do not infer that
every adapter is wired by the Spring Boot starter. The [starter dependencies](../logbook-spring-boot-starter/pom.xml)
include base and ECS auto-configuration, while reactive auto-configuration is a separate module.

For dependency changes, read the module POM together with inherited dependency management in
[logbook-parent](../logbook-parent/pom.xml) and [logbook-bom](../logbook-bom/pom.xml).
An omitted scope can be managed: for example, `logbook-test` is normally test-scoped.
The BOM aligns consumer dependencies; it is not the reactor or a runtime bundle.
The [root POM](../pom.xml) and nested aggregators define the build, with JMH added by the parent's
default profile. The [Ktor aggregator](../logbook-ktor/pom.xml) is distinct from the
[Ktor convenience artifact](../logbook-ktor/logbook-ktor/pom.xml), which depends on client and server
plugins.

## Extension and compatibility boundaries

Prefer composing existing filters, strategies, sinks, and attribute extractors to opening
implementation classes for inheritance. Public compatibility includes interface/default methods,
Lombok-generated constructors and builders, configuration defaults, and log output consumed by
other systems. Inspect the relevant `@API` lifecycle markers and keep Java APIs and language features
compatible with the module's compiler settings. Discuss new public APIs, modules, production
dependencies, or changed defaults with maintainers before expanding the implementation.

Keep framework-specific capture behavior in its adapter and preserve the application's body,
stream, and asynchronous lifecycle. Keep optional feature dependencies from becoming requirements
for unrelated consumers. Preserve redaction defaults; dependency presence alone is not a reason to
enable logging of identities, credentials, or other sensitive data. The sections below explain the
implementation details and limitations behind these boundaries.

## Construction and discovery

[`Logbook.builder()`](../logbook-api/src/main/java/org/zalando/logbook/Logbook.java) creates the
Lombok-generated builder in [`LogbookCreator`](../logbook-api/src/main/java/org/zalando/logbook/LogbookCreator.java).
It merges supplied filter lists and delegates construction to
[`LogbookFactory`](../logbook-api/src/main/java/org/zalando/logbook/LogbookFactory.java).
Factories are discovered through `ServiceLoader`; the highest-priority provider is selected.
Core registers [`DefaultLogbookFactory`](../logbook-core/src/main/java/org/zalando/logbook/core/DefaultLogbookFactory.java)
through its [`META-INF/services` descriptor](../logbook-core/src/main/resources/META-INF/services/org.zalando.logbook.LogbookFactory).
The API's fallback factory reports the missing implementation rather than providing a logging engine.

Default filters also use [service discovery](../logbook-core/src/main/java/org/zalando/logbook/core/DefaultFilters.java).
For example, the JSON modules contribute body filters, so defaults depend on the available providers.
With the direct builder, supplied filters replace the corresponding factory fallbacks rather than
automatically extending them. Trace the builder and factory composition before changing default
obfuscation.

Spring Boot's [`LogbookAutoConfiguration`](../logbook-spring-boot-autoconfigure/src/main/java/org/zalando/logbook/autoconfigure/LogbookAutoConfiguration.java)
assembles the same engine from properties and beans. This is separate from capture in a servlet
filter or client interceptor. Its default body-filter bean, when enabled, is collected alongside
custom body-filter beans. A user `HeaderFilter` bean instead prevents the default header-filter bean
from being created. Check each bean's conditions rather than generalizing one filter type's override
behavior to all of them. Keep optional-class references inside the appropriate conditional configuration.

## Follow an HTTP exchange

The usual staged API sequence is:

```text
adapter: process(request) -> requestStage.write()
         -> responseProcessingStage.process(response) -> responseWritingStage.write()
```

The adapter schedules these calls around its framework's lifecycle. Core does not perform the
HTTP exchange. In [`DefaultLogbook`](../logbook-core/src/main/java/org/zalando/logbook/core/DefaultLogbook.java):

1. **Check activation and condition.** `process(request)` first checks `sink.isActive()`, then
   evaluates the predicate against the original request. An inactive sink or excluded request
   returns a no-op stage before core's request preparation.
2. **Prepare the request.** Core creates correlation metadata, calls `Strategy.process(request)`,
   extracts attributes from that processed request, attaches them through a header-caching wrapper,
   and applies request filtering. It retains both the processed and filtered request views.
3. **Write or defer the request.** `requestStage.write()` calls the strategy with the filtered
   request and the sink. The default writes immediately; another strategy can defer or omit it.
4. **Prepare the response.** `responseProcessingStage.process(response)` calls the strategy with the
   filtered request and original response, and returns the response writing stage.
5. **Write the response.** That stage extracts attributes using the retained processed request and
   processed response, applies response filtering, creates the completed correlation, and delegates
   to the strategy and sink.

[`Strategy`](../logbook-api/src/main/java/org/zalando/logbook/Strategy.java) controls body selection
and write timing; its default processing methods call `withBody()`.
[`Sink`](../logbook-api/src/main/java/org/zalando/logbook/Sink.java) controls delivery.
[`DefaultSink`](../logbook-core/src/main/java/org/zalando/logbook/core/DefaultSink.java) combines a
formatter with a writer. A custom sink can consume structured request/response data directly rather
than forcing every destination through a formatted string. Correlation links an exchange's log
records; it does not implement distributed trace propagation.

Core checks sink activation when preparing the request, not afresh at every write. Its no-op path
does not prove that an adapter or the host client has performed no body work.

Exception handling is also bounded. Core skips logging after runtime failures in request
preparation, request writing, or response writing, and substitutes empty attributes when extraction
fails. The activation/predicate and response-strategy processing calls are outside those runtime
handlers; declared `IOException`s can propagate. Inspect the adapter's handling too, rather than
assuming every logging failure is isolated from application execution.

## Body ownership and privacy

[`HttpRequest`](../logbook-api/src/main/java/org/zalando/logbook/HttpRequest.java) and
[`HttpResponse`](../logbook-api/src/main/java/org/zalando/logbook/HttpResponse.java) expose `withBody()`
and `withoutBody()` for logging. These calls do not mean "read lazily" or "free all buffered bytes"
on every adapter. An adapter may capture eagerly, copy data as it passes, or buffer and replay it.
Preserve what the application can read and who owns closing or releasing the original resources.

[`FilteredHttpRequest`](../logbook-core/src/main/java/org/zalando/logbook/core/FilteredHttpRequest.java)
and [`FilteredHttpResponse`](../logbook-core/src/main/java/org/zalando/logbook/core/FilteredHttpResponse.java)
transform the log view. Header/path/query filtering and body-string filtering occur at different
points; body filters run when that view's body is read. The core caching wrappers cache headers and
attach attributes, not the underlying transport body.

Two distinctions are especially important when extending this flow:

- **Truncation is not a capture limit.** [`BodyFilters.truncate`](../logbook-core/src/main/java/org/zalando/logbook/core/BodyFilters.java)
  shortens an already available string. Boot's `logbook.write.max-body-size` applies this output
  filter after the other body filters; it does not cap captured bytes or heap use. Keep redaction
  before truncation. The [configuration documentation](../README.md#configuration) records the
  output-size behavior.
- **Attributes are a separate privacy surface.** Extraction precedes the corresponding request or
  response filtering. Masking `Authorization` or a body field does not automatically mask an
  attribute extracted from it. Review the attributes a custom extractor exposes. The
  [JWT extractor](../logbook-core/src/main/java/org/zalando/logbook/core/attributes/JwtClaimsExtractor.java)
  decodes claims for logging; it does not authenticate the token or verify its signature.

Default filters cover selected sensitive fields and replace certain binary, multipart, or streaming
bodies with markers. They are not a guarantee that arbitrary payloads or attributes are safe to log.
An output marker is not proof that no capture or framework-level buffering occurred.

## Adapter lifecycle hotspots

### Servlet: request reads and response completion differ

[`RemoteRequest.withBody()`](../logbook-servlet/src/main/java/org/zalando/logbook/servlet/RemoteRequest.java)
eagerly buffers an ordinary request body. The response wrapper instead installs a tee when its
writer or output stream is used: bytes are forwarded to the real response and copied for logging.
Do not apply a request-side buffering fix to the response without checking both lifecycles.

The distinction between the filtered log view and adapter state matters: directly calling
`withoutBody()` on a buffered `RemoteRequest` can make its servlet input stream empty. Preserve the
state-transition and downstream-read behavior exercised by
[`RemoteRequestTest`](../logbook-servlet/src/test/java/org/zalando/logbook/servlet/RemoteRequestTest.java).
Form handling chooses raw-body capture, parameter-based reconstruction, or pass-through; reconstruction
is not a byte-for-byte copy. Multipart requests bypass capture. The
[servlet documentation](../README.md#servlet) describes form-access limitations and the unsupported
`ERROR` dispatch.

[`LogbookFilter`](../logbook-servlet/src/main/java/org/zalando/logbook/servlet/LogbookFilter.java)
retains a processing stage across async dispatch and logs the response on completion. The
[`AsyncOnCompleteListenerWrapper`](../logbook-servlet/src/main/java/org/zalando/logbook/servlet/AsyncOnCompleteListenerWrapper.java)
is an extension point for restoring context around that callback; its identity default does not
propagate thread-local context. Capture the needed context when wrapping on the request thread,
not after control has moved to the completion thread. The
[async dispatch tests](../logbook-servlet/src/test/java/org/zalando/logbook/servlet/AsyncDispatchTest.java)
cover completion behavior. Do not generalize it to every error or timeout path.

### Netty: preserve channel state and buffer ownership

The handlers forward original messages and copy selected body chunks.
[`Buffering`](../logbook-netty/src/main/java/org/zalando/logbook/netty/Buffering.java) restores the
incoming buffer's reader index after copying. Handler instances hold mutable state; do not share
one across channels. In the
[`server handler`](../logbook-netty/src/main/java/org/zalando/logbook/netty/LogbookServerHandler.java),
`LastHttpContent` schedules log writes, with sequencing to keep request before response. This is
not an acknowledgement of successful socket delivery. Preserve buffer lifetime and message ordering
when changing callbacks or completion handling.

### WebFlux: aggregation and replay are not transparent streaming

The decorators capture selected bodies through
[`DataBufferCopyUtils`](../logbook-spring-webflux/src/main/java/org/zalando/logbook/spring/webflux/DataBufferCopyUtils.java),
which joins a publisher, reads and releases the aggregate, and supplies replacement data.
Changes must preserve both release and replacement ownership. The response and client-request
decorators override `writeWith`, not `writeAndFlushWith`: do not infer unbounded-stream, SSE, or
flush-boundary support from tests of finite bodies.

[`LogbookExchangeFilterFunction`](../logbook-spring-webflux/src/main/java/org/zalando/logbook/spring/webflux/LogbookExchangeFilterFunction.java)
can consume a response and rebuild its body from captured bytes. Replaying it must not subscribe
again to the original one-shot body. The
[exchange-filter tests](../logbook-spring-webflux/src/test/java/org/zalando/logbook/spring/webflux/LogbookExchangeFilterFunctionTest.java)
cover empty and nonempty replay.

The [reactive Boot configuration](../logbook-spring-boot-webflux-autoconfigure/src/main/java/org/zalando/logbook/autoconfigure/webflux/LogbookWebFluxAutoConfiguration.java)
selects transport-handler or WebFlux-filter server logging. That placement affects when tracing
context is visible. See [reactive server mode](../README.md#reactive-server-mode) for context-bridging
requirements and error-response limitations. This server-side choice does not establish automatic
context propagation for outgoing Netty client logs.

### Ktor: keep replay and coroutine context separate

The [server plugin](../logbook-ktor/logbook-ktor-server/src/main/kotlin/org/zalando/logbook/server/LogbookServer.kt)
uses `DoubleReceive` for request access and captures response bytes when the response is
`ByteArrayContent`. Do not assume arbitrary response types have the same capture path.

The [client plugin](../logbook-ktor/logbook-ktor-client/src/main/kotlin/org/zalando/logbook/client/LogbookClient.kt)
reads response bytes before deciding whether to record the response body, then provides replacement
content to the caller. Response logging runs in a launched coroutine with the caller's supplied
context elements except `Job`; it does not create MDC or tracing context itself. Preserve context,
coroutine ownership, and replay independently. The
[MDC test](../logbook-ktor/logbook-ktor-client/src/test/kotlin/org/zalando/logbook/client/LogbookClientMdcTest.kt)
explicitly supplies `MDCContext` rather than proving automatic MDC propagation.

## Choose tests at the boundary

For shared engine changes, start with
[`DefaultLogbookTest`](../logbook-core/src/test/java/org/zalando/logbook/core/DefaultLogbookTest.java)
and the affected filter/strategy tests. For transport changes, use the adapter's tests to verify the
application still receives its data and that logging occurs at the correct lifecycle point.
For configuration changes, test actual bean selection, user overrides, and absent optional classes,
not only direct calls to configuration methods. The [base auto-configuration tests](../logbook-spring-boot-autoconfigure/src/test/java/org/zalando/logbook/autoconfigure/)
provide examples.

Keep expected behavior and current limitations distinct when adding a regression test or extending
an adapter. A fix in one adapter is not evidence that the others have the same buffering or context
behavior. Use the contribution guide for validation commands rather than treating this overview as
a complete test plan.
