# 04 — Implementation (Vert.x HTTP Server Evaluation, Approach C)

Scope: test-only prototype + evidence. No production, default, schema, client, or dependency changes.
JDK 21 only for all Maven invocations (`JAVA_HOME=<jdk21>`). Java 17 target unchanged.

## Progress

- [x] T1 dependency/API inventory (evidence in session summary; full matrix goes to T15 report)
- [x] T3 test-scope `io.vertx:vertx-core` added to `endpoints/citrus-http` (no other pom change)
- [x] T5 `VertxHttpServerOptions` + `VertxExchangeExecutor` with unit tests
- [x] T4 `VertxHttpMessageMapper` with unit tests (inbound/outbound parity with `HttpMessageController`)
- [x] T6/T7 `VertxHttpServer` prototype (lifecycle + adapter bridge)
- [x] T8 wire-matrix roundtrip IT (GET/POST/PUT/PATCH/DELETE/OPTIONS/HEAD + TRACE)
- [x] T9 concurrency/failure-isolation IT
- [x] T10 limits IT (413/503/500/timeout/null-reply/HEAD/binary)
- [x] T11 shutdown/generations IT (in lifecycle IT: 3 fixed-port generations, pending-stop via gated adapter in concurrency IT)
- [x] T12 coexistence + regression run (2/2 coexistence; HttpServerJavaIT + HttpServerStandaloneJavaIT 2/2 green)
- [x] T2 baseline Jetty figures (regression sample green; footprint Jetty column is the baseline)
- [x] T13/T14 footprint harness + 5-run campaign under `target/vertx-evaluation`
- [x] T15 decision report (`docs/http-server-vertx-evaluation/decision-report.md`)

## Prototype layout (all under `endpoints/citrus-http/src/test`)

Package `org.citrusframework.http.vertx`:

- `VertxHttpServerOptions` — immutable, validated, builder; snapshot per generation.
- `VertxExchangeExecutor` — endpoint-owned fixed pool (8) + semaphore admission (32).
- `VertxHttpMessageMapper` — `RequestSnapshot`→`HttpMessage` (via `HttpEndpointConfiguration`
  converter, controller-compatible enrichment); `Message`→`ResponsePlan` (status/headers/bytes).
- `VertxHttpServer extends AbstractServer` — Vert.x `HttpServer` on `127.0.0.1`, port 0 default;
  snapshot request on event loop, `EndpointAdapter.handleMessage` on executor, write on Vert.x
  context; terminal guard (1 response/request); 413/503/500/408 fast paths; HEAD suppression;
  per-generation fresh adapter+queue; idempotent stop/destroy; bounded termination.

ITs under `org.citrusframework.http.integration` (`*IT`, failsafe, TestNG):

- `VertxHttpServerLifecycleIT`, `VertxHttpServerRoundtripIT`, `VertxHttpServerConcurrencyIT`,
  `VertxHttpServerLimitsIT`, `VertxHttpServerCoexistenceIT`

## TDD log

- RED: unit tests added before impl classes; `mvn -Dtest='Vertx*Test' test` fails compilation.
- GREEN: impl added; unit tests + ITs green on JDK 21 (versions recorded in evidence files).
- Conventions: TestNG, Apache license headers, `*Test` vs `*IT` naming, nonzero test counts.

## Decisions / deviations

- Plain Vert.x `requestHandler` (no `vertx-web`, core only) — fewer deps, full method control.
- `contextPath` fixed to `""` (root-mounted, matches Jetty root context `getContextPath()`).
- Servlet attribute headers unsupported (no servlet); cookies parsed from `Cookie` header.
- Body-read timeout → `408` empty (prototype-defined, documented in decision report).
- Adapter exception → `500` empty, warn log without stacktrace (no leak to client).
- Timeout/null reply → empty body + configured default status (never 504).
