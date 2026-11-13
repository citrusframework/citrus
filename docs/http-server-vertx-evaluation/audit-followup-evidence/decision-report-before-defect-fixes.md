# Evaluating Vert.x for the Citrus HTTP server

Updated 14 October 2026 after reviewing the prototype and rerunning its focused tests. This report concerns [Citrus issue #1808](https://github.com/citrusframework/citrus/issues/1808).

Keep Jetty in place while the evaluation is completed. The prototype shows that Vert.x can serve plain HTTP requests through existing Citrus receive and send actions. It also exposes lifecycle and compatibility problems that need to be fixed before the experiment can support a migration recommendation. The earlier report overstated both the completed verification and the performance evidence; the findings below replace those conclusions. Corrective code work is currently blocked by the broader regression baseline described below.

This remains a test-only experiment based on commit `e0a55e10e6de5617c6046f1e6b8f40eb2b75fdf0`, with uncommitted prototype additions. It adds `io.vertx:vertx-core` in test scope and leaves the production server, HTTP client, endpoint registrations and configuration schemas unchanged. All execution used Temurin JDK 21.0.12.1 and Maven 3.9.8. The Java 17 compiler target is unchanged. The managed engine versions are Vert.x 4.5.31 and Jetty 12.1.11.

## Why corrective work stopped

Before changing the prototype, the full selected regression command was run on JDK 21. It reached the HTTP module, where all 277 unit tests passed. The integration suite then failed while initializing the Spring test context: `helloHttpServer` could not bind to `0.0.0.0:11080` because the address was already in use.

Failsafe reported 198 entries: 133 failures, no errors and 65 skips. Those figures include repeated TestNG configuration failures following the initial bind error. The build stopped at `citrus-http`, so the required WebSocket, SOAP, OpenAPI and HTTP Cucumber modules did not run. No listener was found on port 11080 after Maven exited; the source of the contention has not been established.

This happened before any corrective code edits. In accordance with the instruction to stop at a blocker, no prototype fixes or replacement benchmark runs were attempted. The audit defects below remain open. Work can resume once the port contention is understood and the required baseline can run without bypassing tests or changing the dependency baseline. The [baseline summary](../audit-followup-evidence/regression-baseline-summary.json) and [full Maven log](../audit-followup-evidence/regression-baseline.log) preserve the result.

## What the experiment establishes

The focused test selection passed again during the audit: 40 unit tests and 24 integration tests, with no failures, errors or skips. Those counts cover the prototype tests, not the full existing HTTP suite or the dependent modules.

Real requests passed through the Citrus HTTP client, the prototype's server receive and send actions, and back to client validation for GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD and TRACE. Separate candidate tests covered text, an explicit non-UTF-8 charset, binary and empty bodies, response status defaults, null replies, reply timeouts and HEAD body suppression. Jetty and Vert.x also served requests in the same JVM on separate ports, and Jetty continued serving after the Vert.x endpoint stopped.

These results establish a useful starting point. Identical payload and response fixtures still need to run against both engines before we can claim that their behavior matches. Wire tests also need to check query parameters, context and protocol metadata, and demonstrate that a deliberately incorrect Citrus expectation fails.

## What the audit found

Stopping and restarting the prototype is not safe yet. A worker remained alive after `stop()`, calling `start()` twice left the first listener open, and a request queued before shutdown reached the fresh default Citrus queue after restart. Calling `destroy()` directly also left the server marked as running even though its listener had closed. These were reproduced with diagnostic probes; the existing lifecycle tests did not cover them.

The configured resource limits do not consistently control resource use. An upload is buffered in full before its size is checked or an admission permit is acquired. In the probe, a chunked upload exceeded a four-byte limit but received no rejection until the client finished sending it. Changing the admission limit after initialization could also leave the executor with 32 permits while the active options reported a limit of one. Source inspection found that the body-read timeout does not stop the upload and that an old worker can release a permit into a later executor. Those last two cases still need dedicated runtime tests.

Several mapping and response paths also need attention. A malformed response status caused an uncaught worker exception and left the client waiting until its own timeout. A reply could contain both `Transfer-Encoding` and `Content-Length`. Cookie serialization dropped `Secure`, `HttpOnly`, `Max-Age` and `SameSite`; a configured charset was overwritten at startup. These behaviors were reproduced. Source inspection also found differences from Jetty in decoding paths and removing semicolon parameters.

The existing reverse-order reply test uses a custom adapter that returns each reply directly to its worker. It therefore does not verify concurrent correlation through Citrus's default temporary reply queues. That requires a separate test using the normal Citrus receive and send actions, including late replies and requests held across shutdown.

The [audit report](../audit-report.md) contains the source locations, individual findings and probe evidence. The original decision report is preserved [with the follow-up evidence](../audit-followup-evidence/decision-report-before-followup.md). None of the audit defects has been corrected in code during this follow-up.

## Performance and dependency evidence

The original campaign used five fresh JVMs per engine, with 100 warmup requests and 500 sequential GET requests returning a fixed response. These are the recorded averages:

| Measurement | Jetty | Vert.x prototype |
|---|---:|---:|
| Server construction/start to bound port | 697 ms | 350 ms |
| Live threads after load | 21 | 37 |
| Heap usage after a GC request, as a rough proxy | 18.1 MB | 16.7 MB |
| Per-run median request latency, averaged across runs | 0.74 ms | 0.61 ms |
| Per-run 95th-percentile latency, averaged across runs | 1.12 ms | 0.92 ms |

These observations do not establish an engine performance advantage. The audit reproduced an uncontrolled difference in the workload: the default JDK client negotiated HTTP/1.1 with Jetty and HTTP/2 with Vert.x. The latency, thread and heap comparison must be repeated with an explicitly selected, verified protocol. The startup numbers also compare different functionality: the prototype omits the Spring MVC and Servlet setup used by the existing server, and the timer does not measure JVM process startup.

The planned campaign calls for eight concurrent clients, text and binary fixtures, 1,000 measured requests per fixture, raw request samples, and medians and ranges across runs. The original campaign did not provide that evidence. Machine details, JVM flags and a complete resolved dependency inventory also need to be retained with the results.

The saved JAR accounting describes a shared test classpath containing both engines: 157 JARs, reported as approximately 75.1 MB. It does not establish the size of a future production deployment. The earlier estimate of a roughly 6 MB Vert.x stack versus 2.5 MB of Jetty is withdrawn as a supported production comparison. A replacement estimate needs a complete dependency graph and must account for modules that would still require Jetty. The saved classpath also contains mixed Netty versions; a dependency-convergence assessment is still required. The audit did not demonstrate an actual linkage failure.

## Compatibility and dependent modules

The current prototype covers a subset of plain HTTP. Host, port, basic status handling and reply timeout have executable coverage. Charset, content type, binary media configuration, cookies and path handling have limitations described above, so they cannot yet be labeled fully compatible. The endpoint is mounted at the root context.

TLS, authentication, Servlet handlers and filters, gzip, Spring web contexts, Servlet attribute headers, response caching and interceptors remain outside the prototype. Existing configurations using those features continue to use Jetty. Concrete retained-Jetty and migration examples are still required before the configuration assessment is complete.

The dependency inventory identifies more than the HTTP server itself. `citrus-websocket` and `citrus-graphql` extend `HttpServer`; the HTTP Cucumber steps configure Jetty TLS; Kubernetes, Knative and OpenAPI code construct or resolve HTTP servers. Other consumers include schema and test generation, OpenAPI code generation, Playwright, the agent connector and catalog exports. The existing `citrus-vertx` module provides event-bus endpoints, not an HTTP server replacement.

[#1807](https://github.com/citrusframework/citrus/issues/1807) concerns WebSocket modernization. Its relevance here is the WebSocket server's coupling to the Jetty-based HTTP server and the possibility of sharing a future Vert.x HTTP/WebSocket transport. This prototype does not test that migration or resolve its configuration compatibility. SOAP is a separate concern: `citrus-ws` owns its own Jetty bootstrap and needs an independent transport assessment. The absence of SOAP features in this prototype says nothing about whether #1807 is complete or unaffected.

## Status against the agreed evaluation

| Criterion | Current conclusion |
|---|---|
| AC1: dependency and API inventory | A useful inventory exists; the WebSocket reuse and transition assessment remains incomplete. |
| AC2: configuration compatibility | Partial. Several mappings are defective and concrete configuration examples are missing. |
| AC3: Citrus-controlled round trips | Basic method/action round trips pass; metadata and deliberate mismatch checks are missing. |
| AC4: payload and reply parity | Candidate tests pass; the shared Jetty comparison is missing. |
| AC5: concurrency and failure isolation | Incomplete. Default-adapter correlation is unproven and requests cross restart boundaries. |
| AC6: lifecycle | Fails the agreed cleanup and restart-isolation requirements. |
| AC7: coexistence | Basic HTTP coexistence passes. The broader baseline is blocked; SOAP/WebSocket regressions and dependency-convergence checks remain incomplete. |
| AC8: measurements | Preliminary observations only; a controlled campaign and complete dependency accounting are missing. |
| AC9: existing behavior | Production scope is unchanged. The required baseline failed in HTTP integration setup and the downstream modules were not reached. |
| AC10: reviewable conclusion | This report corrects the conclusions. The configuration examples, stage-05 documentation and remaining evidence are still outstanding. |

The original evidence included two existing HTTP regression classes, `HttpServerJavaIT` and `HttpServerStandaloneJavaIT`, passing. That sample does not cover the required HTTP, WebSocket, SOAP, OpenAPI and HTTP Cucumber selection. The broader baseline attempted during this follow-up failed as described above. The earlier focused passes remain useful historical evidence, but they do not clear this blocker.

## Recommended next step

Retain Jetty for current users and complete the bounded experiment before deciding whether to continue toward replacement. First establish why the required HTTP integration baseline contends for port 11080. After resolving that blocker, fix the reproduced prototype defects, turn the diagnostic cases into regression tests, add shared Jetty fixtures and real Citrus correlation tests, and repeat the measurements under the agreed workload. Record any required regression failure or missing prerequisite as a blocker.

A full replacement would require a separate plan for the dependent modules and the configuration features outside this prototype. An optional Vert.x endpoint could allow gradual adoption, but it would leave two server implementations to support and needs its own compatibility proposal. Neither option has enough evidence here for an effort estimate. Other engines and Jetty virtual-thread tuning were not measured, so this report makes no comparative claim about them.

## Reproduction and evidence

From the repository root, select a JDK 21 installation and run the prototype tests:

```sh
export JAVA_HOME=/absolute/path/to/jdk-21
./mvnw verify -pl endpoints/citrus-http \
  '-Dtest=Vertx*Test' '-Dit.test=Vertx*IT' \
  -Dsurefire.failIfNoSpecifiedTests=false
```

The audit used the same command with Maven's `-o` option because the configured snapshot metadata repository timed out; all required artifacts for this focused run were already cached. The saved XML confirms that both test forks used JDK 21. Run integration tests through `verify` so Failsafe uses the module working directory required by the Jetty test resources.

The required broader regression selection is shown below. This follow-up ran it with `-o` using cached dependencies and stopped at the HTTP integration failure:

```sh
./mvnw verify \
  -pl endpoints/citrus-http,endpoints/citrus-websocket,endpoints/citrus-ws,connectors/citrus-openapi,tools/cucumber-steps/citrus-cucumber-http \
  -am
```

Supporting material includes the [focused test counts](../audit-evidence/verification-summary.json), [Maven verification log](../audit-evidence/maven-offline-verify.log), [diagnostic probe](../audit-evidence/AuditProbe.java), [primary probe results](../audit-evidence/probe.log), [restart probe results](../audit-evidence/generation-probe.log) and [original measurement samples](../audit-evidence/original-footprint.jsonl). The original measurement harness is retained as preliminary evidence; rerunning it unchanged does not resolve the protocol or workload problems described above.
