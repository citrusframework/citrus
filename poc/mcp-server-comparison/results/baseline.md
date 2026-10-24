# Baseline: MCP server comparison PoC (T1)

Date: 2026-10-05. Citrus commit: `3d109b4608c6354bc13ea27c861b3b974111b651`
(version `5.1.0-SNAPSHOT`). JDK lanes: 17 (baseline) and 21 (comparison).
Protocol revision under test: 2025-11-25.

## Candidate pins (fixed; re-pin only via explicit recorded variant)

| Candidate | Baseline | Native dependency train | Transitive SDK |
| --- | --- | --- | --- |
| Quarkus MCP Server | MCP **2.0.2** (`4b6f3bcd83cd1c288d40c4cb7b30345bb4fd4897`) | Quarkus **3.33.3.1** | (framework-managed) |
| Spring AI MCP | AI **2.0.1** (`c9107fdac0a6c88489c08bb88abcee74c146981d`) | Boot **4.1.1**, Spring **7.0.9** | SDK **2.0.0** |

Client driver SDK: **2.0.1** on its own process classpath only.

## Citrus-managed lane (from Citrus root at inspected commit)

Spring **7.0.8**; Quarkus platform/runtime/deployment **3.38.0**;
Jackson 3 (`tools.jackson`) **3.2.2** and Jackson 2 **2.22.2**
(annotations 2.22); Jetty **12.1.11**; Servlet **6.1.0** where present.
Note: the live root `pom.xml` at implementation time declares Jackson 3.2.3 /
Jackson 2.22.3; the PoC keeps the Stage-2 pins for comparability and records
this drift here. Framework-only versions preserved otherwise. BOM precedence
recorded per lane in T16/T17.

Citrus artifacts come from the recorded checkout (local `install` of
`core/citrus-spring`, `runtime/citrus-junit5`, etc. if provenance requires it);
never an unidentified remote `5.1.0-SNAPSHOT`. No root POM changes.

## Scenario inventory (all NOT RUN at T1)

| ID | Description | AC |
| --- | --- | --- |
| `bootstrap` | Ordinary-Java same-PID start/stop, no listener on construct | AC5 |
| `http-roundtrip` | Init, list tools/resources, invoke `echo`, read resource, tool-error, mismatch fails | AC2, AC3 |
| `resource-list-empty` | Controlled `resources/list`: empty catalog advertised, pending until reply, empty reply | AC4 |
| `resource-list-ordered` | Ordered subset of two declared resources, exact correlation | AC4 |
| `resource-list-delayed` | Delayed reply within 5s budget; barrier proves pending | AC4 |
| `resource-list-error` | Explicit JSON-RPC error reply | AC4 |
| `resource-list-timeout` | Simulation timeout expiry, terminal cleanup | AC4, AC9 |
| `resource-list-concurrent` | Two simultaneous requests, different replies, reverse-order, no cross-talk | AC4, AC6 |
| `lifecycle` | start/start/stop/stop, 3 generations, failed bind cleanup, stop+pending, port reuse | AC5 |
| `isolation-ab` | Endpoints A/B distinct catalogs/ports, equal request IDs, reverse replies, stop A while B serves | AC6 |
| `spring-bean` | Outer `@Bean`, autoStart=false, destroy while running / after stop, unrelated context survives | AC7 |
| `stdio` | Child hosts scenario+framework, protocol-only stdout, stderr diagnostics, EOF, bounded exit, 2 children | AC8 |
| `failure-matrix` | Late reply, disconnect/cancel, malformed params, schema-invalid args, cursor, ID collision | AC9 |

## Profiles

- `candidate-native` (default): framework release's coherent train + Citrus artifacts.
- `citrus-managed`: same MCP versions with Citrus-managed versions applied.
- Resolve per lane with `help:effective-pom` + `dependency:tree -Dverbose`.
