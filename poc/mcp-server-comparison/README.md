# MCP Server Comparison PoC

Isolated feasibility investigation for
[Citrus issue #1334](https://github.com/citrusframework/citrus/issues/1334).
Full plan: `docs/specs/mcp-server-poc/sdd/01-specification.md`,
`02-architecture.md`, `03-tasks.md`.

## Boundaries (do not violate)

- Standalone aggregator. **Not** part of the Citrus reactor, BOM, or release.
  Never add it to the root `pom.xml` modules, never `deploy` it
  (`maven.deploy.skip=true`).
- No changes to `core/*`, `runtime/*`, `endpoints/*`, `tools/mcp-server`,
  root dependency management, or existing tests to make a candidate pass.
- No direct-SDK third server implementation; no production `endpoints/citrus-mcp`.
- Test client SDK (`2.0.1`) lives only in `client-driver`'s launch classpath,
  never on a host runtime classpath.
- Local loopback HTTP, ephemeral ports, no external credentials.

## Layout

| Directory | Responsibility |
| --- | --- |
| `common/` | Protocol-neutral config, fixtures, bridge, exchange registry, evidence model |
| `client-driver/` | Test-only keyless MCP client on its own JVM/classpath |
| `quarkus-host/` | Quarkus MCP extension host adapter |
| `spring-host/` | Spring AI MCP host adapter |
| `quarkus-probe/` | Ordinary-Java JUnit/Citrus probes for Quarkus (one candidate per probe JVM) |
| `spring-probe/` | Ordinary-Java JUnit/Citrus probes for Spring |
| `results/` | Curated comparison and reproduction guide (logs stay under `target/`) |

Each launch selects HTTP **or** stdio explicitly; installing both starters must
never silently open a second transport.

## Build

```bash
# Validate the isolated build (no production POM changes expected in git diff)
./mvnw -f poc/mcp-server-comparison/pom.xml validate
./mvnw -f poc/mcp-server-comparison/pom.xml help:effective-pom

# Unit tests for the shared contract
./mvnw -f poc/mcp-server-comparison/pom.xml -pl common -am test

# Client driver
./mvnw -f poc/mcp-server-comparison/pom.xml -pl client-driver -am verify

# Candidate probes (examples; require T4/T5 prerequisites)
./mvnw -f poc/mcp-server-comparison/pom.xml -pl quarkus-probe -am verify -Dit.test=QuarkusBootstrapIT -Dfailsafe.failIfNoSpecifiedTests=false
./mvnw -f poc/mcp-server-comparison/pom.xml -pl spring-probe -am verify -Dit.test=SpringBootstrapIT -Dfailsafe.failIfNoSpecifiedTests=false

# Dependency lanes (per JDK 17, then 21)
./mvnw -f poc/mcp-server-comparison/pom.xml -pl quarkus-probe -am -Pcandidate-native verify
./mvnw -f poc/mcp-server-comparison/pom.xml -pl quarkus-probe -am -Pcitrus-managed verify
```

A green unit build is **not** evidence of a live transport round trip. Every
executable scenario starts as `NOT RUN`; only named `*IT` reports with
nonzero executed tests and visible skips count. `failsafe.failIfNoSpecifiedTests=false`
only lets upstream `-am` modules participate — the target module must still
report the named class.

## Shared fixtures

Tool `echo` (JSON object input schema, nested objects/arrays/numbers/booleans,
explicit null, absent optional) and text resources `poc://alpha`, `poc://beta`
(see `common/src/main/resources/scenarios.json`). Budgets: 60s startup, 5s
simulation reply, 10s stop/child exit, 120s outer deadline.

## Results

- `results/baseline.md` — pinned versions, scenario inventory (all NOT RUN initially)
- `results/quarkus.md`, `results/spring.md` — per-candidate evidence
- `results/comparison.md` — final matrix and decision (T18)
