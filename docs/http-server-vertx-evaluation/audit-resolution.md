# Follow-up to the HTTP prototype audit

This record maps the historical findings in [audit-report.md](audit-report.md) and
[audit-findings.json](audit-findings.json) to the corrective work. The original audit remains
unchanged. Current test totals, reactor results and measurements are in
[decision-report.md](decision-report.md).

| Findings | Resolution and evidence |
|---|---|
| A01, E01 | Admission is acquired before a body handler is installed. Chunks are checked before being appended. `VertxHttpServerAuditIT.oversizedChunkIsRejectedBeforeUploadFinishes` and `incompleteUploadConsumesAdmissionBeforeBuffering` exercise both limits. |
| E02 | A body timeout sends 408, removes the body handler and closes the connection. `bodyDeadlineClosesIncompleteUpload` verifies EOF without completing the upload. |
| A02, V01 | Stop now cancels and terminates the owned executor, listener and Vert.x runtime within one budget. Tests stop with a pending default reply and a queued request, inspect worker termination, and repeat lifecycle operations. |
| A03, E04 | Each generation captures its adapter, executor, configuration and runtime. Shutdown cancels queued exchanges and wakes default reply queues. `oldQueuedRequestCannotReachRestartedDefaultAdapter` verifies that old work and a late reply cannot enter the new queue. |
| E05 | Permits are released only to the exchange's captured executor. An uncooperative old worker prevents a new generation from starting. `uncooperativeAdapterMakesShutdownFailureVisibleAndPreventsRestart` verifies a bounded reported failure, refusal to restart, and successful cleanup after the worker exits. Its first run exposed a second cleanup defect; close futures are now retained so cleanup retries do not use a terminated event loop. |
| E03 | Start returns immediately when already running. `repeatedStartKeepsTheSameListener` uses the active port to detect an attempted second bind. |
| E06 | Destroy calls the stop path and clears running state. `destroyClearsRunningStateAndAllowsAutoStart` verifies reinitialization. |
| E07 | A new executor is built from the options captured at each start. `changedAdmissionLimitAppliesAtStart` initializes first, changes the limit to one, then verifies a second request gets 503. |
| A04, E08 | Adapter and outbound-conversion failures terminate with an empty 500 and are retained in `getFailures()`. `invalidOutboundStatusGetsTerminal500` verifies the wire response and failure record; the existing adapter-exception test checks that private exception text is not returned. |
| A05, E11 | Response cookies retain Path, Domain, Max-Age, Secure, HttpOnly and SameSite. Mapper tests check the attributes; the shared wire fixture checks immediate deletion and security attributes against both engines. Jetty's Expires-only deletion form is semantically equivalent to the prototype's Max-Age=0 form. This does not claim support for arbitrary extension attributes. |
| A06 | Startup captures the exposed HTTP configuration instead of resetting it. A wire test verifies ISO-8859-1 after startup. Content type, binary media types, status, cookie and path settings use the same snapshot. Custom converter objects remain caller-owned references and must not be mutated concurrently. |
| E09 | Paths use the existing controller's semicolon-removal utility, request charset decoding and slash normalization. Mapper tests compare with `UrlPathHelper`; shared wire tests cover an escaped path and a comma-containing semicolon parameter. |
| E10 | The mapper removes user-supplied Transfer-Encoding and Content-Length, then computes the response length. The framing regression asserts no conflicting transfer header. |
| E12 | Both the probe client and prototype explicitly use HTTP/1.1; every measured response verifies the negotiated version. The old mixed-protocol results are withdrawn from the comparison. |
| A08, V03, V04 | `VertxHttpContractIT` runs common fixtures against both engines, covering payloads, explicit/default status, null replies, HEAD, cookies and request metadata. Query fixtures include escaped, duplicate and valueless parameters. `incorrectCitrusExpectationFails` asserts the real Citrus validation failure. The expanded method fixture also records that Jetty handles TRACE in its Servlet layer, whereas the prototype invokes Citrus. This establishes the tested subset, not TRACE parity or every Servlet conversion behavior. |
| A09, V02 | `defaultAdapterCorrelatesReverseRepliesAndIsolatesServers` uses real Citrus receive/send actions, separate test contexts and the default adapter to answer the second request before the first while another server has a pending request. Old queued work and late replies are covered separately by the restart regression. |
| A07, V05 | The required reactor was attempted, the duplicate Spring context was fixed, and the reactor was rerun/resumed without skipping tests. Exact module outcomes are retained with the final report. A verbose dependency tree records mediation and mixed Netty versions; passing HTTP/1.1 tests is not presented as a clean convergence result. |
| A10 | The harness now records five fresh processes per engine per text/binary fixture, eight clients, 100 warmups, 1,000 raw samples per process, protocol checks, hardware/JVM provenance, artifact sizes and median/range summaries. Statistics and real child timeout/failure handling have tests. The classpath remains additive; the unsupported future production JAR-size estimate is withdrawn. |
| A11 | The report identifies #1807 as WebSocket modernization and explains its HTTP inheritance dependency. SOAP's independent Jetty bootstrap is discussed separately. |
| A12 | Stage 05 now documents the actual prototype, verification commands, limitations, an ordinary HTTP experiment and retained-Jetty TLS/Servlet examples. Stage 04 records actual results and remaining evaluation requirements. |
| S01 | The decision report begins with the recommendation and its practical implications. |

## Test-first evidence

Before the implementation changes, `audit-red.log` recorded three failing mapper tests and nine
failing wire/lifecycle tests. `-Dmaven.test.failure.ignore=true` was used only in that red run to
allow the integration tests to execute after the unit failures. Its Maven success banner does
not represent passing tests. All subsequent green and regression runs use normal failure handling.

The initial full-suite failure on port 11080 came from the prototype action test bootstrapping a
second Spring context. It now extends `TestNGCitrusSpringSupport`, like the module's existing
integration tests. No production listener port or dependency version was changed to make it pass.
The earlier blocker and intermediate test failures remain in `audit-followup-evidence/`.

This resolution record closes reproduced implementation defects and corrects unsupported report
claims. It does not declare every original SDD task complete. A future public API migration,
complete configuration equivalence, dependency alignment and a deployable Jetty-free package
remain separate work; the decision report states the remaining evaluation gaps explicitly.
