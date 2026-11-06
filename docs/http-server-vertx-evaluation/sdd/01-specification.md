# Specification: Evaluate Vert.x for the Citrus HTTP server

## Status and scope

Prepared **2026-10-13** for [issue #1808](https://github.com/citrusframework/citrus/issues/1808),
with [issue #1807](https://github.com/citrusframework/citrus/issues/1807) as a related WebSocket
initiative. Repository evidence is the Java code at
`f821ff802842ecb9969261352587d8c754b35c72`; existing unrelated local changes are excluded.

The original planning scope covered **SDD stages 01–03**. The user subsequently authorized
audit-driven prototype corrections and a report that matches the implementation. The acceptance
criteria below remain the agreed evaluation requirements; current results are recorded in
[Stage 04](04-implementation.md) and the [decision report](../decision-report.md). No production
migration is authorized. Stop on an actual blocker; do not silently change the scope or
dependency baseline to work around one.

The issue is exploratory. Its proposed benefits are hypotheses to measure. This plan recommends
an isolated, test-only prototype and a migration decision report; it does not select Vert.x as
the production default. Documents follow the existing `docs/<feature>/sdd/` convention and the
local `docs/` Git exclusion. Nothing is committed or published in this planning pass.

## Overview

Determine whether a Vert.x HTTP server can preserve the Citrus request/reply testing model with
acceptable compatibility and maintenance costs. Inventory Jetty coupling, define a comparable
prototype, and produce evidence for retaining Jetty, introducing an optional alternative, or
pursuing a later replacement.

## User stories

- **As a Citrus maintainer**, I want an evidence-based comparison of HTTP server approaches so
  that a migration decision accounts for dependencies, maintenance, and existing integrations.
- **As a test author**, I want existing HTTP receive/send actions, message semantics, and server
  lifecycle to remain usable so that changing the transport does not require rewriting tests.
- **As an extension maintainer**, I want concrete treatment of Jetty and Servlet customization
  points so that I can identify incompatible configurations before a migration is proposed.
- **As the WebSocket maintainer**, I want the shared HTTP boundary and transition constraints
  documented so that #1807 can proceed without assuming SOAP or Servlet support comes for free.

## Current behavior and evidence

No HTTP server capability spec exists in `docs/_capabilities/`. Existing GraphQL and Playwright
HTTP capability specs were inspected: they describe separate consumers or client transports,
not a replacement server. This investigation changes no shipped capability, so its explicit
`none` delta below requires no new capability baseline or archive. A production follow-up must
baseline the HTTP capability and declare its actual behavior changes before implementation.

Repository paths in this table are relative to the root. Source inspection is evidence of the
current design; it is not a substitute for running the proposed compatibility tests.

| Current behavior | Evidence |
|---|---|
| Embedded Jetty server, Spring MVC dispatcher, configurable connectors, filters, interceptors and security | `endpoints/citrus-http/src/main/java/org/citrusframework/http/server/HttpServer.java`, `AbstractHttpServerBuilder.java` in the same package, and `http/servlet/CitrusDispatcherServlet.java` |
| Incoming method, path, context path, query parameters, headers and body become a Citrus HTTP message | `endpoints/citrus-http/src/main/java/org/citrusframework/http/controller/HttpMessageController.java`; `endpoints/citrus-http/src/test/java/org/citrusframework/http/integration/HttpMessageControllerJavaIT.java` |
| Adapter reply supplies body, headers and status; missing status uses the server default. A null reply yields an empty response with the default status, normally 200 | HTTP controller; `HttpServerStandaloneJavaIT.java` and `HttpMessageControllerJavaIT.java` in the HTTP integration test directory |
| Configured binary media types retain bytes; cookie handling is optional | `http/message/DelegatingHttpEntityMessageConverter.java`; `HttpServerBinaryJavaIT.java`, `HttpHandleCookiesIT.java` |
| Default adapter waits synchronously for a Citrus test response, catches a reply timeout and returns null unless a fallback supplies a response | `core/citrus-base/src/main/java/org/citrusframework/base/server/AbstractServer.java`, `core/citrus-base/src/main/java/org/citrusframework/endpoint/direct/DirectEndpointAdapter.java`, and `base/endpoint/AbstractEndpointAdapter.java` |
| HTTP test actions accept the generic `Endpoint` contract | `endpoints/citrus-http/src/main/java/org/citrusframework/http/actions/HttpActionBuilder.java` and `HttpServerActionBuilder.java` |
| Existing Java, annotation, Spring XML, XML/YAML and Groovy configuration routes expose concrete HTTP server builders | HTTP `config`, `endpoint/builder`, `server`, and `src/test/java/org/citrusframework/http/{xml,yaml,groovy}` packages |
| WebSocket subclasses HTTP and customizes Jetty Servlet setup; SOAP has its own Jetty server extending the generic server base | `endpoints/citrus-websocket/src/main/java/org/citrusframework/websocket/server/WebSocketServer.java`; `endpoints/citrus-ws/src/main/java/org/citrusframework/ws/server/WebServiceServer.java` |
| Existing Citrus Vert.x endpoints use the event bus; their factories can create clustered instances | `endpoints/citrus-vertx/src/main/java/org/citrusframework/vertx/endpoint/VertxProducer.java` and `factory/AbstractVertxInstanceFactory.java` |

## Acceptance criteria for the future evaluation

Each experiment must record `PASS`, `FAIL`, or `BLOCKED`, exact versions and commands, and the
observed result. A failed candidate can support a completed evaluation; an unexecuted or blocked
experiment cannot establish compatibility. A mandatory experiment blocked by unavailable
prerequisites stops the work with its evidence preserved.

- [ ] **AC1 — Dependency and API inventory.** Produce a repository-wide inventory of direct
  Jetty imports, Jetty dependencies, HTTP subclasses and typed consumers. Include HTTP,
  WebSocket, SOAP, HTTP Cucumber steps, Selenium, Kubernetes/Knative, OpenAPI, endpoint catalog,
  and test consumers. Distinguish runtime coupling, public signatures, configuration references,
  dependency-only coupling and test-only use; identify every production search hit's disposition.
- [ ] **AC2 — Configuration compatibility.** Classify each current HTTP builder/annotation/XML
  option and its XML/YAML/Groovy exposure as reusable, requiring translation, retained on Jetty,
  or requiring a breaking change. Include connectors, TLS/authentication, Servlet filters and
  handlers, Spring contexts/controllers/interceptors, cookies, attribute headers, binary media,
  context/path mapping, message conversion, logging and response caching. Give a concrete
  before/after or retained-Jetty example for ordinary HTTP, TLS, and custom Servlet configuration.
- [ ] **AC3 — Citrus-controlled round trips.** The Vert.x prototype accepts real loopback HTTP
  requests and exposes them through existing `http().server(endpoint).receive()` actions. The
  test chooses the response through existing send actions. Verify GET, POST, PUT, PATCH, DELETE,
  OPTIONS and HEAD; test TRACE explicitly and record restrictions. Assert request method, path,
  context path, protocol, headers, encoded/duplicate/valueless query values, and response status,
  headers and text. A deliberate Citrus expectation mismatch must fail the test.
- [ ] **AC4 — Payload and reply semantics.** Compare both engines with identical UTF-8 text,
  non-UTF-8 text with an explicit charset, empty bodies and binary byte fixtures. Test explicit
  status, missing status, null adapter result and default-adapter reply timeout. Preserve
  the default-status behavior described above; a timeout must not become an invented 504.
  Cover HEAD response body suppression. Cookie round trips, form/multipart, gzip, matrix path
  handling and custom converters receive explicit compatibility classifications even where
  excluded from the first executable prototype.
- [ ] **AC5 — Concurrency and failure isolation.** With at least two concurrent requests, hold
  the first reply pending and deliver the second first. Check correct correlation and independent
  progress, no adapter invocation on an event-loop thread, and exactly one response per request.
  Exercise adapter exceptions, client disconnect, late replies, and configured admission limits;
  record the outcome and verify no cross-request or cross-server reply leakage.
- [ ] **AC6 — Lifecycle.** Constructing the prototype opens no socket. An initialized instance
  starts only after binding succeeds and exposes the bound port. Exercise failed bind, stop
  with pending work, repeated stop, destruction after stop, and three start/stop generations.
  Verify port reuse, bounded termination, and no owned worker/event-loop resources left alive.
- [ ] **AC7 — Coexistence.** Start Jetty and Vert.x HTTP endpoints on distinct ports in the same
  JVM, exchange distinguishable messages with both, and stop one while the other continues.
  Record dependency convergence and linkage outcomes. Keep existing WebSocket and SOAP on
  Jetty; run their existing relevant integration tests without modifying their contracts.
- [ ] **AC8 — Reproducible footprint and runtime comparison.** Record Citrus commit, JDK,
  OS/hardware, resolved dependencies and fixture parameters. Compare runtime JAR count/size,
  startup-to-listen time, idle/live thread count, retained heap or a clearly labeled memory proxy,
  and request latency for the same HTTP workload. Use at least five fresh-process runs per engine,
  identical warmup/load settings, and report raw samples plus median/range. Separate the additive
  prototype classpath from any modeled future Jetty-free deployment. Do not claim that the
  prototype proves dependency removal or that Vert.x is lighter without measured evidence.
- [ ] **AC9 — Existing behavior stays available.** No production server, default endpoint
  registration, exported configuration schema, runtime dependency or HTTP client changes are
  needed for the prototype. Relevant current HTTP, WebSocket, SOAP, OpenAPI and Cucumber tests
  must be accounted for with passed, failed, skipped and unrun counts. No blanket skip is a pass.
- [ ] **AC10 — Reviewable conclusion.** Deliver a feature/configuration compatibility matrix,
  dependency inventory, reproducible experiment results, migration examples and a recommendation
  with at least two compared alternatives. Answer the issue's questions about other consumers,
  coexistence, and existing configurations; separate proven results from remaining assumptions.
  Explain how #1807 could reuse the design and why SOAP needs separate treatment.

## Constraints

- **Technical:** Use the Maven wrapper and repository-managed dependency versions. At the
  inspected revision these include Java source level 17, Jetty 12.1.11 and Vert.x 4.5.31. The
  Vert.x version is explicitly tied to the Fabric8 dependency in the root POM. Do not upgrade
  it or change the global compiler baseline to make a prototype pass. Run all evaluation builds,
  tests and measurement processes on **JDK 21 only**, as requested. Keep the repository's Java 17
  source/release target unchanged; this evaluation makes no JDK 17 runtime compatibility claim.
- **Isolation:** Proposed prototype classes and any added Vert.x dependency belong to test
  scope. Existing public HTTP server types and defaults remain available. Reuse the generic
  endpoint/adapter boundary; no new core abstraction is required to conduct the evaluation.
- **Performance:** There is no approved production performance target or promised improvement.
  The measurable requirement is a reproducible comparison under AC8. Proposed experiment
  budgets are 30 seconds for startup, 5 seconds for a test reply, 10 seconds for shutdown and
  60 seconds per scenario; log any explicit override. Use barriers/events for concurrency proofs.
- **Security:** Bind the plaintext prototype to loopback with synthetic fixtures. Give it a
  configurable body limit (initial fixture limit: 1 MiB) and finite pending-request admission
  (initial limit: 32). Oversized bodies and saturated admission must fail deterministically.
  TLS, authentication, Servlet security and credential-bearing logs must appear in the
  compatibility inventory; plaintext prototype success is not evidence of security parity.
- **Compatibility:** Preserve observable request/reply semantics for the exercised subset.
  Explicitly identify unsupported Servlet/Jetty integrations. Source compatibility, binary
  compatibility, configuration compatibility and dependency compatibility are separate claims.
- **Development:** Future executable work follows tests-first development, Apache 2.0 headers,
  `*Test.java`/`*IT.java` naming and existing module test conventions. The HTTP module currently
  uses TestNG extensively; the repository also supports JUnit Jupiter. Do not introduce a
  second test runtime solely for this experiment.

## Out of scope

- Replacing Jetty in production, removing Jetty dependencies, publishing a new endpoint artifact,
  or implementing a selectable engine in the public HTTP DSL in this issue's first experiment.
- Implementing #1807, rewriting the Spring-WS SOAP transport, or changing the HTTP client.
- Full Servlet emulation, production TLS/authentication replacements, complete HTTP/2/HTTP/3,
  streaming/SSE support, or a production performance guarantee.
- Executable prototype code, dependency edits, builds, tests, commits, GitHub comments and pull
  requests during this stages-01–03 planning request.

## Success measures and decision boundary

The investigation succeeds when every acceptance criterion has reproducible evidence or an
explicit failed outcome and the recommendation accounts for all discovered compatibility costs.
An inability to preserve a required integration is a reason to recommend retaining Jetty or
limiting a future alternative, not to silently remove that requirement. A production proposal
needs a separate scope decision, capability baseline/delta and compatibility policy.

No blocker to preparing stages 01–03 is known. Dependency/runtime convergence, semantic parity,
cleanup and relative footprint remain experiments. They are not claimed as already proven.

## Capability delta: none — test-only evaluation tooling; no shipped HTTP behavior changes

The future prototype exists only to generate evidence. No current capability requirements are
added, modified, removed or archived by this evaluation. A future published Vert.x server would
require a separate behavioral delta, including its supported configuration surface.

## Stage 1 quality gate

- [x] **Completeness:** stories, acceptance criteria, constraints and exclusions are explicit.
- [x] **Clarity:** evaluation completion is distinct from candidate success and production adoption.
- [x] **Measurability:** criteria name experiments or inspectable evidence deliverables.
- [x] **Feasibility:** the evaluation can report negative results; no known planning blocker.
- [x] **Capability delta:** explicit `none` matches the test-only evaluation scope.
- [x] **Validation:** targeted `capspec.py validate` passed on 2026-10-13 with zero errors and
  zero warnings; zero pending deltas is expected for this evaluation.

Planning continues in [Stage 02](02-architecture.md) and [Stage 03](03-tasks.md).
Stage 04 implementation remains outside the current request.

## Context-path and URL-mapping extension (14 October 2026)

Support one context path and one Citrus dispatch mapping per prototype endpoint. Preserve the
full request path/URI in Citrus headers and expose the configured context path separately.
Match context paths on segment boundaries; return 404 outside the selected context or mapping.
Support default/root (`/`, `/*`), exact (`/orders`), prefix (`/orders/*`), extension (`*.json`)
and context-root-only (empty string) patterns. A bare non-root context redirects to its trailing
slash with HTTP 301, retaining the query string, as observed against Jetty. Settings are captured at start.
This translates URL selection only; custom Servlet instances, filters, forwarding, dispatching
and multiple competing Servlet registrations remain outside scope. Keep default root routing
and production code unchanged. Run under JDK 21 with the Java 17 compiler target.

## Basic authentication extension

Add optional, immutable Basic credentials for all matched prototype routes. Missing, malformed
or incorrect credentials receive 401 with a realm challenge before body buffering, admission
or Citrus adapter invocation. Credentials and realm are captured per startup. Keep authentication
disabled by default. Support printable ASCII credentials (username cannot contain a colon),
passwords containing colons, and case-insensitive scheme names. Reject duplicate Authorization
headers. Scope excludes roles, external login services, per-path policies and TLS.

Use a small immutable credential checker in the existing request handler. This avoids adding
Vert.x Web/Auth dependencies; reusing Jetty security handlers would retain the Servlet dependency.
Verify shared Jetty/prototype socket cases first (red), then parser/configuration edge cases,
restart snapshots, incomplete unauthorized uploads and real Citrus actions. Run the complete HTTP
module on JDK 21 and update the comparison with the precise support boundary.

## Form and multipart conversion extension

Add URL-encoded form reading to a Citrus MultiValueMap and form/multipart writing from a
MultiValueMap. Reuse Spring FormHttpMessageConverter, already on the classpath, rather than
hand-written escaping/MIME boundary logic or a new multipart dependency. Preserve repeated
fields, empty/valueless fields, explicit charsets, filenames, part headers and binary bytes.
Keep inbound multipart as a raw Citrus message, matching the current upload API; no Servlet
Part objects or upload-to-disk API. Configured binary media types retain raw bytes.

Write shared Jetty/prototype socket tests first, then conversion edge cases and real Citrus
receive/send tests. Retain authentication/body-size/timeout checks before conversion and preserve
500 reporting for invalid outbound conversion. Run the clean HTTP module on JDK 21. Document
any parameterized media-type differences instead of asserting complete Spring MVC equivalence.

## Response tracing cache extension

Add a bounded response tracing cache to each prototype generation. This stores converted
response plans keyed by exchange ID; it never serves later requests from the cache. Reuse the
Citrus response-cache-size setting as the default, capture capacity at start, support zero as
disabled, and reject negative capacity. Retain at most N entries, evict oldest insertions, and
return defensive snapshots. Close and clear the cache on stop; late old-generation work cannot
repopulate it. Cache admitted exchanges when their response is submitted, not as proof of delivery.
Route/authentication/admission rejections have no exchange and remain outside this cache.

Choose a synchronized bounded LinkedHashMap with immutable stored snapshots over separate
concurrent map/queue structures, to keep insertion, eviction and shutdown atomic. Do not reuse
Servlet request keys or implement Servlet interceptors. Verify the missing behavior first via
HTTP tests, then immutability, concurrency, capacity boundaries, shutdown and real Citrus actions.
Run the complete HTTP module on JDK 21 and document the cache's purpose and integration limits.

## Custom message-body converter extension

Register ordered Spring HttpMessageConverter instances with explicit inbound payload types.
Capture registrations at startup; use first matching canRead/canWrite converter before default
body conversion. Retain the existing Citrus HttpMessageConverter around the body conversion.
Execute converters on exchange workers, copy the registration list, preserve custom headers,
recompute response framing and retain HEAD/error semantics. Converter instances are caller-owned
and must be thread-safe; no Servlet context, MVC advice or controller argument resolution is added.

Prefer a small typed registration/stream adapter using Spring APIs already present over a new
Servlet dependency or a proprietary serializer API. First reproduce the missing conversion with
HTTP tests, then test ordering, typed conversion, errors, lifecycle snapshots, default fallback
and real Citrus actions. Run the complete HTTP module on JDK 21 and document the exact boundary.

## Gzip extension

Automatically decode gzip requests before body conversion and encode eligible replies when
Accept-Encoding permits gzip. Run compression on exchange workers using JDK gzip streams rather
than adding a dependency or enabling unbounded automatic Netty decompression. Enforce the existing
body-size limit on compressed input and expanded bytes. Reject corrupt gzip with 400, expanded
oversize with 413 and unsupported/stacked request codings with 415. Preserve original request
headers for Citrus tracing. Honor gzip q=0, merge Vary, recompute framing, and avoid double encoding,
HEAD, empty/no-body responses and range responses. Trace cache stores final compressed reply bytes.

Verify shared Jetty/prototype gzip text and binary HTTP cases first, then malformed input,
expansion limits, negotiation and a real Citrus receive/send exchange. Document differences from
the Servlet filter's substring-based encoding selection. Run the clean HTTP module on JDK 21.


## Parsed multipart uploads

Opt-in multipart/form-data decoding produces ordered parts with repeated names, empty fields,
filenames, case-insensitive headers and exact binary file bytes. Text uses each part's declared charset
or UTF-8. Default raw multipart behavior stays unchanged. Bound total decoded input, part count
and per-part headers; malformed/truncated bodies fail before Citrus sees a partial upload.
Parts remain usable after processing without temporary files. This adds parsed upload objects,
not Servlet request.getParts(), Servlet Part.write/delete or automatic file persistence.


## Equivalent handlers and filters

Add opt-in equivalent handlers and filters without a Servlet container or new dependencies.
Filters run in registration order around first-match custom handlers, with Citrus adapter fallback.
They may change converted messages, wrap replies, short-circuit or throw. Run only on exchange
workers after routing, authentication, admission, body reading and conversion. Preserve default
reply correlation, gzip, response framing and tracing. Capture registration lists at startup.
Disallow repeated, deferred and cross-thread continuations; exceptions produce recorded empty 500.
Keep existing shutdown behavior for blocking user code. No Servlet binary compatibility is claimed.


## HTTPS/TLS

Add opt-in HTTPS using Vert.x with JDK TLS, no Jetty connector or new dependency. Accept a
PKCS12 key store and password, default to TLS 1.2/1.3, and optionally require client certificates
from a PKCS12 trust store. Preserve HTTP/1.1 and the existing Citrus request/reply pipeline.
Verify trusted connections, hostname/trust failures, protocol restriction, required client
identity, bad-store startup cleanup and restart configuration. Do not imply TLS benchmarking,
HTTP/2, certificate hot reload or full Jetty connector configuration parity.


## Roles, login services and path policies

Add roles, pluggable external login services and ordered path/method policies without Jetty.
An immutable service identity supplies username and roles. First matching rule permits anonymous
access, requires authentication, requires any listed role, or denies access; unmatched requests
are denied. Use Basic credentials with HTTPS recommended. Invalid credentials yield 401 with a
challenge, insufficient roles yield 403, and service exceptions yield a recorded empty 500.
Do not run login I/O on event loops. Deny before conversion/hooks/Citrus, expose trusted identity
metadata, and reject spoofed principal/role headers. Preserve existing single-user Basic mode.
Verify canonical paths, protocol failures, worker execution, restart snapshots and Citrus actions.
Specific LDAP/OIDC/JAAS connectors and Jetty LoginService binary compatibility remain out of scope.

## WebSocket server extension

Add an opt-in WebSocket endpoint to the same test-only Vert.x listener. Accept text and binary messages (including fragmented messages), expose them to Citrus send/receive actions, broadcast or target replies by session ID, and handle ping/close. Bound sessions, queued messages and message sizes. Apply route and authentication policies to upgrades, support WSS through the existing TLS listener, and isolate sessions and queued messages across restarts. No Jetty or Servlet dependency; no new public DSL, client endpoint, subprotocol negotiation or streaming contract.

## SOAP server extension

Add SOAP 1.1/1.2 request/reply over the native Vert.x HTTP listener. Preserve Citrus SOAP payload/header/action and fault objects, existing SOAP actions, per-request reply correlation and server lifecycle. Reject malformed XML, DTDs and unsupported MIME types before invoking Citrus. Reuse HTTP body limits, routing, authentication and TLS. Verify normal responses, faults, headers, one-way replies, concurrent correlation and real Citrus SOAP client/server actions on JDK 21. Attachments/MTOM, WSDL publishing, WS-Security and Servlet endpoint mappings are outside this extension.
