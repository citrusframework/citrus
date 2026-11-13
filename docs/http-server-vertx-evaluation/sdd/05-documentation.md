# Using and reviewing the corrected HTTP evaluation

14 October 2026. This is test-only evaluation tooling for Citrus #1808. It does not add a public
HTTP endpoint or change existing Jetty configuration. The maintainer-facing conclusion and final
verification results are in [the decision report](../decision-report.md).

## What changed

The prototype now reserves an exchange slot before reading an upload, checks the body limit as
chunks arrive, and closes rejected or timed-out uploads. Each start owns its listener, Vert.x
runtime, worker executor, adapter, configuration snapshot and pending exchanges. Stop cancels
pending work, wakes the default Citrus reply waiters and waits for owned resources within one
shutdown budget. A custom adapter that ignores interruption causes a reported shutdown failure;
a new generation cannot start while its worker survives.

Response conversion failures produce an empty 500 and remain available through `getFailures()`.
The mapper preserves cookie security and lifetime attributes, removes conflicting transfer
framing, and follows the existing controller's path normalization. Configuration supplied through
`getHttpEndpointConfiguration()` is captured at start and is no longer overwritten by defaults.
Repeated start is harmless; destroy clears the running flag.

The implementation lives under `endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/`.
`VertxHttpServer` owns lifecycle and exchanges; `VertxExchangeExecutor` bounds blocking adapter
work; `VertxHttpMessageMapper` translates messages; `VertxHttpServerOptions` validates settings.
Audit regression tests and `VertxHttpContractIT` exercise real sockets. The existing
`integration/VertxHttpServerRoundtripIT` now shares the module's Spring test context and verifies
Citrus actions and default-adapter reply correlation. This context change fixed the duplicate
port-11080 listener that had blocked the original full-suite attempt.

`FootprintProbe`, its statistics test, and `src/test/resources/vertx-evaluation/` provide the
measurement harness. The Python tests exercise real child failure and timeout handling. Maven's
only dependency addition is the managed `vertx-core` artifact in test scope. There is no production
changelog or manual change because there is no new production behavior.

## Ordinary HTTP: retain production configuration or opt into a local experiment

Existing code remains valid:

```java
HttpServer server = new HttpServer();
server.setPort(8080);
server.setDefaultTimeout(5000L);
server.initialize();
server.start();
// Existing http().server(server).receive()/send() actions continue to use Jetty.
// Stop and destroy the server in the test fixture's cleanup hook.
```

Inside this module's test sources, the equivalent bounded experiment is:

```java
VertxHttpServer server = new VertxHttpServer();
server.setPort(0); // The OS selects an available loopback port.
server.setReplyTimeoutMs(5000L);
server.setMaxConcurrentExchanges(32);
server.setMaxBodySizeBytes(1024 * 1024);
server.getHttpEndpointConfiguration().setCharset("UTF-8");
server.initialize();
server.start();
try {
    int port = server.getActualPort();
    // Point the HTTP client at this port; existing Citrus server actions accept this Endpoint.
} finally {
    server.stop();
    server.destroy();
}
```

This is a Java test fixture, not an XML/YAML/Groovy engine-selection option. Configuration changes
made while running apply on the next start. A configuration object or custom converter supplied
by a caller must not be mutated concurrently with active exchanges. TRACE is an explicit compatibility exception: the existing Jetty Servlet layer echoes the request
without invoking the Citrus adapter; the prototype routes it through Citrus actions. Keep text payloads as strings
and binary payloads as byte arrays with a configured binary content type; arbitrary Spring MVC
payload coercion is outside the prototype's supported subset.

## HTTPS on the Vert.x prototype

Before start, call `server.setTls(new VertxHttpTls(Path.of(keyStorePath), keyStorePassword))`
with a PKCS12 private-key/certificate store. TLS 1.2 and TLS 1.3 use the JDK engine; the HTTP
protocol stays HTTP/1.1. Add `.withClientAuthentication(Path.of(trustStorePath), trustStorePassword)`
to require a trusted client certificate. Clients must trust the server certificate and verify
its hostname. Configuration and certificate changes take effect after restart. `setTls(null)`
selects plain HTTP on the next start. See [HTTPS configuration and verification](../tls.md).

## Custom Servlets remain on Jetty

With an existing `jakarta.servlet.http.HttpServlet` class named `MyServlet`:

```java
var handlers = new org.eclipse.jetty.ee11.servlet.ServletHandler();
handlers.addServletWithMapping(MyServlet.class, "/custom/*");
HttpServer server = new HttpServer();
server.setPort(8080);
server.setServletHandler(handlers);
server.initialize();
server.start();
```

Supplying a custom Servlet handler replaces the default dispatcher setup, as it does today.
Retain the existing dispatcher configuration when the application needs Citrus HTTP controller
routes as well. The prototype cannot translate Servlet handlers, filters, Spring web contexts,
interceptors or authentication policies. SOAP and WebSocket retain their existing Jetty
implementations alongside the test-only Vert.x exchanges. #1807 is the related WebSocket initiative; SOAP is separate.

## Verification and measurement

From the worktree root, select JDK 21. The compiler still targets Java 17.

```sh
export JAVA_HOME=/absolute/path/to/jdk-21
./mvnw verify -pl endpoints/citrus-http \
  '-Dtest=Vertx*Test,FootprintProbeTest' '-Dit.test=Vertx*IT'
./mvnw verify -pl endpoints/citrus-ws \
  '-Dtest=VertxSoapMessageExchangeTest' '-Dit.test=VertxSoapIT'
./mvnw verify \
  -pl endpoints/citrus-http,endpoints/citrus-websocket,endpoints/citrus-ws,connectors/citrus-openapi,tools/cucumber-steps/citrus-cucumber-http -am
python3 endpoints/citrus-http/src/test/resources/vertx-evaluation/test_run_footprint.py
```

The recorded runs use `-o` because required artifacts were cached and the configured snapshot
metadata service had previously timed out. Exact commands and outcomes are retained in
`../audit-followup-evidence/`. See the decision report for the results and any remaining limits.

Resolve a test classpath and run the bounded campaign from the repository root:

```sh
./mvnw org.apache.maven.plugins:maven-dependency-plugin:3.6.1:build-classpath \
  -pl endpoints/citrus-http -DincludeScope=test \
  -Dmdep.outputFile=/absolute/path/to/test-classpath.txt
sh endpoints/citrus-http/src/test/resources/vertx-evaluation/run-footprint.sh \
  endpoints/citrus-http /absolute/path/to/test-classpath.txt /absolute/path/to/measurements 5 1000
```

The harness requires JDK 21, runs eight clients, verifies HTTP/1.1 and exact reply bytes on every
request, and uses 100 warmups plus 1,000 measurements in each fresh process. It runs five processes
per engine for each text and binary fixture, interleaves engine order, and applies a 120-second
child deadline. Failed runs stop the campaign and retain their output. Raw request durations,
per-run results, medians/ranges, JVM flags, hardware and the shared classpath inventory are saved.
Server startup is measured with a monotonic clock; parent process launch to the child's listening
timestamp uses the host wall clock. Heap after an observed GC is a process-level proxy, including
clients and framework machinery. It is not a retained-heap analysis or production capacity test.

The resolved classpath includes both engines. A future Jetty-free deployment is not modeled here.
The fixed dependency baseline mixes Netty minor versions, so passing plain HTTP tests must not
be described as proof of complete dependency convergence or HTTP/2 compatibility.

## Capability status

The capability delta remains `none`: no production capability has shipped or needs archiving.
The numbered planning documents remain historical; Stage 04 and the decision report describe
current verification. Remaining evaluation requirements must stay open rather than being inferred
from the presence of this documentation file.

## Custom context and dispatcher URL mapping

The test-only prototype can restrict requests to one context and one dispatcher route:

```java
server.setContextPath("/api");
server.setServletMappingPath("/orders/*");
```

Configure these before starting the server. A Citrus receive action should expect the full path
`/api/orders/42`; its `citrus_http_context_path` header is `/api`. Requests outside this route
receive HTTP 404. A bare `/api` redirects with HTTP 301 to `/api/`, retaining the query string.
Exact, prefix, extension, default and empty context-root mappings are supported. Settings take
effect at startup. This does not provide a Servlet container or support custom Servlet classes.
See the [routing investigation](../routing-investigation.md) for normalization limits and the
observed semicolon-path difference in the Jetty/Spring implementation.

## Optional Basic authentication

Before starting the test-only server, call
`server.setBasicAuthentication("test-user", "test-password", "citrus-tests")`.
All matched paths require those credentials. Unauthenticated requests receive HTTP 401 and a
realm challenge before reaching Citrus. Credentials and realm are captured at startup;
`clearBasicAuthentication()` takes effect after restarting. Printable ASCII credentials are
supported, with a nonempty username containing no colon. This adds no roles or Servlet principals.
Use disposable credentials in local tests, and configure HTTPS when transport protection is needed.
See the [authentication investigation](../authentication-investigation.md) for comparison cases.

## Form and multipart messages

The prototype now reads URL-encoded forms into Citrus multi-value maps and writes form or
multipart maps through Spring's form converter. For a server reply, use
`.message().from(new HttpMessage(fields).status(HttpStatus.OK).contentType("application/x-www-form-urlencoded"))`;
use `multipart/form-data` with text, byte arrays or resources for multipart output.
The existing Citrus form validator expects an XML description of the controls for its expected
message. Incoming multipart uploads remain raw messages by default; configure a binary media type when
preserving binary file bytes is required. See [form conversion](../form-conversion.md) for examples,
verified cases and the observed Jetty differences.

## Response tracing cache

Set `server.setResponseCacheSize(100)` before start, or use zero to disable the cache. Retrieve
recent snapshots with `getCachedResponses()` or call `getResponseCache(exchangeId)` using the
received Citrus message's `citrus_vertx_exchange_id` header. Entries contain converted response
status, headers and body bytes, not reusable replies. Capacity is captured at startup; stopping
clears all entries. See [response tracing cache](../response-cache.md) for eviction, copying,
HEAD/error behavior and the boundary with Servlet logging integration.

## Custom Spring body converters

Before start, configure `setMessageBodyConverters(List.of(new VertxMessageBodyConverter<>(
MyPayload.class, new MyPayloadConverter())))`. The first matching Spring reader/writer handles
that payload class and media type; otherwise the defaults apply. The existing Citrus HTTP
converter remains in the conversion chain. Registrations are captured at startup, and converter
instances must be safe for concurrent use. See [custom conversion](../custom-conversion.md) for
configuration, errors, framing and the boundary with full Spring MVC behavior.

## Gzip

Send `Content-Encoding: gzip` with a compressed request body and `Accept-Encoding: gzip` to
request a compressed reply. Citrus receives the decoded payload and sends its ordinary reply;
compression happens around the existing conversion pipeline. Body limits also cover expanded
request bytes. See [gzip handling](../gzip-handling.md) for supported responses, errors and the
limits of encoding negotiation.


## Parsed multipart uploads

Register `new VertxMessageBodyConverter<>(VertxMultipartUpload.class,
new VertxMultipartUploadConverter(maxBytes, maxParts, maxHeaderBytes))` before starting the
server. `VertxMultipartUpload` carries ordered parts with names, optional filenames, read-only
headers and copied bytes. `parts(name)` retains repeated fields, and `text()` uses the part charset.
The request-only converter reuses Spring parsing on exchange workers and keeps input in memory
within configured limits. No automatic file storage or Servlet Part facade is provided.
See [parsed uploads](../multipart-uploads.md) for a complete configuration and Citrus validation example.


## Equivalent handlers and filters

`setFilters(List<VertxHttpFilter>)` installs ordered synchronous worker filters, and
`setHandlers(List<VertxHttpHandlerMapping>)` installs first-match predicate/handler pairs.
Filters can modify converted requests and replies, or short-circuit the exchange. Unmatched
requests fall through to Citrus, with reserved correlation metadata restored. Continuations
must be synchronous, same-thread and single-use. Lists are captured at startup; callbacks are
caller-owned and must be thread-safe. No Servlet container, Servlet compatibility layer or new
dependency is added. See [handlers and filters](../handlers-and-filters.md) for configuration,
execution order, error handling, lifecycle and verification.


## Roles, login services and path policies

Configure `setSecurity(new VertxHttpSecurity(realm, loginService, rules))` before start. The
login-service callback returns an immutable username/role identity or null for invalid credentials.
First-match path/method rules permit anonymous access, require authentication, require any listed
role, or deny access. Unmatched requests are denied. Protected requests receive trusted principal
and role metadata; client-supplied identity headers are removed. Login work runs on the bounded
workers before converters, hooks and Citrus, after bounded body reading. Provider I/O needs finite
timeouts and interruption support. Policy security and legacy single-user Basic are mutually
exclusive. See [security policies](../security-policies.md) for configuration, statuses, lifecycle,
HTTPS usage and the limits of the external-service integration point.

## Native WebSocket endpoint

Added `VertxWebSocketEndpoint`, its wire and endpoint tests, and a WSS Citrus action test to
`VertxHttpServerRoundtripIT`. `VertxHttpServer` captures the endpoint at startup and upgrades a
paused, authorized request on its event loop. The endpoint owns bounded sessions and complete
message queues for that server generation. See [WebSocket usage](../websocket.md) for configuration,
message headers, broadcast/targeted sends, limits and exclusions. The feature comparison now
reflects this test-only implementation. Production registrations and the existing WebSocket
module are unchanged. No capability is archived as shipped production behavior.

## Native SOAP endpoint

Added `VertxSoapMessageExchange`, its exchange unit tests and `VertxSoapIT` to the
`citrus-ws` test sources, reusing the `VertxHttpServer` test fixture from the `citrus-http`
test-jar. `VertxHttpServer` dispatches POST requests with a SOAP content type to the
exchange, which converts them through the existing `WebServiceEndpoint` and
`SoapMessageConverter`. Both SOAP media types must be configured as binary media types,
since they are not defaults. See [SOAP usage](../soap.md) for configuration, message
headers, status handling and exclusions. The feature comparison now reflects this
test-only implementation. Production registrations and the existing SOAP module are
unchanged. No capability is archived as shipped production behavior.
