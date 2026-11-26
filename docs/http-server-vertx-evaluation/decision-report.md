# Evaluating Vert.x for the Citrus HTTP server

15 October 2026 (re-audited against the current prototype) — [Citrus #1808](https://github.com/citrusframework/citrus/issues/1808)

The prototype shows that Vert.x
can handle HTTP and HTTPS requests through existing Citrus actions, serve WebSocket and SOAP
exchanges on the same listener, and support Spring-style interceptors and request-attribute
headers — and the regression tests pass. However, it still does not cover several features that
users get through Jetty and Servlets today. TRACE also behaves differently, and the resolved
dependencies include different Netty versions. The prototype starts faster in these measurements,
but requests are not consistently faster.

## What was evaluated

The experiment adds a Vert.x HTTP server to the test sources at commit
`e0a55e10e6de5617c6046f1e6b8f40eb2b75fdf0`. The changes are uncommitted in the evaluation
worktree. It uses the existing managed version of `io.vertx:vertx-core` as a test dependency.
The production HTTP server, endpoint registrations, configuration schemas and dependency versions
are unchanged.

The tests and measurements ran with Temurin JDK 21.0.12.1 and Maven 3.9.8, using Vert.x 4.5.31
and Jetty 12.1.11. The code still compiles for Java 17, but it was not tested on a Java 17 runtime.

## Test results

The prototype tests passed: **116 unit tests and 153 integration tests** in total, split across
the two modules that host the fixture:

| Prototype area | Unit tests | Integration tests | Evidence |
|---|---:|---:|---|
| HTTP and WebSocket fixture (`citrus-http` test sources) | 114 | 147 | [full HTTP run](audit-followup-evidence/http-module-rerun.log) |
| SOAP exchange (`citrus-ws` test sources) | 2 | 6 | [focused run](soap-evidence/focused.log) |

The HTTP tests exercise real Citrus receive and send actions, including two requests answered in
reverse order and requests waiting on separate servers. They also check that an incorrect
expectation fails validation, that the server can stop with requests still pending, and that
request-size and timeout limits work. The same set of HTTP cases runs against both Jetty and
Vert.x. The newest additions verify Spring-style interceptors with path patterns and the
request-attribute header feature described in the feature comparison below.

The SOAP tests exercise SOAP 1.1 and 1.2 roundtrips through real Citrus SOAP client send/receive
actions, an explicit-status exchange with an empty body, rejection of malformed envelopes, wrong
media types and non-POST methods before the adapter runs, a MustUnderstand fault and attachment
rejection.

The broader regression tests also passed:

| Module | Unit tests | Integration tests or scenarios |
|---|---:|---:|
| HTTP | 351 | 187 |
| WebSocket | 28 | 2 |
| SOAP | 208 | 35 (+ 6 prototype) |
| OpenAPI | 351 | 60 |
| HTTP Cucumber | — | 23 scenarios, 259 steps |

A [fresh full HTTP module run](audit-followup-evidence/http-module-rerun.log) on 15 October 2026
reported no failures or errors: 351 unit tests and 187 integration tests, with the prototype
tests included in those totals, so they should not be counted twice. The earlier
[clean HTTP build](websocket-evidence/clean-http.log) from the WebSocket extension passed
336 unit and 182 integration tests; the increase comes from the interceptor and
request-attribute additions since then. The existing WebSocket module was also
[rerun on JDK 21](websocket-evidence/existing-websocket.log) after this extension: all 28 unit
and 2 integration tests passed.

The SOAP module needs one caveat. The prototype tests pass in their
[focused run](soap-evidence/focused.log), and the 35 pre-existing SOAP integration tests still
pass in a full-module run without the prototype test in the JVM. The full `citrus-ws` module,
however, does not pass in a single JVM when the prototype test class is included. The cause
predates this experiment: the pre-existing Spring tests bind fixed ports (`helloSoapServer` on
13080 and an embedded broker on 61616), and any second test class that builds a default Citrus
context in the same JVM collides with them. A zero-Citrus probe class confirmed this is not
specific to Vert.x code, and the prototype tests use dynamic ports. This is a test-isolation
limit of the current suite, not a regression in the SOAP module.

Cucumber's 23 scenarios are recorded in its console summary. A later test-provider pass runs
zero tests and overwrites its XML report. The results are preserved in the
[module summary](audit-followup-evidence/regression-results.json),
[build log](audit-followup-evidence/regression-resumed.log) and
[HTTP test log](websocket-evidence/clean-http.log).

The shared tests cover UTF-8 and ISO-8859-1 text, binary data, empty bodies, explicit and default
response statuses, and cases where the adapter returns no reply. They check that HEAD responses
have no body, that cookies retain the tested expiration and security settings, and that paths
and query parameters are handled correctly. Query cases include encoded values, repeated keys
and parameters without values. Context paths and HTTP versions are checked too.

GET, POST, PUT, PATCH, DELETE, OPTIONS and HEAD reach Citrus on both engines. **TRACE behaves
differently:** Jetty's Servlet layer answers the request itself, while the prototype passes it
to Citrus actions. The tests confirm this difference. Cookie deletion is also formatted
differently, although both implementations expire the cookie immediately and preserve the
tested security settings.

## Startup, memory and request times

Each engine was measured with authentication disabled at its default root context in **five fresh JVMs for each of two payloads**: UTF-8 text and 1 KiB
of binary data. Each JVM used eight clients, sent 100 warmup requests, and then measured
1,000 requests through an adapter that echoed the request body. Every response was checked for
HTTP/1.1, the expected status and the exact payload bytes. All 20 JVM runs and 20,000 measured
requests completed successfully. These measurements cover HTTP handling and adapter execution;
they do not include the time a full Citrus test script might spend waiting between actions,
and they do not measure form, multipart or custom-converter workloads.

The tests and benchmarks ran on the following laptop:

| System detail | Configuration |
|---|---|
| Laptop | Apple MacBook Pro (model identifier: Mac16,5) |
| Processor | Apple M4 Max, 14 CPU cores, ARM64 architecture |
| Memory | 36 GiB RAM |
| Operating system | macOS 27.0.1 (build 26A434) |
| Java runtime | Eclipse Temurin JDK 21.0.12.1+1, 64-bit |
| Build tool | Maven 3.9.8 |
| Benchmark JVM settings, identical for both engines | 128 MiB initial heap, 256 MiB maximum heap, G1 garbage collector (`-Xms128m -Xmx256m -XX:+UseG1GC`) |

The workload uses plain HTTP and uncompressed traffic, with custom handlers and filters disabled.
Authentication is disabled. These results do not measure TLS, gzip, login-service or user callback performance.
Both engines use the default response tracing cache setting of 100 entries. Their retention
rules differ as described in the response cache documentation.

The benchmark script alternates which engine runs first, allows each JVM up to 120 seconds,
and saves the individual measurements and process output. Tests also cover its percentile
calculations and handling of failed or timed-out processes.

For each run, p50 is the time within which 50% of requests completed, and p95 is the time within
which 95% completed. Each cell shows the median of that percentile across five runs, followed
by the lowest and highest per-run values in parentheses. All times are in milliseconds (ms).
For example, Jetty's text p95 was 1.753 ms, with per-run p95 values ranging from 1.682 to 2.415 ms.

| Engine | Payload | p50 (ms): median (min–max across runs) | p95 (ms): median (min–max across runs) |
|---|---|---:|---:|
| Jetty | text | 1.087 (1.049–1.172) | 1.753 (1.682–2.415) |
| Jetty | binary | 1.028 (0.972–1.059) | 1.731 (1.707–1.812) |
| Vert.x prototype | text | 1.118 (1.057–1.145) | 1.914 (1.891–2.080) |
| Vert.x prototype | binary | 1.039 (1.008–1.075) | 2.065 (2.045–2.214) |

For the text runs, creating and starting the server took a median of 482 ms with Jetty
(range 471–495 ms), compared with 240 ms for the prototype (231–246 ms). Including the time to
launch the JVM, the medians were 703 ms (689–707 ms) and 451 ms (448–469 ms). Server startup was
measured with a monotonic clock; the JVM-launch measurement used timestamps from the host clock.
The prototype does not perform Jetty's Spring MVC and Servlet setup, which is an important part
of the difference: the two implementations do not yet offer the same features.

In the text runs, Jetty had a median of 14 idle threads and 61 live threads after the requests;
the prototype had 9 and 48. After garbage collection, median heap usage was about 19.6 MiB for
Jetty and 17.9 MiB for the prototype. These figures include the clients and framework code in the
same JVM, so they do not isolate the server's resource use.

The prototype started faster. Its median p95 request time was higher for both text and binary data.
This small test on one machine is not enough to predict production throughput or capacity.
These HTTP measurements were taken before the WebSocket extension, with no WebSocket endpoint
configured. They have not been rerun for the WebSocket, SOAP, interceptor or request-attribute
extensions.

The [measurement summary](security-evidence/measurements/summary.json) contains the full
results and ranges, alongside the [individual samples](security-evidence/measurements/footprint.jsonl)
and [machine and JVM details](security-evidence/measurements/environment.json).

## Feature comparison

The tables below compare the **current Citrus server built on Jetty** with the **Vert.x prototype
implemented for this evaluation**. They do not describe everything the Vert.x library could
support in a future implementation. “Verified” means exercised by this evaluation's tests;
“supported” describes an existing Citrus feature, without claiming a new comparison test for it.

The Vert.x column describes **Vert.x plus the Citrus integration code in this prototype**.
Vert.x handles HTTP connections; the prototype converts requests to Citrus messages, invokes a
Citrus endpoint adapter and converts replies back to HTTP. “Verified” is not a claim about the
Vert.x library alone, nor does it mean every row has a full Citrus action test. The receive/send,
reply-correlation and authenticated-exchange tests use real Citrus actions. Other HTTP comparison
cases use real sockets and Citrus messages with a small test adapter; unit tests cover individual
helpers. All added integration code remains in test sources, with no production endpoint registration.

The rows are grouped by theme so the table is easier to scan. The middle column names the
Jetty or Servlet mechanism each Citrus feature actually relies on and whether Vert.x can
cover it; the last column carries the test classes that back the Vert.x claim.

### Core HTTP exchange

| Feature | Current Citrus server (Jetty) | What Citrus uses from Jetty → coverable in Vert.x? | Vert.x prototype | Vert.x evidence (source classes) |
|---|---|---|---|---|
| Plain HTTP/1.1 through Citrus receive/send actions | Supported; shared HTTP cases verified | Jetty connector + DispatcherServlet dispatch to the Citrus controller — yes | Verified | VertxHttpServerRoundtripIT |
| GET, POST, PUT, PATCH, DELETE, OPTIONS and HEAD | Verified | Jetty request parsing + method-specific controller mappings — yes | Verified | VertxHttpContractIT |
| TRACE | Servlet layer answers directly; verified | Container-level TRACE handling — yes; deliberately answered by Citrus actions instead | Citrus actions choose the reply; verified, but different behavior | VertxHttpContractIT |
| UTF-8 and ISO-8859-1 text | Verified | Servlet request character encoding + Spring text converters — yes | Verified | VertxHttpContractIT |
| Binary data and empty bodies | Verified | Servlet input stream + configured binary media types — yes | Verified for configured binary media types | VertxHttpContractIT |
| Explicit/default response status and an empty response when the adapter returns no reply | Verified | Controller response-entity handling — yes | Verified | VertxHttpContractIT, VertxHttpPipelineIT |
| HEAD responses without a body | Verified | Container HEAD body suppression — yes | Verified | VertxHttpContractIT, VertxHttpPipelineIT |
| Request paths, query parameters, context and HTTP-version metadata | Verified | HttpServletRequest accessors — yes | Verified at root and custom contexts | VertxHttpContractIT, VertxHttpRoutingIT |
| Cookie deletion, Secure, HttpOnly and SameSite | Verified | Servlet Cookie type + request/response cookie APIs — yes | Verified; deletion-header formatting differs | VertxHttpContractIT, VertxHttpMessageMapperAuditTest |

### Concurrency, lifecycle and limits

| Feature | Current Citrus server (Jetty) | What Citrus uses from Jetty → coverable in Vert.x? | Vert.x prototype | Vert.x evidence (source classes) |
|---|---|---|---|---|
| Concurrent requests with separate Citrus replies | Supported through the default adapter | Jetty thread pool + reply-queue correlation — yes | Verified, including reverse-order replies and separate servers | VertxHttpServerConcurrencyIT |
| Start, stop and restart | Supported | Jetty Server lifecycle — yes | Verified, including pending requests and cleanup | VertxHttpServerLifecycleIT |
| Body-size, upload-timeout and concurrent-exchange limits | Uses Jetty/Servlet configuration; an equivalent set of limits was not compared | Jetty connector and thread-pool settings — yes | Explicit prototype settings; verified | VertxHttpServerLimitsIT |

### Security and TLS

| Feature | Current Citrus server (Jetty) | What Citrus uses from Jetty → coverable in Vert.x? | Vert.x prototype | Vert.x evidence (source classes) |
|---|---|---|---|---|
| HTTPS/TLS | Supported through Jetty connectors; existing HTTPS Cucumber scenario passed | Jetty TLS connector configuration — yes | Verified with PKCS12 stores and JDK TLS 1.2/1.3, including Citrus actions | VertxHttpTlsIT |
| Required TLS client certificates | Available through Jetty TLS configuration; not compared here | Jetty TLS client-auth setting — yes | Verified with a PKCS12 client trust store, including rejection of missing/untrusted identities | VertxHttpTlsIT |
| Basic authentication | Supported through Jetty security handlers; shared credential cases verified | Jetty security handler + login service + Basic authenticator — yes | Verified for one configured user, or a pluggable login service with path/method policies | VertxHttpBasicAuthIT, VertxHttpSecurityIT |
| Authentication roles and path/method security policies | Available through Jetty security configuration | Jetty constraint mappings — yes | Implemented and verified; ordered rules, role checks and default deny | VertxHttpSecurityIT, VertxHttpSecurityTest |
| External login services | Available through Jetty login services | Jetty LoginService SPI — yes; pluggable interface ready, connectors not bundled | Pluggable worker login-service interface verified; concrete directory/identity-provider connectors are not bundled | VertxHttpSecurityIT |

### Routing

| Feature | Current Citrus server (Jetty) | What Citrus uses from Jetty → coverable in Vert.x? | Vert.x prototype | Vert.x evidence (source classes) |
|---|---|---|---|---|
| Custom context path | Verified | Servlet context path — yes | Verified; a configurable base path such as `/api` | VertxHttpRoutingIT, VertxHttpServerRoundtripIT |
| Dispatcher URL mappings (exact, prefix, extension, default and context root) | Verified | Servlet mapping patterns — yes; one rule per server today, multiple rules possible | Verified; one URL rule can cover many paths (for example, `/*` accepts every path in the context) | VertxHttpRoutingIT, VertxHttpRouteTest |

### Handlers, filters, interceptors and request attributes

| Feature | Current Citrus server (Jetty) | What Citrus uses from Jetty → coverable in Vert.x? | Vert.x prototype | Vert.x evidence (source classes) |
|---|---|---|---|---|
| Custom request handlers and filters | Supported through Servlet APIs | Servlet filter chain + custom controllers/servlets — yes, as adapted message-level hooks | Equivalent worker hooks implemented and verified; ordered filters, first-match handlers and Citrus fallback | VertxHttpPipelineIT, VertxHttpPipelineTest |
| Existing Servlet handler/filter implementations without adaptation | Supported | The Servlet container API itself — no (a facade could cover only simple cases) | Not supported; prototype hooks use Citrus messages and do not add a Servlet container | VertxHttpFilter, VertxHttpHandler (message-only contracts; no Servlet types) |
| Spring handler-interceptor semantics | Supported through the handler-mapping interceptor chain | Handler-mapping interceptor chain + delegating interceptor — yes | Implemented and verified; ordered pre/post/completion callbacks with path patterns around custom handlers and the Citrus adapter | VertxHttpInterceptorIT, VertxHttpInterceptorTest |
| Spring web contexts, controllers and unmodified interceptor classes | Supported | DispatcherServlet + annotation-driven MVC — yes, with an MVC subset and a Servlet facade (not built) | Not implemented; no Servlet container, DispatcherServlet or annotation-driven controller detection | — |
| Servlet attributes exposed as Citrus message headers | Supported | HttpServletRequest attribute enumeration — yes | Implemented and verified; event-loop staging hook with verbatim names, raw values and overwrite semantics | VertxHttpAttributeHeadersIT, VertxHttpAttributeHeadersTest |

### Forms and multipart

| Feature | Current Citrus server (Jetty) | What Citrus uses from Jetty → coverable in Vert.x? | Vert.x prototype | Vert.x evidence (source classes) |
|---|---|---|---|---|
| URL-encoded form requests and responses | Verified; duplicate-field and charset behavior differs as described below | Cached request body + Spring form converter — yes | Verified; decodes forms and writes form maps using Spring conversion | VertxHttpFormIT, VertxHttpFormConverterTest |
| Multipart uploads received by Citrus | Verified as raw message bodies | Raw request body bytes (container-independent) — yes | Verified as raw message bodies; binary preservation is configurable | VertxHttpServerRoundtripIT |
| Multipart responses from field/file maps | Verified | Spring multipart writer — yes | Verified for text, byte arrays, named resources and custom part headers | VertxHttpServerRoundtripIT |
| Parsed multipart upload parts | Not exercised by the existing raw-body upload API | Nothing Jetty-specific (raw-body API only) — yes | Implemented and verified through an opt-in converter, including Citrus actions | VertxHttpServerRoundtripIT, VertxMultipartUploadIT, VertxMultipartUploadTest |
| Servlet `Part` objects and `request.getParts()` | Available through Servlet multipart configuration; not compared here | Servlet multipart configuration + Part API — possible; needs a Part facade over parsed uploads (not built) | Not implemented; parsed upload values do not require a Servlet container | — |

### Message conversion

| Feature | Current Citrus server (Jetty) | What Citrus uses from Jetty → coverable in Vert.x? | Vert.x prototype | Vert.x evidence (source classes) |
|---|---|---|---|---|
| Gzip request/response handling | Shared text and binary exchanges verified through the existing Servlet filter | Gzip Servlet filter + request/response wrappers — yes | Implemented with bounded request decoding and negotiated response compression; shared exchanges and Citrus actions verified | VertxHttpGzipIT, VertxHttpGzipTest |
| Custom Citrus HTTP message converters | Supported through the existing conversion API | Citrus conversion API (container-independent) — yes, reused unchanged | Verified together with a custom body converter and Citrus actions | VertxHttpCustomConverterIT |
| Custom Spring HTTP body converters | Supported through Spring MVC configuration | Spring MVC message-converter list — yes | Implemented and verified; ordered typed readers/writers with default fallback | VertxHttpCustomConverterIT, VertxMessageBodyConverterTest |
| Full Spring MVC conversion pipeline | Uses Spring MVC; not exhaustively compared | DispatcherServlet + handler adapter + advice/resolvers — yes, with the MVC subset (not built) | Not implemented; no MVC advice, controller argument resolution or generic type-based selection | — |

### Tracing, protocols and configuration

| Feature | Current Citrus server (Jetty) | What Citrus uses from Jetty → coverable in Vert.x? | Vert.x prototype | Vert.x evidence (source classes) |
|---|---|---|---|---|
| Response cache for message tracing | Supported by the Citrus controller, keyed by Servlet request | Controller cache keyed by the Servlet request — yes | Implemented and verified; bounded snapshots keyed by Citrus exchange ID | VertxHttpResponseCacheIT, VertxResponseCacheTest |
| HTTP/2 | Not evaluated | Jetty HTTP/2 connector support — yes; Vert.x supports h2, disabled in this prototype | Disabled in this plain HTTP prototype | VertxHttpServer (plain listener bootstrap; no H2 configuration) |
| Java, annotations, Spring XML and XML/YAML/Groovy configuration | Existing configuration routes are available | Citrus builders, annotation/XML parsers and schema (container-independent) — yes, not yet wired for the prototype | Java test fixture only; no public configuration registration | VertxHttpServer, VertxHttpServerOptions (Java-only fixture wiring) |

### Other protocols on the same listener

| Feature | Current Citrus server (Jetty) | What Citrus uses from Jetty → coverable in Vert.x? | Vert.x prototype | Vert.x evidence (source classes) |
|---|---|---|---|---|
| WebSocket server | Existing Jetty-based module; regression tests passed | Jetty WebSocket upgrade handling — yes | Implemented in the test fixture; text/binary messages, broadcast and session replies, bounded queues, authenticated upgrades and WSS verified with Citrus actions | VertxWebSocketIT, VertxWebSocketEndpointTest |
| SOAP server | Separate Jetty-based module; regression tests passed | Own Jetty server + SAAJ (container-independent) — yes | Implemented in the test fixture; SOAP 1.1/1.2 envelopes, SOAPAction handling, MustUnderstand faults and explicit reply statuses verified with Citrus actions | VertxSoapIT, VertxSoapMessageExchangeTest |

In the middle column, “yes” means no fundamental blocker — where the prototype already
implements the feature, the Vert.x prototype column says so; “possible / not built” marks
feasible work that has not been done; “no” marks features that require a Servlet container
or Servlet API by definition. Several features turn out to use nothing Jetty-specific at
all (raw request bodies, Citrus conversion, SAAJ); those are the easiest to move.

Evidence classes are short names. HTTP classes live in test sources under
`endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/` (prototype and unit
tests) and `.../http/integration/` (Citrus-action round trips); SOAP classes live under
`endpoints/citrus-ws/src/test/java/org/citrusframework/ws/vertx/`. A `—` entry marks a
deliberate non-goal with no corresponding class; the absence is the evidence.

The WebSocket endpoint shares the Vert.x listener and works with Citrus send/receive actions.
Tests cover fragmented text, binary data, targeted replies, broadcast, ping/pong, limits, restart
cleanup and a role-protected WSS exchange. It adds no Jetty integration. The existing WebSocket
module and its public configuration remain unchanged; see [WebSocket support](websocket.md)
for the supported behavior and limits. WebSocket performance has not been measured.

The SOAP exchange shares the Vert.x listener and works with Citrus SOAP client actions.
Tests cover SOAP 1.1 and 1.2 envelopes, SOAPAction handling, MustUnderstand faults,
explicit reply statuses and rejection of malformed requests, wrong media types and
non-POST methods before the adapter runs. Replies with attachments or MTOM are rejected.
It adds no Jetty or Servlet integration. The existing SOAP module and its public
configuration remain unchanged; see [SOAP support](soap.md) for the supported behavior
and limits. SOAP performance has not been measured.

Security policies can require authentication or roles for selected paths and HTTP methods,
allow public paths, or deny access. A pluggable login service runs on the bounded workers and
returns the trusted username and roles. Unmatched paths are denied. Tests cover denial before
Citrus, provider failures, identity-header spoofing and a role-protected HTTPS exchange. See
[roles and login services](security-policies.md) for configuration and the provider contract.

HTTPS uses Vert.x and the JDK TLS engine. Tests verify trusted TLS 1.2/1.3 connections, hostname
and trust failures, required client certificates and a Citrus receive/send exchange. PKCS12
configuration and its limits are described in [HTTPS and client certificates](tls.md). The
footprint measurements use plain HTTP and do not measure TLS performance.

Custom handlers and filters can inspect or change converted requests, choose a reply, or wrap
the Citrus exchange. They run on the existing workers and use the same response conversion and
lifecycle. They do not add a Jetty integration. Existing Servlet implementations need adapting
to these hooks. See [handlers and filters](handlers-and-filters.md) for configuration and limits.

Interceptors add the Spring `HandlerInterceptor` shape on top of those hooks: an ordered
`preHandle`/`postHandle`/`afterCompletion` contract with Ant-style include and exclude path
patterns. A `preHandle` can short-circuit the exchange with its own reply, `postHandle` can
transform the reply, and the completion callbacks run even when a handler or the Citrus adapter
throws. The chain wraps custom handlers and the Citrus fallback alike, so the same interceptor
sees both kinds of traffic — mirroring how the Jetty server's handler-mapping chain sees both
`HttpMessageController` and user controllers. What the prototype does not provide is the rest of
Spring MVC: there is no `DispatcherServlet`, no annotation-driven controller detection, and
existing interceptor classes cannot be reused unmodified. The
[interceptor evidence log](handler-evidence/interceptor.log) records the passing tests.

Request attributes are the second opt-in parity feature. On Jetty, `handleAttributeHeaders`
copies every Servlet request attribute into the Citrus message with verbatim names and raw
values. The prototype offers the same semantics through an event-loop staging hook — the
analogue of a Servlet filter that sets attributes before dispatch — whose values are snapshotted
with the request and copied into the Citrus message when the flag is on, overwriting any
existing headers just like the Servlet version. The
[attribute-header evidence log](handler-evidence/attribute-headers.log) records the passing
tests, and [handlers and filters](handlers-and-filters.md) shows the hook configuration.

Gzip handling uses the same Citrus conversion and receive/send flow. Expanded request bodies
are size-limited, and corrupt gzip is rejected before it reaches Citrus. The prototype respects
`gzip;q=0`; the existing Servlet filter uses a substring check. See [gzip handling](gzip-handling.md)
for response exclusions and the limits of content-coding negotiation.

For example, a context of `/api` and a mapping of `/orders/*` accept `/api/orders/42`
and reject `/api/other` with HTTP 404. Citrus receives the full path `/api/orders/42`
and the context header `/api`. A request to `/api` redirects to `/api/`, preserving its query.
The prototype applies changed routing settings when it next starts. It supports these URL rules
without loading Java Servlets; custom Servlet classes, filters and Servlet lifecycle hooks
remain unavailable. One URL rule can cover many endpoints: `/*` accepts every path in the context,
so Citrus can handle both `/api/orders` and `/api/customers`. Registering several separate URL
rules with different handlers on the same prototype server is not implemented.

Basic authentication is optional. In the single-user mode, one user and realm protect all
matched routes. Policy security instead uses a login-service callback and path/method rules.
Invalid credentials on protected paths receive HTTP 401 and a realm challenge before Citrus
handles the request. Credentials are limited to printable ASCII. Malformed Base64 credentials return
401 in the prototype but produced 500 in the Jetty comparison. Details and configuration examples
are in the [authentication investigation](authentication-investigation.md).

There is one observed routing difference: `/api/orders;key=a,b/42` under that prefix mapping
returns HTTP 500 in the current Jetty/Spring setup before reaching Citrus. The prototype accepts
it and removes the path parameter from the Citrus path. The prototype rejects encoded path
separators, encoded semicolons and dot segments with HTTP 400. Context names are limited to
ASCII letters, digits, underscores and hyphens in slash-separated segments. These checks do not
establish compatibility with every Servlet URI-normalization rule. The
[routing investigation](routing-investigation.md) describes the cases and limits.

The prototype also retains recent converted responses for tracing. This cache never supplies
replies to later requests: each request runs through its configured handler or Citrus adapter. Its capacity is captured at
startup, zero disables it, and stopping the server clears it. Entries contain response status,
headers and body bytes and can be retrieved by exchange ID. It does not connect to the existing
Servlet logging interceptor. The [response cache documentation](response-cache.md) explains the
API, retention rules and the difference in capacity handling from the current controller.

The common HTTP cases work on both implementations. Features listed as “not implemented” are
missing from this prototype, rather than proven limitations of Vert.x itself. Existing users
who need those features should keep using Jetty. The [usage documentation](sdd/05-documentation.md)
includes HTTP and HTTPS examples, plus an existing Jetty example for custom Servlets.

## Dependencies and integration impact

Maven resolves 40 runtime JARs totaling 24.7 MB. The test classpath contains 157 JARs totaling
75.1 MB, including both engines and the test tools. The inventories record each artifact's name,
version, path and size. These totals describe the Maven classpaths, not the size of a packaged
application. A production comparison would also need to account for dependencies supplied by
the application and decide which Jetty components would remain. In this experiment, Vert.x is
a test dependency and Jetty remains in the production runtime.

The [dependency tree](audit-followup-evidence/dependency-tree.log) includes Netty 4.2.16 for
modules managed by Citrus, alongside 4.1.136 for the HTTP/2 and DNS modules brought in by Vert.x.
The HTTP/1.1 tests did not encounter class-loading or method-linking errors, but they do not
establish that every combination is compatible. These versions need to be reviewed and aligned
before production adoption, especially if HTTP/2 is required.

The verified payload cases include strings, binary byte arrays and form/multipart maps. URL-encoded
forms retain repeated fields, empty values and explicit charsets in the prototype. The Jetty
comparison retained only the first repeated value in the plain form case and delivered a String
rather than a decoded map when the content type included a charset. These differences are explicit
in the tests. Multipart uploads remain raw by default. An opt-in converter delivers ordered parsed
parts with filenames, headers and binary file bytes. Empty parts, repeated fields and size limits
are covered by tests, including an actual Citrus receive/send exchange. See
[parsed multipart uploads](multipart-uploads.md) for configuration and limits.
Multipart responses use Spring conversion to write the
boundary, part headers and payload bytes. Both form and multipart exchanges also pass through real
Citrus receive/send actions. See the [form conversion details](form-conversion.md).
Custom Spring body converters can now be registered with an explicit request payload class.
The first matching converter runs before the default body conversion, while the existing Citrus
HTTP converter still maps the surrounding message and headers. Tests verify typed input/output,
worker-thread execution, custom headers, errors, HEAD handling and real Citrus receive/send
integration. This does not provide MVC advice, Servlet context or generic type-based converter
selection. See [custom conversion](custom-conversion.md). Cookie attributes beyond the tested set
still need checking.

A replacement would affect more than the HTTP module. WebSocket and GraphQL inherit from the
current HTTP server, and other integrations create or configure it.
[#1807](https://github.com/citrusframework/citrus/issues/1807) covers WebSocket modernization;
the native endpoint demonstrates a tested subset, but migrating the existing module and its
public configuration still needs its own design and tests. The existing SOAP module starts
its own Jetty server; the prototype demonstrates a tested subset on the shared Vert.x
listener, and a migration of the module and its public configuration would need its own
design and tests.
The passing regression tests show that the existing implementations continue to work with this
experiment; they do not establish a complete replacement for those modules.

## Recommended next step

The goal is to replace Jetty with Vert.x in `citrus-http` entirely and remove the Jetty
dependency from the module. The prototype proves the approach is technically possible; turning
it into a full replacement takes these steps, in order:

1. **Promote the prototype to production code.** Move the server from the test sources into
   `src/main` of `citrus-http` as a real Citrus endpoint, next to the existing `HttpServer`.
   The code already carries 114 unit and 147 integration tests, which move with it.

2. **Define the public configuration API.** The server is wired in Java only today. A
   production endpoint needs the same entry points the Jetty server offers: annotation and
   Spring XML namespace configuration, XML/YAML/Groovy test DSL parsers, and builder methods
   for every supported option (routes, security, TLS, gzip, filters, handlers, interceptors,
   request attributes, converters, limits).

3. **Close the remaining feature gaps.** Multiple URL routing rules, Spring web contexts and
   `@Controller` support, the Servlet `Part` API, and the full Spring MVC conversion pipeline
   are still missing. Each gap needs either an implementation or a documented replacement so
   existing user setups keep working.

4. **Settle the open technical decisions.** TRACE behavior (the Servlet container answers
   directly, while the prototype routes it through Citrus actions), whether to enable HTTP/2,
   and the Netty version alignment (the build manages 4.2.16 while Vert.x resolves 4.1.136).

5. **Remove Jetty from the module.** Decide which Jetty and Jakarta Servlet artifacts may
   remain on the `citrus-http` classpath — the prototype still uses the Servlet `Cookie`
   value type — then make the Servlet dependency optional or drop it entirely. Verify the
   module builds and passes with no Jetty server present.

6. **Migrate the downstream consumers.** WebSocket and GraphQL build on the current HTTP
   server, and the SOAP module already runs a tested subset on the shared Vert.x listener.
   Each consumer needs its own migration design and regression evidence before Jetty can
   leave the dependency tree.

7. **Prove it at scale.** Rerun the full module regression with Vert.x as the only server,
   repeat the performance measurements on representative hardware (the current numbers come
   from a small laptop study that predates the WebSocket, SOAP, interceptor and
   request-attribute extensions), and publish a migration guide so users can adapt existing
   Servlet filters, handlers and interceptors.

Until these steps are done, Jetty must stay in the module. The passing tests show that both
implementations coexist without regressions, which is what makes this path possible.

The [usage documentation](sdd/05-documentation.md) provides the commands to reproduce the tests
and measurements with JDK 21.
