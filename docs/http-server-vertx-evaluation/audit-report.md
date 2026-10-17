# Audit: Vert.x HTTP evaluation for Citrus #1808

The evaluation is not complete against its approved specification and architecture. It establishes useful plain-HTTP prototype behavior, but the lifecycle/resource guarantees fail in diagnostic runs, important comparison and regression checks are absent, and the reported latency comparison uses different HTTP protocols. Keeping Jetty remains a possible recommendation; the current report does not establish all the reasons it claims for that decision.

Audited 2026-10-14 in `/Users/asuruceanu/development/projects/citrus-vertx-eval`, detached HEAD `e0a55e10e6de5617c6046f1e6b8f40eb2b75fdf0`, including uncommitted prototype changes and the locally excluded stage 01–04 documents and decision report. No implementation source or existing decision document was changed. This audit adds only its own local report/evidence files; they inherit the existing documentation exclusion.

Review lenses: adversarial, edge cases, verification gaps, and document structure. The independent reviewers' overlapping findings are retained and labeled by lens. IDs identify findings; they do not rank them. The customization resolver was absent in this worktree, so the installed bmad-review defaults were used.

## Verification performed

- Reran `./mvnw -o verify -pl endpoints/citrus-http '-Dtest=Vertx*Test' '-Dit.test=Vertx*IT' -Dsurefire.failIfNoSpecifiedTests=false` with explicit JDK 21.0.12.1 `JAVA_HOME`. Maven 3.9.8 completed successfully: **40 unit tests and 24 integration tests; zero failures, errors or skips**. XML properties confirm the JDK of both forks. This was the prototype selection, not the required multi-module regression selection.
- The initial online Maven attempt stalled on the configured private snapshot metadata repository and was stopped. Offline execution succeeded using existing dependencies; no build configuration workaround was introduced.
- Compiled the isolated diagnostic probe with JDK 21 and `--release 17`, then executed two bounded probe runs on JDK 21. They exited successfully after recording the observations below. The probes live in the audit evidence folder, outside the implementation/test source tree.
- `capspec.py validate docs/http-server-vertx-evaluation/sdd` returned 0 errors and 0 warnings, checking **zero capability files and zero pending deltas**. It does not validate code, task completion, or missing documentation deliverables.
- Preserved the original saved test/evaluation evidence before rerunning tests. The audit includes copies of the original measurement JSONL, environment record and JAR totals. Full original reports remain in the temporary audit snapshot recorded below.

## Reproduced observations

| Scenario | Observed result |
|---|---|
| Stop after a completed request | An owned exchange worker remains alive in WAITING. |
| Start twice on an ephemeral port, then stop/destroy | The first listener still accepts connections. The probe explicitly cleans up the leaked instance. |
| Queue a request before stop/restart | Generation 2's fresh default Citrus queue receives a request tagged generation 1. |
| Destroy while running, then initialize with autoStart | `isRunning()` remains true with `actualPort=-1`; no listener restarts. |
| Change admission limit after initialize | Active options report 1; the executor retains 32 permits. |
| Incomplete 8-byte chunked upload with a 4-byte limit | No rejection until upload completion; then 413. |
| Reply with malformed status | Uncaught worker conversion exception; client timeout instead of a controlled terminal response. |
| Set charset before startup | ISO-8859-1 is overwritten with UTF-8. |
| Reply cookie with security/expiry attributes | Header contains only `token=value`; attributes disappear. |
| Reply with Transfer-Encoding | Raw HTTP/1.1 response also includes Content-Length. |
| Benchmark-style default JDK client | Jetty negotiates HTTP/1.1; Vert.x negotiates HTTP/2. |

These are eleven concrete runtime observations. Other findings below are source-traced edge cases or missing-verification/document findings; they are not presented as experimentally reproduced.

## Acceptance evidence after audit

| Criterion | Audit disposition |
|---|---|
| AC1 inventory | Useful inventory, but linked #1807 analysis confuses WebSockets with SOAP. |
| AC2 configuration | Partial prototype subset; several options labeled supported are overwritten or incompletely mapped. Required migration/configuration examples are missing. |
| AC3 controlled round trips | Method/action round trips pass; query/context/protocol assertions and deliberate Citrus mismatch proof are absent. |
| AC4 semantic parity | Candidate fixtures pass; identical Jetty fixtures were not delivered. |
| AC5 concurrency/isolation | Custom-adapter tests pass; concurrent Citrus reply-queue correlation is unproven, and queued work crosses restart generations. |
| AC6 lifecycle | Does not meet the specified cleanup and generation-isolation contract. |
| AC7 coexistence | Basic same-JVM HTTP coexistence passes; required SOAP/WebSocket regression and dependency-convergence evidence is incomplete. |
| AC8 measurements | Preliminary measurements exist; protocols differ, planned workload and evidence are incomplete, and runtime dependency deltas are not demonstrated. |
| AC9 existing behavior | Test-only scope preserved; the required regression selection has not been accounted for. |
| AC10 conclusion | Decision report exists, but completion claims, #1807 analysis, examples and stage-05 deliverables need reconciliation. |

The original issue concerns evaluating the HTTP engine and dependent modules. Its linked [#1807](https://github.com/citrusframework/citrus/issues/1807) concerns WebSocket modernization; SOAP is a separate consumer identified by [#1808](https://github.com/citrusframework/citrus/issues/1808). No production migration is demanded by this audit.

## Findings by lens

Overlap is intentional: adversarial findings A01–A06 overlap several edge findings; A07–A09 overlap verification gaps V01–V05. One is the defect or unsupported conclusion; the other describes the boundary case or missing test that lets it escape. These are not thirty independent bugs.

### Adversarial

**A01 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:433–471](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java)**

- Trigger/problem: An oversized or concurrent slow request is fully aggregated by `request.body()` before either the body-size check or admission permit is applied.
- Guard or missing check: Acquire admission before reading; count streamed chunks and terminate the read as soon as the configured limit is exceeded. Cancel/drain or close timed-out reads. Add chunked oversize and incomplete-body tests required by T10.
- Consequence: The advertised 1 MiB/32-request limits do not bound buffered memory or concurrent body readers. The existing oversize test establishes eventual 413 only, while T10 is marked complete.
- Audit observation: probe.log: no rejection while an 8-byte chunked body was incomplete with maxBodySizeBytes=4; 413 only after the final chunk.

**A02 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:224–263](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java)**

- Trigger/problem: `stop()` is called after requests have created exchange-worker threads, without immediately calling `destroy()`.
- Guard or missing check: Terminate the endpoint-owned executor within the shutdown budget and create a fresh executor on restart; verify named worker termination after every stop, including pending work.
- Consequence: `shutdown()` closes only the listener and Vert.x instance. The fixed pool remains alive, with no idle timeout, until destruction. AC6/T11 claim owned-resource cleanup and bounded termination without testing or implementing it at the start/stop-generation boundary.
- Audit observation: probe.log: auditStoppedWorkers-exchange-1 remained alive in WAITING after stop().

**A03 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:299–304](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java)**

- Trigger/problem: Requests remain queued behind a blocked worker while the server stops and restarts.
- Guard or missing check: Bind each exchange to its original adapter, executor and generation; reject obsolete queued exchanges before adapter invocation, and terminate/cancel the old generation on stop. Test that no old request satisfies a new-generation Citrus receive.
- Consequence: `handleExchange()` looks up the current adapter when the queued task eventually runs. The executor survives stop, startup replaces the default adapter, and the stored generation is never checked. A request from the old listener can therefore enter the new generation’s queue, contradicting generation isolation.
- Audit observation: generation-probe.log: current generation 2 fresh default queue received second-old from generation 1.

**A04 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:330–337](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java)**

- Trigger/problem: Outbound conversion throws after an adapter successfully supplies a reply, for example through the publicly configurable message converter.
- Guard or missing check: Catch conversion failures, record the exchange failure for the harness, and complete the transport with the defined error response while releasing the permit exactly once.
- Consequence: The outer block has only a `finally`; the worker fails and releases admission without completing or closing the HTTP response. The client waits for its own timeout, although the evaluation claims failure isolation and terminal outcomes.
- Audit observation: probe.log: HTTP status header bad threw NumberFormatException on the worker and the client timed out.

**A05 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpMessageMapper.java:283–292](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpMessageMapper.java)**

- Trigger/problem: A reply cookie includes `Max-Age`, `Secure`, `HttpOnly`, or other attributes beyond Path and Domain.
- Guard or missing check: Preserve supported Servlet cookie attributes in `Set-Cookie` and verify them on the wire against Jetty, or explicitly classify the narrower subset in the compatibility matrix.
- Consequence: The renderer silently discards those attributes. A deletion cookie with `Max-Age=0`, for example, becomes an ordinary cookie. The report’s unconditional cookie-support classification is inaccurate.
- Audit observation: probe.log: a cookie configured with Max-Age=0, Secure, HttpOnly and SameSite=Strict rendered only token=value.

**A06 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:276–283](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java)**

- Trigger/problem: A caller configures charset, content type, binary media types, or semicolon-path policy through `getHttpEndpointConfiguration()` before starting.
- Guard or missing check: Expose those options through the actual server configuration path, or snapshot the supplied HTTP configuration without overwriting it. Add a non-default configuration integration test.
- Consequence: Startup overwrites the settings from the private `draft`, for which the server exposes no corresponding setters or options-injection method. Custom binary types can consequently decode as text, despite the report marking these options supported.
- Audit observation: probe.log: configured ISO-8859-1 was reset to UTF-8 at startup.

**A07 — [docs/http-server-vertx-evaluation/decision-report.md:18–20](decision-report.md)**

- Trigger/problem: AC7 and AC9 are declared passed using coexistence tests and two HTTP regression classes.
- Guard or missing check: Account for the required WebSocket, SOAP, OpenAPI, HTTP Cucumber, TLS/authentication and configuration regressions with current run counts and dependency-convergence evidence; leave unmet criteria incomplete or report their actual blocked/failed status.
- Consequence: The report closes the regression gate without the evidence explicitly required by AC7/AC9 and T2/T12. Test-scope isolation alone does not establish that the added dependency causes no test-classpath linkage or mediation regressions in affected consumers.

**A08 — [docs/http-server-vertx-evaluation/decision-report.md:14–15](decision-report.md)**

- Trigger/problem: AC3/AC4 are reported passed from Vert.x-only fixtures without the specified shared Jetty comparison and deliberate Citrus mismatch case.
- Guard or missing check: Run identical request/reply fixtures against both engines, retain engine-specific outcomes, and add an asserted Citrus expectation failure. Include the required context/protocol and encoded, duplicate and valueless query checks.
- Consequence: The current tests establish that selected prototype happy paths work, rather than the claimed compatibility with current HTTP behavior. No `JettyHttpServerContractIT` or shared fixture matrix was delivered, although T3/T8 are represented as complete.

**A09 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServerConcurrencyIT.java:57–80](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServerConcurrencyIT.java)**

- Trigger/problem: The reverse-order test is used as evidence that Citrus’s request/reply correlation is preserved.
- Guard or missing check: Exercise two concurrent requests through the default `DirectEndpointAdapter` and existing Citrus receive/send actions, using explicit correlation or separate contexts; add late-reply and cross-server isolation cases.
- Consequence: The custom gated adapter returns each response directly to its invoking worker and bypasses Citrus temporary reply queues entirely. Thus it cannot expose the correlation hazards called out in T9, yet AC5 is marked passed.

**A10 — [docs/http-server-vertx-evaluation/decision-report.md:23–37](decision-report.md)**

- Trigger/problem: The five-run sequential GET campaign is treated as completion of the planned footprint work and its partial JAR accounting is described as the production dependency delta.
- Guard or missing check: Reconcile the campaign with T13/T14: preserve resolved artifact names/versions/sizes, machine and JVM provenance, request samples and median/range; run the specified text/binary workload and concurrency or explicitly leave that work incomplete. Label any future packaging calculation as a model using a complete dependency graph.
- Consequence: The saved evidence contains aggregate metrics from five runs, combined test-classpath totals and minimal environment data, but lacks the runtime dependency inventory, raw request samples, hardware metadata and planned workload. The approximately 6 MB versus 2.5 MB estimate cannot establish the stated production delta from `jars.txt`, which records only the shared classpath’s total count and bytes. The measured sequential results are usable as preliminary observations, but do not support closing T13/T14 as written.

**A11 — [docs/http-server-vertx-evaluation/decision-report.md:70–75](decision-report.md)**

- Trigger/problem: The section addressing #1807 substitutes SOAP/WS-Security/MTOM scope for the linked WebSocket modernization issue.
- Guard or missing check: Describe WebSocketServer coupling, Vert.x HTTP/WebSocket reuse and transition options for #1807; discuss citrus-ws SOAP independently and label any untested behavior.
- Consequence: The recommendation fails to evaluate a stated motivation of #1808, and the assertion that #1807 is unaffected is not supported by the SOAP exclusions.
- Evidence: Checked the issue bodies on 2026-10-14. https://github.com/citrusframework/citrus/issues/1807 concerns Jetty-coupled WebSockets; https://github.com/citrusframework/citrus/issues/1808 explicitly links HTTP migration to that work. Stage 03 T15 also separates #1807 reuse from SOAP's independent transport.

**A12 — [docs/http-server-vertx-evaluation/sdd/04-implementation.md:20](sdd/04-implementation.md)**

- Trigger/problem: T15 is checked complete while its required 05-documentation.md, migration examples and reconciled command/evidence record are absent.
- Guard or missing check: Complete the named documentation deliverables and reconcile task/AC status with observed evidence, or explicitly revise the plan and leave undelivered work incomplete.
- Consequence: The recorded completion cannot be traced to the agreed deliverables; future reviewers cannot distinguish accepted scope changes from omitted work.
- Evidence: Stage 03 T15:382–401 requires 05-documentation.md, ordinary HTTP/TLS/custom-Servlet examples and consistent commands. Only stages 01–04 and decision-report.md exist. The capability validator succeeds but checks zero capability files and zero pending deltas; it is not a check of those documentation deliverables.

### Edge cases

**E01 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:433-447](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java)**

- Trigger/problem: Large or concurrent request bodies arrive before admission is checked.
- Guard or missing check: Acquire admission before reading; enforce maxBodySizeBytes incrementally before appending each chunk.
- Consequence: Entire oversized bodies accumulate in memory, bypassing both advertised resource limits.
- Audit observation: See runtime observation A01.

**E02 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:427-430](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java)**

- Trigger/problem: A client continues an incomplete upload after receiving the body-read timeout response.
- Guard or missing check: After the 408 write completes, close/reset the request and stop accumulating its body.
- Consequence: The body accumulator and connection remain active beyond the configured deadline.

**E03 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:175-198](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java)**

- Trigger/problem: start() is called twice while the server already runs on an ephemeral port.
- Guard or missing check: Serialize lifecycle entry points and return from start() when isRunning().
- Consequence: The first listener and Vert.x instance become unreachable and survive subsequent stop/destroy.
- Audit observation: probe.log: first listener still accepted a TCP connection after two starts followed by stop and destroy; the diagnostic explicitly closed the leaked instance afterward.

**E04 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:299-304](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java)**

- Trigger/problem: Old requests remain queued in the shared executor across stop/start.
- Guard or missing check: Capture adapter/configuration/context per generation; close admission and terminate queued work during stop.
- Consequence: Old requests can enter the new default adapter queue and satisfy new-generation receives.
- Audit observation: See runtime observation A03.

**E05 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:334-336](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java)**

- Trigger/problem: An interruption-ignoring worker finishes after destroy() and a subsequent start().
- Guard or missing check: Release the captured executor that acquired this exchange's permit, never this.exchangeExecutor.
- Consequence: The old worker increments the new executor's semaphore, exceeding its configured concurrency bound.

**E06 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:254-255](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java)**

- Trigger/problem: destroy() is called directly on a running server.
- Guard or missing check: Call stop() before destroying remaining resources so the running flag is cleared.
- Consequence: isRunning() remains true after closure; initialize() with autoStart refuses to restart.
- Audit observation: generation-probe.log: after direct destroy and autoStart initialize, running=true while actualPort=-1.

**E07 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:190-193](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java)**

- Trigger/problem: Worker or concurrency limits change after initialize() or between stop/start generations.
- Guard or missing check: Construct each generation's executor from that generation's validated options.
- Consequence: The active options report new limits while the reused executor continues enforcing old limits.
- Audit observation: generation-probe.log: active maximum was 1 while the executor still had 32 available permits.

**E08 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:330-332](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java)**

- Trigger/problem: An adapter returns an invalid status header or the outbound converter throws.
- Guard or missing check: Catch response-plan conversion exceptions and terminally complete with the controlled 500 response.
- Consequence: The worker exits without responding or closing the request, leaving the client waiting.
- Audit observation: See runtime observation A04.

**E09 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpMessageMapper.java:101-108](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpMessageMapper.java)**

- Trigger/problem: The request path contains percent escapes or comma-containing semicolon parameters.
- Guard or missing check: Apply UrlPathHelper-compatible semicolon removal, URI decoding, and path normalization before assigning path/uri.
- Consequence: Citrus receives different path headers than Jetty, breaking existing receive-path expectations.

**E10 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpMessageMapper.java:145-159](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpMessageMapper.java)**

- Trigger/problem: A reply carries Transfer-Encoding, including an echo of a chunked request.
- Guard or missing check: Filter transport-managed framing headers before setting the computed Content-Length.
- Consequence: HTTP/1.1 responses advertise both Transfer-Encoding and Content-Length, creating conflicting framing.
- Audit observation: probe.log: raw HTTP/1.1 response contained Transfer-Encoding: chunked and Content-Length: 4 together.

**E11 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpMessageMapper.java:283-292](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpMessageMapper.java)**

- Trigger/problem: A reply cookie specifies Secure, HttpOnly, Max-Age, or SameSite attributes.
- Guard or missing check: Serialize all supported Cookie attributes with a standards-compliant cookie encoder.
- Consequence: Cookie security and expiration attributes disappear, contradicting the reported cookie compatibility.
- Audit observation: See runtime observation A05.

**E12 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/FootprintProbe.java:128-134](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/FootprintProbe.java)**

- Trigger/problem: The default JDK client negotiates HTTP/2 with Vert.x but HTTP/1.1 with Jetty.
- Guard or missing check: Set HttpClient.Version.HTTP_1_1 for both campaigns and assert/report every response protocol.
- Consequence: Latency, thread, and memory comparisons conflate engine changes with different HTTP protocols.
- Audit observation: probe.log: the benchmark-style default JDK client negotiated HTTP_1_1 with Jetty and HTTP_2 with Vert.x.

### Verification gaps

**V01 — [docs/http-server-vertx-evaluation/sdd/04-implementation.md:16](sdd/04-implementation.md)**

- Trigger/problem: T11 is marked complete although no test stops the server with pending work or observes owned-thread termination.
- Guard or missing check: Hold a request or adapter pending, invoke stop, assert termination within the agreed budget and absence of owned worker/event-loop threads, then restart and verify old requests/replies cannot enter the new generation.
- Consequence: The evaluation reports lifecycle compatibility while leaking workers or allowing old-generation work to survive restart. Port and adapter-identity assertions cannot detect this.
- Gap shape: broken-verification-gap
- Consumer: Prototype shutdown/restart at endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:224.
- Evidence: Repository-wide symbol/import searches and every prototype test were inspected. LifecycleIT:111–153 checks adapter identity and three completed-request generations without pending work, elapsed shutdown assertions or thread inspection. ConcurrencyIT:74–80 completes both requests before teardown. ExchangeExecutorTest:105–110 waits for the task to finish before shutdown. The claimed 'pending-stop via gated adapter' test is absent.

**V02 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServerConcurrencyIT.java:58](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServerConcurrencyIT.java)**

- Trigger/problem: The reverse-order test replaces the default Citrus adapter with a custom echo adapter, bypassing Citrus reply queues and send/receive correlation.
- Guard or missing check: Run two concurrent requests through the default DirectEndpointAdapter; receive both with Citrus actions using separate contexts or explicit correlation; send the second response first and assert each client receives its own distinguishable response.
- Consequence: Default-adapter wiring or reply-correlation failures can pass the claimed concurrency proof because each gated worker directly returns its own reply.
- Gap shape: broken-verification-gap
- Consumer: Default adapter at VertxHttpServer.java:180; server actions in VertxHttpServerRoundtripIT.java:107.
- Evidence: ConcurrencyIT:58–80 installs VertxPrototypeAdapters.Gated via startWith:189. Gated:143 returns replies.apply(message), bypassing temporary reply queues. RoundtripIT:68–77 completes one exchange before beginning the next. Repository-wide searches found no concurrent Citrus-action prototype test. T9 explicitly requires preserving temporary reply queues and correlation.

**V03 — [endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java:460](../../endpoints/citrus-http/src/test/java/org/citrusframework/http/vertx/VertxHttpServer.java)**

- Trigger/problem: No wire test supplies query parameters or checks inbound protocol/context metadata, despite AC3 being reported as passed.
- Guard or missing check: Send encoded, duplicate and valueless query parameters over HTTP; assert them, protocol and root context through Citrus receive actions. Include the specified deliberate expectation mismatch.
- Consequence: Replacing request.query() with null would discard all incoming queries while the inspected tests still pass; direct mapper tests inject prepared snapshots.
- Gap shape: regression-gap
- Consumer: Citrus receive actions at endpoints/citrus-http/src/test/java/org/citrusframework/http/integration/VertxHttpServerRoundtripIT.java:104.
- Evidence: All prototype tests were read following repository-wide symbol/import searches. RoundtripIT:39 uses '/vertx-echo'; clientSend:81–101 adds no query; serverReceive:104–125 checks method/path/body/custom header without context/protocol/query. MapperTest:48–60 only injects 'a=1&b=2'. No encoded/duplicate/valueless wire queries or expected failing Citrus mismatch were found.

**V04 — [docs/http-server-vertx-evaluation/decision-report.md:15](decision-report.md)**

- Trigger/problem: AC4 is called passed using candidate-only payload tests although it requires identical fixtures against both engines.
- Guard or missing check: Run UTF-8, explicit non-UTF-8, empty, binary, status/null/timeout and HEAD fixtures against Jetty and Vert.x; record engine-specific outcomes and differences.
- Consequence: Candidate behavior can differ from Jetty while the report presents parity as established. The candidate's expectations currently serve as their own baseline.
- Gap shape: broken-verification-gap
- Consumer: Migration/parity conclusion at docs/http-server-vertx-evaluation/decision-report.md:80.
- Evidence: LimitsIT:55–188 runs payload/reply cases through startWith, which constructs VertxHttpServer:192. Jetty appears alongside the prototype only in CoexistenceIT and FootprintProbe; those check 200 or fixed pong. Additional saved regressions are HttpServerJavaIT and HttpServerStandaloneJavaIT, not shared fixtures. T8 requires both engines' contract outcomes.

**V05 — [docs/http-server-vertx-evaluation/decision-report.md:20](decision-report.md)**

- Trigger/problem: AC7/AC9 and T12 are marked passed/completed with two existing HTTP regressions and no accounting for required affected suites or convergence.
- Guard or missing check: Record passed, failed, skipped and unrun counts for HTTP, WebSocket, SOAP, OpenAPI and Cucumber selections; retain convergence/linkage results and label incomplete criteria accurately.
- Consequence: Maintainers can mistake limited evidence for affected-consumer and dependency compatibility. The 40-unit-test figure covers the new prototype tests, not existing HTTP regression coverage.
- Gap shape: other
- Consumer: decision-report.md:18 and sdd/04-implementation.md:17.
- Evidence: Saved Surefire XML has 40 cases, all options/mapper/executor tests; saved Failsafe XML has 24 cases in five prototype IT classes, all JDK 21.0.12.1. Two separate HTTP regression class reports exist; no WebSocket/SOAP Failsafe reports were found. The saved classpath mixes Netty 4.2.16.Final with 4.1.136.Final codec-http2/resolver-dns/codec-dns, with no convergence outcome. This does not demonstrate an actual linkage failure.

### Structure

The decision report exists to help Citrus maintainers decide whether a Vert.x migration merits further work. A conclusion-first decision memo is the appropriate structural model; this suggestion addresses placement only, leaving the content corrections to the other lenses.

| Pass | Original Text | Revised Text | Changes |
|---|---|---|---|
| structure | §Recommendation, 77 words, after all supporting analysis | MOVE immediately after the opening scope/runtime paragraph | Expose the decision before its evidence; remove 0 words. |

Exact word metrics: 1,131 total; introduction 38; acceptance verdicts 370; footprint 185; configuration 121; alternatives 135; #1807/SOAP notes 55; recommendation 77; reproduction 99. One recommendation; estimated reduction 0 words (0%); no length target; no comprehension trade-off from cuts.

## Evidence and reproduction

- [Canonical findings JSON](audit-findings.json)
- [Probe source](audit-evidence/AuditProbe.java)
- [Primary probe log](audit-evidence/probe.log)
- [Generation/configuration probe log](audit-evidence/generation-probe.log)
- [Successful Maven verification log](audit-evidence/maven-offline-verify.log)
- [Test counts and JVM identities](audit-evidence/verification-summary.json)
- [Audited source hashes](audit-evidence/source-manifest.json)
- [Original footprint samples](audit-evidence/original-footprint.jsonl), [environment](audit-evidence/original-environment.txt), [JAR totals](audit-evidence/original-jars.txt)

The exact captured test classpath is in `audit-evidence/test-classpath.txt`. It refers to this checkout and local Maven cache. Run the command below from the module directory after the focused Maven verification. Set `AUDIT_DIR` to the absolute audit-evidence directory and `JAVA_HOME` to the JDK 21 installation. The probe performs deliberate failure scenarios; its uncaught worker conversion exception is an observed defect, not a probe setup failure.

```bash
JAVA_HOME=/Users/asuruceanu/Library/Java/JavaVirtualMachines/jdk-21.0.12.1+1/Contents/Home
AUDIT_DIR=/Users/asuruceanu/development/projects/citrus-vertx-eval/docs/http-server-vertx-evaluation/audit-evidence
mkdir -p "$AUDIT_DIR/classes"
"$JAVA_HOME/bin/javac" -proc:none --release 17 \
  -cp "$(cat "$AUDIT_DIR/test-classpath.txt")" -d "$AUDIT_DIR/classes" "$AUDIT_DIR/AuditProbe.java"
"$JAVA_HOME/bin/java" -cp "$AUDIT_DIR/classes:$(cat "$AUDIT_DIR/test-classpath.txt")" \
  org.citrusframework.http.vertx.AuditProbe
"$JAVA_HOME/bin/java" -cp "$AUDIT_DIR/classes:$(cat "$AUDIT_DIR/test-classpath.txt")" \
  org.citrusframework.http.vertx.AuditProbe generations
```

Original pre-audit report snapshot and staged review diff: `/var/folders/h5/j6sjlnjn40d1vh48jv0pk5kw0000gn/T/citrus-vertx-audit-tdao95b4`. Diagnostic runs were bounded at 60 and 45 seconds. Existing source, specs and decision report were left intact for the author to revise.
