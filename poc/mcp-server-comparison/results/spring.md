# Spring evidence (live log)

Candidate: Spring AI MCP **2.0.1** (`c9107fdac0a6c88489c08bb88abcee74c146981d`), native Boot **4.1.1** / Spring **7.0.9**, transitive SDK **2.0.0**.
Protocol revision: 2025-11-25. Hosting under test: same-JVM ordinary-Java probe.

## Bootstrap contract (T5, adapter mechanics — PASS)

- `SpringBootstrapIT` (ordinary Java, no `@SpringBootTest`): construction opens no
  listener and no owned context; `start` creates exactly one owned context on the
  same PID with a single selected transport; repeated start is a no-op; repeated
  stop is safe; restart uses a fresh builder/context; failed bind closes the owned
  context with cause preserved; shutdown-hook ownership stays disabled so an outer
  context is never closed.
- Command: `./mvnw -f poc/mcp-server-comparison/pom.xml -pl spring-probe -am verify -Dit.test=SpringBootstrapIT -Dfailsafe.failIfNoSpecifiedTests=false -Dgib.disable=true`

## Live framework embedding (T5, framework context — NOT RUN)

- Pending: Spring AI-managed high-level server in the owned Boot context (MVC
  Streamable HTTP starter or stdio, `McpPocConfiguration` with `autoStart=false`);
  owned-context shutdown semantics; transport cleanup and coexistence with Citrus
  Spring 7.0.8. Source leads: `McpServerAutoConfiguration`, `WebMvcStreamableServerTransportProvider`
  (Stage 2 refs `SAUTO`/`SMVC`).

## Whole-list and transport (T7/T9/T11/T13 — NOT RUN)

- Pending: public high-level list customizer search in pinned Spring AI / SDK 2.0.0
  (`McpAsyncServer` resource-list handler uses the server catalog). Static
  registration alone is insufficient for AC4. A low-level session-factory
  replacement is a boundary-changing alternative for review, not the Spring PoC.

## Runtime matrix (T17 — contract lanes PASS, live lanes NOT RUN/BLOCKED)

- JDK 17, `candidate-native` (Spring-active 7.0.9): `SpringBootstrapIT,
  SpringLifecycleIT, SpringResourceListIT, SpringHttpRoundTripIT` (16 IT) —
  3x green. Full probe: 28 IT green. Client SDK 2.0.1 confirmed absent from
  host classpaths (driver-only).
- JDK 17, `citrus-managed` (Spring-active 7.0.8): same 16 IT — 3x green.
- JDK 21 (Temurin 21.0.12.1, `~/Library/Java/JavaVirtualMachines/jdk-21.0.12.1+1`): PASS — full `verify` green on both lanes (84 tests), incl. live stdio suites.
- Live lanes (owned-context high-level server, list customizer, wire round
  trips): NOT RUN; blockers as in `comparison.md`. Root dependency management
  untouched.

## Live framework embedding (T5-live/T11-live — PASS, stdio)

Executed 2026-10-05 on JDK 17 (default JVM 25 also used for manual probes; ITs run on the build JVM):

- **Owned Boot context**: `SpringLiveHost` builds `SpringApplicationBuilder(LiveServerConfig)` with `spring.main.register-shutdown-hook=false`, closes only the owned context. Same-PID ownership asserted in the child (`host.ownerPid() == ProcessHandle.current().pid()`).
- **Protocol owner**: `McpSyncServer` bean (`io.modelcontextprotocol.server.McpSyncServer`, SDK 2.0.0) created by `McpServerAutoConfiguration`; child logs `owner=io.modelcontextprotocol.server.McpSyncServer version=2.0.0`.
- **Public extension inventory**: `ToolCallback` bean (echo) via `ToolCallbackConverterAutoConfiguration`; static text resources via `SyncResourceSpecification` beans. No session-factory replacement, no fork, no custom wire code. Wire IDs stay framework-managed; bridge correlation uses endpoint+generation+session+nonce (documented limitation).
- **Round trip** (`SpringLiveStdioIT`, SDK 2.0.1 client parent / SDK 2.0.0 server child, classpath isolation asserted pre-launch): initialize 2025-11-25, tools/list exposes echo with description+schema, resources/list advertises alpha+beta, tools/call returns Citrus-controlled `{"echoed": "12345"}` after bridge receive+reply, resources/read returns `alpha-content`, deliberate mismatch fails the tool call AND the Citrus scenario (`CITRUS_ASSERTIONS status=fail-as-designed`, child exit 1). Bounded natural exit; protocol-only stdout (logback-test routes all logging to stderr).
- **Lanes**: candidate-native (Spring 7.0.9) and citrus-managed (spring-core/beans/context mediated to 7.0.8 over Boot 4.1.1) — live round trip green on both, on JDK 17 and JDK 21.
- **Findings**: LIVE-NULL-1 — the SDK stack elides explicit-null tool arguments on the wire (`"maybeNull":null` sent never reaches the server callback); live expectations use the delivered form. Streamable HTTP (Tomcat 11 not cached) and whole-list customizer remain NOT RUN.
- Commands: `./mvnw -f poc/mcp-server-comparison/pom.xml -pl spring-probe -am verify -Dgib.disable=true -Dit.test='SpringLiveStdioIT' -Dfailsafe.failIfNoSpecifiedTests=false -Dsurefire.failIfNoSpecifiedTests=false` (+ `-Pcitrus-managed`).

## Live Streamable HTTP (T9-live — PASS) + live A/B isolation (T13-live — PASS)

Executed 2026-10-05 on the build JVM:

- **Transport selection**: `spring.ai.mcp.server.protocol=STREAMABLE` is REQUIRED — the framework default is legacy SSE (verified via `--debug` conditions report: `StreamableEnabledCondition` needs `protocol=STREAMABLE`). Stdio launches set `web-application-type=none` and assert no container; HTTP launches assert exactly one container via `local.server.port` (Boot 4 removed `WebServerApplicationContext`; port comes from `ServerPortInfoApplicationContextInitializer`, registered explicitly).
- **Round trip** (`SpringLiveHttpIT`, 3 tests): version-pure Tomcat child (ephemeral loopback port, `HTTP_READY` marker) + in-probe same-JVM start/stop (lifecycle only — probe JVM resolves SDK 2.0.1, recorded variant). Init/list/call/read + mismatch, all Citrus-controlled; bounded exits (0 / 1) with `CITRUS_ASSERTIONS` on stderr.
- **A/B** (`SpringLiveABIT`): two owned Boot contexts in ONE probe JVM, distinct ephemeral ports; raw JSON-RPC IDs force EQUAL request IDs across sessions; reverse-order completion with no cross-talk (`servedBy` markers); stop A while B serves (B answers, A refuses new sessions).
- **Mediation findings (LIVE-JACKSON-1)**: the server-side jackson3 provider needs networknt 3.0.0; Boot 4.1.1's tools.jackson 3.1.5 works, the SDK-pom's 3.0.3 does NOT (NoSuchMethodError — tried and reverted). Probe JVMs pin networknt 3.0.0 once (aggregator) because the client's jackson2 provider would otherwise shadow 2.0.4 by nearest-wins.
