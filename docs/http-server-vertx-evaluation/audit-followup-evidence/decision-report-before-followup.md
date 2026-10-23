# Decision Report: Vert.x HTTP Server Evaluation (Approach C)

Date: 2026-10-13. Worktree: clean `origin/main` (`e0a55e10e`). JDK 21 only
(Temurin 21.0.12.1, Maven 3.9.8). Java 17 target unchanged. Vert.x 4.5.31, Jetty 12.1.11.
Prototype is test-only: `endpoints/citrus-http/src/test/.../http/vertx/` + one test-scope
`io.vertx:vertx-core` dependency. No production, default, schema, client, or dependency change.

## Verdict per acceptance criterion

| AC | Result |
|---|---|
| AC1 dependency/API inventory | Done. Jetty coupling: `citrus-http` (`HttpServer`, builder, parsers, `SSLConnection`, `SecurityHandlerFactory`, `BasicAuthentication`), `citrus-ws` (own Jetty bootstrap), `citrus-websocket` (`WebSocketServer extends HttpServer`), `citrus-graphql` (`GraphQlServer extends HttpServer`), `citrus-cucumber-http` (Jetty TLS setup), K8s/Knative (create/resolve concrete `HttpServer`), OpenAPI builders, catalog re-export. Compile consumers of `citrus-http`: websocket, graphql, schema-generator, knative, kubernetes, playwright, agent-connector, openapi, test-generator, openapi-codegen, cucumber-http. `citrus-vertx` is event-bus-only (always clustered Hazelcast) — not reusable for HTTP. |
| AC2 config compatibility | Partial by design. Supported: host/port, default status, reply timeout, charset/content-type/binary media types, cookies (parsed from `Cookie` header), semicolon-path removal, per-generation immutable options. NOT supported: TLS/secure port/connectors, basic-auth/security handler, servlet handler/filters/gzip, Spring `contextConfigLocation`/`resourceBase`/root-parent, servlet attribute headers, response cache, interceptors, servlet name/mapping. `contextPath` fixed to root. Full matrix below. |
| AC3 Citrus-controlled roundtrips | Pass. `VertxHttpServerRoundtripIT`: GET/POST/PUT/PATCH/DELETE/OPTIONS/HEAD/TRACE, each sent by the Citrus HTTP client, received + answered via Citrus server actions on the prototype, reply validated by the Citrus client (1/1 green). |
| AC4 payload/reply semantics | Pass. `VertxHttpServerLimitsIT` 10/10: UTF-8 + latin-1 charset, empty body, byte-identical binary, explicit 201, missing status -> configured default (202 tested), null reply -> 200 empty, reply timeout -> 200 empty (never 504), HEAD suppresses body but keeps `Content-Length`. |
| AC5 concurrency/failure isolation | Pass. `VertxHttpServerConcurrencyIT` 5/5: 2 concurrent exchanges with reverse-order replies and correct pairing; adapter never on `vert.x-eventloop-*`; adapter exception -> 500 empty with no leak of the failure text; client disconnect mid-handling tolerated, server keeps serving; admission saturation -> 503 without invoking the adapter. |
| AC6 lifecycle | Pass. `VertxHttpServerLifecycleIT` 6/6: no socket after construct/initialize; bind on start with ephemeral port exposure; failed bind raises `CitrusRuntimeException`, server stays stopped; repeated stop/destroy safe; generations increment with fresh adapter+queue; fixed port reusable across 3 generations; bounded termination. |
| AC7 coexistence | Pass. `VertxHttpServerCoexistenceIT` 2/2: Jetty + Vert.x serve side by side on distinct ports; stopping Vert.x leaves Jetty serving. |
| AC8 footprint | Measured, 5 fresh JVMs per engine (evidence `endpoints/citrus-http/target/vertx-evaluation/`). Table below. |
| AC9 existing behavior unchanged | Pass. `git status`: only `endpoints/citrus-http/pom.xml` (test-scope dep) modified; all prototype code under `src/test`. Regression sample `HttpServerJavaIT,HttpServerStandaloneJavaIT` 2/2 green on JDK 21. Unit suite 40/40. |
| AC10 this report | Done. |

## Footprint comparison (5 fresh JVMs/engine, 100 warmup + 500 sequential GETs, JDK 21)

| Metric (avg of 5) | Jetty `HttpServer` | Vert.x prototype | Delta |
|---|---|---|---|
| Startup to bound (ms) | 697 | 350 | ~2x faster (no Spring MVC/servlet init on the Vert.x path) |
| Live threads after load | 21 | 37 | +16 (Vert.x event-loop pool + 8 exchange workers) |
| Heap used after GC (MB) | 18.1 | 16.7 | -8% (proxy metric, same workload) |
| Sequential GET p50 (ms) | 0.74 | 0.61 | -18% |
| Sequential GET p95 (ms) | 1.12 | 0.92 | -18% |
| Engine JARs on disk | jetty-server 0.71 MB + jetty-ee11-servlet 0.46 MB (+ jetty-http/io/util) | vertx-core 1.68 MB + Netty stack ~4.5 MB | Vert.x notably heavier (~6 MB vs ~2.5 MB) |
| Test-classpath totals | 157 jars / 75.1 MB (shared; both engines present in test scope) | same | n/a (production delta would be the row above) |

Caveats: single-machine loopback numbers, sequential load only, fixed "pong" reply. They compare
engine overhead, not framework validation cost. Raw evidence: `footprint.jsonl`, `environment.txt`,
`jars.txt`. Harness: `src/test/resources/vertx-evaluation/run-footprint.sh` + `FootprintProbe`.

## Config compatibility matrix (Jetty `HttpServer` vs prototype)

| Option | Jetty | Prototype |
|---|---|---|
| port/host, default status, timeout | yes | yes (host loopback default, port 0 default) |
| charset/content-type/binary media types | yes | yes |
| handleCookies | yes (servlet) | yes (parsed from `Cookie` header; `Set-Cookie` on reply) |
| removeSemicolonPathContent | yes | yes |
| contextPath | configurable | root only |
| TLS (secured/securePort/connectors) | yes | no |
| authentication/securityHandler | yes | no |
| servletHandler/filters/gzip | yes | no |
| contextConfigLocation/resourceBase/rootParent | yes | no |
| handleAttributeHeaders | yes (servlet attrs) | no (no servlet; documented) |
| responseCacheSize/interceptors/debugLogging | yes | no |
| autoStart/actor/destroy lifecycle | yes | yes (inherited `AbstractServer`) |

## Alternatives considered

1. **Stay on Jetty 12 (recommended).** Wire parity for plain HTTP is achievable on Vert.x, but
   the expensive surface (TLS, auth, servlet filters, Spring MVC dispatch, WS/SOAP bootstraps) is
   exactly what would need re-implementation, and the measured gains are modest outside startup
   time. Jetty 12 already supports virtual threads, which is the cheaper path to thread economy.
2. **Full Vert.x port.** Requires re-implementing the matrix rows above plus `citrus-ws` SOAP
   bootstrap, `citrus-websocket` WS initializer, GraphQL server, cucumber TLS steps and K8s/Knative
   coupling — a cross-module effort an order of magnitude larger than this prototype.
3. **Hybrid (Vert.x plain-HTTP only).** Rejected: two engines doubles the support surface while
   TLS/auth users stay on Jetty anyway.
4. **Undertow/Tomcat.** No evidence gathered; same porting cost as Vert.x with less ecosystem
   overlap (Vert.x is already a managed dependency via Fabric8).

## Notes on #1807 and SOAP

- `citrus-ws` `WebServiceServer` owns a separate Jetty bootstrap (not a subclass of `HttpServer`);
  any engine change must port it independently, including SOAP fault serialization paths.
- No conclusion is drawn here on issue #1807 beyond: the prototype does not cover WS-Security,
  MTOM, or SOAP-specific dispatch, so #1807 behavior is unaffected (AC9) and out of scope.

## Recommendation

Keep Jetty 12 as the HTTP server engine. The prototype proves Vert.x can serve Citrus-controlled
plain-HTTP roundtrips with correct payload, concurrency, lifecycle and coexistence semantics, and
it starts ~2x faster with ~18% lower loopback latency — but it costs more threads, a heavier
dependency footprint (Netty), and a large compatibility tail (TLS, auth, servlet integration,
SOAP/WS subclass bootstraps). Revisit only if server startup time becomes the dominant cost and
someone funds the compatibility tail. Maintainers to confirm.

## How to reproduce

```bash
export JAVA_HOME=<jdk21>   # Temurin 21.0.12.1 used here; ~/.zshenv re-pins JAVA_HOME in new zsh,
                           # so run Maven in the same shell as the export, not via #!/bin/zsh scripts
./mvnw -pl endpoints/citrus-http verify \
  -Dtest='Vertx*Test' -Dit.test='Vertx*IT' \
  -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false
./endpoints/citrus-http/src/test/resources/vertx-evaluation/run-footprint.sh \
  <module-dir> <classpath-file> <evidence-dir> 5 500   # script pins JDK 21 itself, aborts otherwise
```

Note: `*IT` classes must run under failsafe (`verify`), not surefire — the Citrus Spring bootstrap
in `citrus-context.xml` resolves a relative Jetty `resourceBase` that only holds with the module
working directory. Test counts must be nonzero (40 unit + 24 IT here); no blanket skips.
