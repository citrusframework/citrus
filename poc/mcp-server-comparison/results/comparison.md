# Comparison: Quarkus vs Spring MCP server PoCs (T18)

Date: 2026-10-05. Citrus commit `3d109b4608c6354bc13ea27c861b3b974111b651` (`5.1.0-SNAPSHOT`).
Protocol revision: 2025-11-25. Transports: Streamable HTTP + real subprocess stdio
(legacy HTTP+SSE excluded). Budgets: 60s startup / 5s simulation / 10s stop-exit /
120s outer deadline, identical for both candidates.

## Verdict: neither yet — narrowed to whole-list hooks only (live HTTP: both PASS)

Update 2026-10-05 (HTTP increment): both candidates now also have green **live
Streamable HTTP** evidence with Citrus control (child round trips + mismatches +
same-JVM lifecycle mechanics where the platform allows). Transport selection is
explicit per launch on both (Quarkus flags; Spring `protocol=STREAMABLE` since the
framework default is legacy SSE). A/B: Spring proves one-JVM dual contexts with
equal IDs and reverse replies; Quarkus proves two-process isolation while one-JVM
dual hosts stay BLOCKED by construction (single augmented app per JVM).
The only remaining required-operation gap on BOTH sides is the supported
whole-`resources/list` extension (validator/customizer are confirmed boundary-only
by executable tests) — so per AC12 no candidate is recommended yet and no
production endpoint work starts.

Earlier 2026-10-05 (stdio increment, kept for history): pinned bootstrap/augmentation
routes executed, owned-context/hosting proven, tool + resource-read round trips over
stdio with deliberate-mismatch failure on both sides. The race is genuinely
comparable: identical scenario inventory, identical budgets, identical client.

Prior verdict text (superseded only for the stdio rows): contract/mechanics evidence
was green for both while live embedding was unresolved.

## Result matrix (contract layer: PASS; live stdio: PASS; live HTTP + whole-list hook: NOT RUN/BLOCKED)

| Candidate | Area | Transport | Scenario | Lane | JDK | Outcome | Evidence |
| --- | --- | --- | --- | --- | --- | --- | --- |
| quarkus 2.0.2 | bridge | — | typed tool + variable + mismatch | both | 17 | PASS | `CitrusPocEndpointTest` (5), 3x green |
| spring 2.0.1 | bridge | — | typed tool + variable + mismatch | both | 17 | PASS | `CitrusPocEndpointTest` (5), 3x green |
| both | exchange | — | 1:1 correlation, equal IDs, reverse, timeout, stop-gen | both | 17 | PASS | `ExchangeRegistryTest` (7), 3x green |
| both | client | — | descriptor, bounded child, 5-exchange round trip, concurrent IDs | both | 17 | PASS | `ClientDriverTest` (7), 3x green |
| quarkus 2.0.2 | bootstrap contract | same-JVM | construct/start/stop, same PID, no `@QuarkusTest` | both | 17 | PASS | `QuarkusBootstrapIT` (3), 3x green per lane |
| spring 2.0.1 | bootstrap contract | same-JVM | owned context, same PID, no `@SpringBootTest` | both | 17 | PASS | `SpringBootstrapIT` (3), 3x green per lane |
| quarkus 2.0.2 | controlled list semantics | — | empty/ordered/error/timeout/concurrent/reverse, no mutation | both | 17 | PASS | `QuarkusResourceListIT` (6) |
| spring 2.0.1 | controlled list semantics | — | empty/ordered/error/timeout/concurrent/reverse, no mutation | both | 17 | PASS | `SpringResourceListIT` (6) |
| quarkus 2.0.2 | static round trip semantics | — | discovery, typed args, read, tool-error, mismatch | both | 17 | PASS | `QuarkusHttpRoundTripIT` (4) |
| spring 2.0.1 | static round trip semantics | — | discovery, typed args, read, tool-error, mismatch | both | 17 | PASS | `SpringHttpRoundTripIT` (4) |
| quarkus 2.0.2 | stdio contract | stdio child | protocol-only stdout, bounded exit, 2 children, kill=failure | both | 17 | PASS | `QuarkusStdioIT` (3) |
| spring 2.0.1 | stdio contract | stdio child | protocol-only stdout, bounded exit, 2 children, kill=failure | both | 17 | PASS | `SpringStdioIT` (3) |
| quarkus 2.0.2 | lifecycle/isolation contract | same-JVM | 3 generations, A/B, equal IDs, stop-A-while-B-serves | both | 17 | PASS | `QuarkusLifecycleIT` (3) |
| spring 2.0.1 | lifecycle/isolation contract | same-JVM | 3 generations, A/B, equal IDs, stop-A-while-B-serves | both | 17 | PASS | `SpringLifecycleIT` (3) |
| quarkus 2.0.2 | outer bean | Spring bean | resolve-before-init, explicit start, destroy paths, unrelated survives | both | 17 | PASS | `QuarkusSpringBeanIT` (4) |
| spring 2.0.1 | outer bean | Spring bean | resolve-before-init, explicit start, destroy paths, unrelated survives | both | 17 | PASS | `SpringBeanIT` (4) |
| both | failure taxonomy | — | late-reply rejected, cancel, kinds, malformed/schema-invalid, stale-gen | both | 17 | PASS | `QuarkusFailureIT`/`SpringFailureIT` (5+5) |
| quarkus 2.0.2 | **live bootstrap** | same-JVM | augmented runner, `@QuarkusMain` ordinary-Java `Quarkus.run`, `asyncExit` stop, no `@QuarkusTest` | native (3.33.3.1) | 17 | LIVE-PASS | `QuarkusLiveStdioIT` (2), runner isolation asserted | `… -pl quarkus-probe -am verify -Dit.test='QuarkusLiveStdioIT' …` |
| spring 2.0.1 | **live bootstrap** | same-JVM | owned Boot context, shutdown-hook disabled, `McpSyncServer` bean proof, no `@SpringBootTest` | native (Boot 4.1.1 / Spring 7.0.9) | 17 | LIVE-PASS | `SpringLiveStdioIT` (2), server classpath asserted | `… -pl spring-probe -am verify -Dit.test='SpringLiveStdioIT' …` |
| quarkus 2.0.2 | **live stdio round trip** | stdio child | init 2025-11-25, echo + alpha read Citrus-controlled, mismatch fails both sides, protocol-only stdout | native | 17 | LIVE-PASS | client + `CITRUS_ASSERTIONS status=ok` / `fail-as-designed` | same as above |
| spring 2.0.1 | **live stdio round trip** | stdio child | init 2025-11-25, echo + alpha read Citrus-controlled, mismatch fails both sides, protocol-only stdout | native | 17 | LIVE-PASS | client + `CITRUS_ASSERTIONS status=ok` / `fail-as-designed` | same as above |
| quarkus 2.0.2 | live managed lane | stdio child | re-augmented MCP 2.0.2 on Quarkus 3.38.0 | citrus-managed | 17 | LIVE-PASS | tolerates newer platform; JUnit pin mediation recorded | `… -Pcitrus-managed …` |
| spring 2.0.1 | live managed lane | stdio child | Spring 7.0.8 mediated over Boot 4.1.1, SDK 2.0.0 kept | citrus-managed | 17 | LIVE-PASS | tree-verified 7.0.8/2.0.1/2.0.0 | `… -Pcitrus-managed …` |
| quarkus 2.0.2 | **live HTTP** | http child | STREAMABLE `/mcp`, ephemeral port, init/list/call/read + mismatch, protocol-only framing, `http.enabled`/`stdio.enabled` flags | native | 17 | LIVE-PASS | `QuarkusLiveHttpIT` (2) + stdio `Listening`-absence proof | `… -Dit.test='QuarkusLiveHttpIT' …` |
| spring 2.0.1 | **live HTTP** | http child + in-probe | `protocol=STREAMABLE` (default is legacy SSE), Tomcat loopback ephemeral, round trip + mismatch + same-JVM lifecycle | native | 17 | LIVE-PASS | `SpringLiveHttpIT` (3) | `… -Dit.test='SpringLiveHttpIT' …` |
| quarkus 2.0.2 | **live whole-list hook** | http+stdio | validator deny-only executed (denial error, other methods unaffected); 74-method sweep pins no interception | both | 17 | GAP (executed) | `QuarkusValidatorDiagnosticIT` (2) + `HookSweepTest` (1) | `verify` / unit `test` |
| spring 2.0.1 | **live whole-list hook** | http+stdio | customizer additive-only; 85-method sweep over SDK server + AI surface pins no interception | both | 17 | GAP (executed) | `HookSweepTest` (1) + `ListHookBoundaryTest` (2) | unit `test` |
| spring 2.0.1 | **live A/B** | one JVM | dual owned contexts, distinct ports, equal JSON-RPC IDs, reverse replies, stop-A-while-B-serves, A refused | native | 17 | LIVE-PASS | `SpringLiveABIT` (1, raw client) | `… -Dit.test='SpringLiveABIT' …` |
| quarkus 2.0.2 | **live A/B** | one JVM | impossible three ways: construction, qualifier-without-declaration (boot FAIL), `servers.*` map unbindable; IGNORE silently drops | both | 17 | BLOCKED (executed) | `QuarkusNamedServerIT` (1, pins the drop) | `verify` |
| quarkus 2.0.2 | two-process variant | 2 children | distinct sessions/ports, concurrent serve, no cross-talk | native | 17 | PASS (labeled variant) | `QuarkusLiveABIT` (1) | `… -Dit.test='QuarkusLiveABIT' …` |
| both | JDK 21 lane | — | full `verify` both lanes, incl. live HTTP/A/B + re-augmentation | both | 21 | PASS | 101 tests green per lane; Temurin 21.0.12.1 user-local install |

Test counts (final full `verify`, default lane): common 17 unit, client-driver 7,
quarkus-probe 37 IT (28 contract + 9 live) + 3 unit, spring-probe 34 IT (28 contract
+ 6 live) + 3 unit — 101 total, 0 failures, 0 unexplained skips. Deliberate feasibility failures retain their reports; no expectation was
changed to turn the build green. No `BLOCKED`/`FAIL` cell is described as support.

## Conclusion

- **No candidate is recommended (AC12).** Quarkus 2.0.2 and Spring AI 2.0.1 / SDK 2.0.0
  are tied on all evidence gathered so far; nothing separates them yet.
- **What both proved:** the Citrus-side design (bridge, exchange registry, client driver)
  works; each framework can be started and stopped from ordinary Java without
  `@QuarkusTest`/`@SpringBootTest`; live stdio AND Streamable HTTP round trips with
  Citrus-controlled replies pass; both survive the `citrus-managed` version lane and
  JDK 17 + 21; Spring proves one-JVM A/B isolation, Quarkus two-process isolation.
- **What blocks a decision:** whole-`resources/list` interception only — no supported
  public hook in either pinned release (explicit GAPs, executable boundary tests).
- **Next steps, in order:** (~~run live HTTP for both — done~~) send the two upstream
  questions on the list hook (needs authorization). If a hook exists, grade it by API
  class; if neither has one, the maintainers must decide on a scope concession (see
  above) or a custom layer.
- **Not started:** production `endpoints/citrus-mcp` work waits on this decision.

## Reproduction

```bash
./mvnw -f poc/mcp-server-comparison/pom.xml validate -Dgib.disable=true
./mvnw -f poc/mcp-server-comparison/pom.xml verify -Dgib.disable=true
# Focused (upstream -am modules participate silently):
./mvnw -f poc/mcp-server-comparison/pom.xml -pl quarkus-probe -am verify -Dgib.disable=true -Dit.test='QuarkusBootstrapIT,QuarkusLifecycleIT' -Dfailsafe.failIfNoSpecifiedTests=false -Dsurefire.failIfNoSpecifiedTests=false
# Lanes (JDK 17 and JDK 21, both green):
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
# or: export JAVA_HOME=~/Library/Java/JavaVirtualMachines/jdk-21.0.12.1+1/Contents/Home
./mvnw -f poc/mcp-server-comparison/pom.xml -pl quarkus-probe -am -Pcandidate-native verify -Dgib.disable=true
./mvnw -f poc/mcp-server-comparison/pom.xml -pl quarkus-probe -am -Pcitrus-managed verify -Dgib.disable=true
# Lane introspection:
./mvnw -f poc/mcp-server-comparison/pom.xml -pl quarkus-probe help:effective-pom -Dgib.disable=true -Pcandidate-native
./mvnw -f poc/mcp-server-comparison/pom.xml -pl quarkus-probe -am dependency:tree -Dgib.disable=true -Pcandidate-native
```

Profile precedence verified: `candidate-native` resolves Quarkus-active 3.33.3.1 /
Spring-active 7.0.9; `citrus-managed` resolves 3.38.0 / 7.0.8. Common dependency
graph is framework-free (Jackson 2.22.2 + JUnit 6.1.2 + AssertJ + SLF4J); no SDK
client classes on any host classpath. Cold augmentation/build time was not
measured (no live augmented build ran); runtime start/stop timings are
sub-second for the contract adapters and recorded per-report, not as production
promises.

## Maintenance inventory (source-pinned, per AC11)

| Responsibility | Quarkus 2.0.2 lead | Spring AI 2.0.1 / SDK 2.0.0 lead | Owner today |
| --- | --- | --- | --- |
| Init/capabilities/dispatch/error serialization | MCP extensions + `McpMessageHandler` dispatch | `McpServerAutoConfiguration` + high-level server | Framework (proven live: stdio + HTTP round trips) |
| Whole-`resources/list` interception | `ResourceMessageHandler` assembles internally; `McpRequestValidator` deny-only, no reply channel | `McpAsyncServer` list handler uses server catalog; `SyncSpecification` additive-only | Explicit GAP both (boundary tests) |
| Sessions/transport lifecycle | HTTP/stdio extensions + flags; `Quarkus.run`/`asyncExit`, CDI barrier on bridge handoff | MVC `WebMvcStreamableServerTransportProvider` (`protocol=STREAMABLE`); owned context, shutdown-hook disabled | Framework (proven live) |
| Cancellation/disconnect → Citrus futures | No hook exercised; stop-generation cleanup proven at bridge | Same | Gap (unobserved live) |
| Citrus expectations/replies/deadlines/correlation | `CitrusPocEndpoint` + `ExchangeRegistry` (this PoC) | Same | Citrus PoC |

Classification rule applied: an executable interception success would still be
graded by API class (documented public extension vs undocumented vs
runtime/internal vs custom protocol policy). Direct SDK session-handler
replacement, a fork, or custom wire work under a Spring/Quarkus label is
explicitly out; the session-factory swap is recorded as a boundary-changing
alternative, not the Spring PoC.

## Endpoint DSL feasibility (production `endpoints/citrus-mcp` input)

Assessed 2026-10-05 against the live evidence. The PoC builds no DSL; this section
records what the proposed builder/receive-send DSL may assume per candidate.

| DSL element | Status | Evidence / condition |
| --- | --- | --- |
| Server builder + lifecycle (Spring shape: port/path/`@Bean`/start/stop) | Supportable | Loopback bind, `/mcp`, ephemeral ports, idempotent start/stop, `autoStart=false` proven live; still needs the `AbstractServer` adapter (`startup`/`shutdown`/`destroy` mapping) and a live `@Bean`-in-outer-context proof (T14-live). XML parsers stay out of scope — no conflict. |
| Uniform programmatic builder incl. Quarkus (`McpServer.builder()…`) | Reshape | Spring servers are constructed; Quarkus servers are compiled. The builder pattern fits the former and is fiction for the latter: a Quarkus MCP server is a build artifact (CDI, augmentation, config-driven), not an object, so it cannot be `.build()`-constructed — declare Quarkus endpoints in config and launch the augmented runner. |
| Static tool declaration (name/description/input-schema) | Supportable both | Spring `ToolCallback` bean takes exactly this shape; Quarkus `@Tool` + `@ToolArg` generates typed schemas live. Explicit schema-*string* form verified on Spring; assumed (annotation element exists) but unexecuted on Quarkus. |
| `receive().invokeTool().argument()` + `send().toolResponse().content()` | Supportable both | Exactly `CitrusPocEndpoint.receive`/`reply` as executed live both, including `${}` resolution and mismatch-fails-scenario. |
| Resource read receive/send | Supportable both | Proven live both (`alpha-content` pattern). |
| Resource **list** receive/send | **Blocked both** | Frameworks serve listings from their own catalog; neither pinned release exposes a supported interception hook (boundary tests). The DSL may offer the syntax only with an explicit pending-upstream limitation — never silently static. |

No production DSL work starts from this PoC (verdict above); when the endpoint SDD
begins, the list row is its critical dependency.

## Scope concessions needing maintainer agreement (none granted)

1. Framework-hosted test only — i.e. accepting `@QuarkusTest`/`@SpringBootTest`
   ownership instead of ordinary-Java same-JVM start/stop. A Citrus test must survive
   on `@CitrusSupport` + plain Java alone: the server is a test-owned field, built in
   `@BeforeEach` and stopped in `@AfterEach`, with `receive()`/`send()` asserting
   through the Citrus runner. A framework harness boots the server *around* the test
   (shared cached contexts, auto-start, framework-owned shutdown), which hides
   lifecycle bugs and fights Citrus context management. Rule of thumb: if removing
   the framework annotation breaks the setup, the test was proving the harness, not
   the endpoint. (Quarkus setup stays config-declared per the DSL table above; only
   the harness rule is common.)
2. Shared host with named paths instead of independent A/B hosts.
3. Subprocess-HTTP-child instead of same-JVM embedding.
4. Upstream extension request before endpoint design changes.

## Upstream questions (status 2026-10-05)

Answered by the live increment (no upstream contact needed):

- ~~Quarkus: supported ordinary-Java bootstrap for an augmented app?~~ Yes:
  `@QuarkusMain` + `Quarkus.run(App)` in the runner jar, `asyncExit` stop, no test
  harness. Two independently owned instances / classloader isolation still untested.
- ~~Spring: owned-context embedding (shutdown hooks, transport cleanup, coexistence
  with Spring 7.0.8)?~~ Yes: `SpringApplicationBuilder` + `register-shutdown-hook=false`,
  close-owned-only, green on both 7.0.9 and mediated 7.0.8.

Still open (drafted, not sent):

- Quarkus: is there a supported whole-`resources/list` operation extension in
  2.0.2 retaining normal init/session/policy (validator is diagnostic-only)?
- Spring: is there a public high-level `resources/list` customizer in AI 2.0.1 /
  SDK 2.0.0 preserving high-level ownership (session-factory swap is boundary change)?

## What this does not do

No production `endpoints/citrus-mcp`, builder/DSL, XML/YAML, user guide, security
hardening, benchmarking, or conformance certification. No root reactor/BOM,
core lifecycle, `tools/mcp-server`, or existing-test changes (verified via git
status). No third direct-SDK server. Sending the upstream questions or opening
endpoint work requires separate authorization.
