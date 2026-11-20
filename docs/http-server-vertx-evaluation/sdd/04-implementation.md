# Implementation and audit corrections: Vert.x HTTP evaluation

Updated 14 October 2026. The user authorized resolving audit defects and updating the maintainer
report to reflect the code. The reproduced implementation defects have been corrected in the
test-only prototype. Production code, dependency versions and the Java 17 compiler target remain
unchanged. Tests and valid measurement processes run under Temurin JDK 21.0.12.1.

## Test-first corrections

Before implementation edits, the audit regression selection reproduced **12 failures**: three
mapper tests and nine integration tests. `audit-followup-evidence/audit-red.log` preserves the
red run. Its failure-ignore flag allowed both test phases to execute; it was not used in any
green or regression run.

The fixes reserve admission before streaming the body, stop incomplete uploads at their size or
time limit, capture resources per generation, terminate owned workers on stop, cancel obsolete
queued requests, preserve supplied configuration, make start idempotent, and clear running state
on destroy. Conversion failures now return an empty 500 and remain visible to the harness.
Cookie attributes, path normalization and response framing have regression coverage.

Additional tests cover a slow upload occupying the last admission slot, a custom adapter that
ignores interruption, real Citrus default-adapter correlation, another server's pending request,
intentional Citrus validation failure, and shared Jetty/Vert.x wire fixtures. The uncooperative
adapter test exposed a retry-cleanup defect; retaining close futures fixed it. The original
client-disconnect test now waits for adapter entry before closing the socket, so it tests the
claimed race deterministically.

The shared method fixture documents a compatibility exception: Jetty's Servlet layer answers
TRACE without invoking Citrus, whereas the prototype routes TRACE through its adapter. This is
an observed difference, not a claim of TRACE parity. Cookie deletion formatting also differs,
while the tested expiration and security semantics agree.

The detailed mapping from every audit ID to its resolution is in [audit-resolution.md](../audit-resolution.md).

## The earlier regression blocker

The first full regression attempt failed on port 11080 because the new prototype action test
created a second Spring application context. It now extends `TestNGCitrusSpringSupport`, matching
the existing module integration tests. No listener port was changed. The complete HTTP suite then
passed. The selected reactor was rerun and resumed from HTTP after correcting a new shared fixture.
The preceding 24 reactor entries had passed; all 11 resumed entries passed, including the five
required target modules. Intermediate logs remain available alongside the final evidence.

Final required-module results: HTTP **281 unit / 79 integration**; WebSocket **28 / 2**;
SOAP **206 / 35**; OpenAPI **351 / 60**; HTTP Cucumber **23 passing scenarios, 259 passing steps**.
There were no failures, errors or skips in those executed target suites. Cucumber's later
zero-test provider pass overwrote its XML, so its executed scenario count is taken from the
preserved Cucumber and Maven console summaries. Stale named XML files from the red run are not
summed into the current HTTP suite results.

After the final shared-method and UTF-8 additions, the focused selection again passed
**44 unit tests and 39 integration tests**. A final clean HTTP build passed all 281 unit and 79 integration tests from regenerated classes.
The Python harness tests also passed both real child failure and timeout cases. This focused result overlaps the full HTTP result; the counts must
not be added together.

## Current task status

| Tasks | Current evidence and limits |
|---|---|
| T1: inventory | The architecture records HTTP, WebSocket, SOAP and typed consumers. A verbose resolved dependency tree and complete Maven runtime/test JAR inventories are retained. The original requirement to disposition every production search hit remains broader than this defect-correction pass. |
| T2: baseline | The port-conflict blocker is resolved. The required target modules and their reactor dependencies passed across the rerun and resumed build. |
| T3: shared fixtures | Both engines run the same payload, metadata, status, HEAD, cookie and method fixtures on ephemeral ports. TRACE's differing behavior is explicitly asserted. |
| T4: mapping | Reproduced cookie, framing, path and conversion-failure defects are fixed and tested. Arbitrary Servlet payload conversion and extension cookie attributes are not claimed compatible. |
| T5: options/executor | Options are captured at start; the executor and admission limit belong to that generation. Resource-limit and configuration regressions pass. |
| T6–T7: lifecycle/bridge | Idempotent start, destroy state, pending-request cancellation, old-queue isolation and worker termination pass. Shutdown failure is visible and prevents restart until an uncooperative worker exits. |
| T8: wire behavior | Existing Citrus actions pass, an incorrect expectation fails as expected, and shared fixtures cover escaped/duplicate/valueless queries and context/protocol metadata. |
| T9: concurrency | Real default-adapter reverse-order replies and cross-server isolation pass, alongside existing event-loop exclusion, disconnect and overload tests. |
| T10–T11: limits/shutdown | Early streaming limits, incomplete-upload timeout/closure, worker cleanup, queued-request isolation and bounded failed shutdown are verified. |
| T12: regressions/convergence | Required suites pass. Dependency mediation is inspected; the fixed baseline mixes Netty 4.2.16 and 4.1.136 modules. No linkage failure was observed in the tested HTTP/1.1 subset. This is not a clean-convergence claim. |
| T13–T14: measurement | The corrected harness verifies HTTP/1.1, uses eight clients and both payload fixtures, retains raw samples and median/range, bounds child lifetimes and records process/server startup separately. The campaign passed in 20 fresh JDK 21 processes with 20,000 measured requests. Results are linked from the decision report. Runtime and shared-test inventories are separate; a future Jetty-free package is not modeled. |
| T15: report/documentation | The decision report incorporates verified fixes and limits in natural language. Stage 05 provides concrete ordinary-HTTP, retained-TLS and retained-Servlet examples. |

The original all-tasks-complete Stage 04 gate is **not** asserted. Complete public configuration
classification and production-search-hit disposition remain open; the decision report makes a
bounded recommendation from the observed results. This correction pass does not authorize or
implement a production migration.

## Reproduction and provenance

Commands were run from the `citrus-vertx-eval` worktree at detached commit
`e0a55e10e6de5617c6046f1e6b8f40eb2b75fdf0`, with uncommitted test-only additions. Maven 3.9.8
and cached dependencies (`-o`) were used. The previous reports and original audit evidence are
preserved. Current reproduction commands are in [Stage 05](05-documentation.md); exact results,
classpath inventories and raw measurements are in `../audit-followup-evidence/`.

The capability delta remains `none`. There is no production capability archive to apply.

## Context-path and URL-mapping verification (14 October 2026)

Completed the routing extension in test sources. `VertxHttpRoute` validates and captures one
context and one dispatcher mapping before server resources are created. Route selection runs
before body buffering and admission. The bridge sets the Citrus context header while preserving
the complete request path. Configuration changes take effect at the next startup.

The initial 38 socket cases reproduced the absent routing behavior and established Jetty's
301 redirect. They also exposed a Spring path-parsing error for a semicolon on the mapped prefix;
the comparison now explicitly records Jetty's 500 and the prototype's successful handling.
All 38 cases pass with the routing implementation. Sixteen unit cases check invalid configuration,
ambiguous paths and root aliases. Additional integration tests check snapshot/restart behavior
and a real Citrus receive/send exchange at `/api/orders/42`.

A clean HTTP module build on JDK 21 passed 297 unit tests and 119 integration tests, with zero
failures or skips. These totals include 60 prototype unit tests and 79 prototype integration
tests. See [the complete build log](../routing-evidence/clean-http.log). The other module results
above come from the existing regression run; production code and dependencies were not changed
by this extension. The broader production-migration gate remains open as described above.

The current implementation also completed the 20-process benchmark campaign on JDK 21, with
20,000 verified requests. Measurements use the default root route, with route selection enabled;
they do not measure every mapping pattern. Results are in `../routing-evidence/measurements/`.

## Basic authentication extension

Implemented an immutable single-user credential checker and captured it with each server
startup. Authentication is disabled by default. Requests selected by the URL rule are checked
before body buffering and admission. Failed checks return 401 with an escaped realm challenge,
then close the connection. No Jetty or Vert.x Auth dependency was added.

The initial 16 socket cases failed for six missing prototype rejection paths. They also exposed
Jetty's HTTP 500 response for invalid Base64, now explicitly distinguished from the prototype's
401. The focused run passes seven unit cases and 23 integration cases, including the existing
Citrus action tests. Coverage includes credential changes across restart, disabling authentication,
duplicate headers and rejecting incomplete unauthorized uploads without invoking the adapter.

The initial implementation run also caught an assertion that incorrectly expected a successful
response body for Jetty's recorded 500 case. The corrected fixture checks success bodies only
for 200 replies; the expected 500 and absence of adapter invocation are still asserted.
See `../auth-evidence/red.log`, `../auth-evidence/first-implementation.log` and
`../auth-evidence/focused.log`. Final module verification follows below.

Final Basic authentication verification: the clean HTTP build passed 304 unit tests and 138
integration tests with no failures or skips, including 67 prototype unit tests and 98 prototype
integration tests. Runtime: JDK 21. Compiler target remains Java 17. See
[the complete build log](../auth-evidence/clean-http.log). The broader migration gate remains open;
roles, external authentication services, production registration and TLS are outside this extension.

## Form and multipart conversion extension

Added a Servlet-independent Spring form converter to the prototype message mapper. Nonempty
URL-encoded requests become multi-value maps; form and multipart response maps are written to
bytes with the converter's generated headers. The server's existing body limits and worker
execution apply before inbound conversion and around outbound conversion. Raw String/byte-array
responses and raw multipart uploads retain their existing representation.

The initial socket comparison reproduced absent prototype form decoding, map serialization and
multipart boundaries. It also showed that the current Jetty/Spring request path drops later
repeated fields in a plain form, represents a valueless field as empty, and returns a String for
a charset-parameterized form. The corrected comparison records those differences explicitly.
The prototype preserves the direct Spring form reader's values instead of reproducing the loss.

Six converter unit tests and eighteen focused integration tests pass. The integration selection
includes eleven socket cases and the seven Citrus action integration methods. Tests cover
binary multipart output, named resources, custom part headers, repeated/empty form values,
explicit charset decoding, HEAD, conversion errors and the upload-size limit. Real Citrus form
validation uses its expected XML control format; supplying a map as the control message was
rejected by that validator during fixture development, as recorded in the first action log.
No production source, dependency version or public endpoint registration changed.

The clean HTTP build passed 310 unit tests and 151 integration tests, with zero failures or
skips. The totals include 73 prototype unit tests and 111 prototype integration tests. All ran
on JDK 21; the compiler target remains Java 17. See
[the complete build log](../form-evidence/clean-http.log). The general production-migration gate
remains open; this extension adds only the bounded conversion behavior described above.

The final implementation completed the existing benchmark in 20 fresh JDK 21 processes with
20,000 verified text/binary requests. Results are in `../form-evidence/measurements/`. The workload
uses the root route with authentication disabled; it does not benchmark form/multipart payloads.

## Response tracing cache extension

Implemented a synchronized bounded response cache owned by each server generation. It stores
converted response plans by exchange ID with defensive header/body copies, FIFO eviction,
zero-capacity disablement and negative-capacity validation. The default comes from Citrus's
HTTP response-cache setting. Startup captures capacity; shutdown atomically closes and clears
the cache, so late work cannot restore entries. No cached entry is used to answer another request.

The initial HTTP test failed because no entries were retained. The implementation passes four
unit tests and ten focused integration tests. Coverage includes a real Citrus reply retrieved
from the cache, capacity boundaries, mutable input/output isolation, concurrent writers,
closed-cache writes, error/HEAD inclusion and authentication rejection exclusion. A component
test confirms the existing Citrus controller's early eviction at capacity two; the prototype
retains the full configured capacity instead of reproducing that threshold behavior.

The Servlet logging interceptor is not connected to this API: the prototype has exchange IDs
and serialized response plans rather than Servlet request keys and ResponseEntity values.
Production sources and dependencies remain unchanged. Evidence is under `../cache-evidence/`.

The clean HTTP build passed 314 unit tests and 154 integration tests with zero failures or
skips, including 77 prototype unit tests and 114 prototype integration tests. Runtime was JDK 21;
bytecode remains Java 17. See [the complete build log](../cache-evidence/clean-http.log).
The broader production-migration gate remains open as described above.

The cache-enabled implementation completed the benchmark in 20 fresh JDK 21 processes with
20,000 verified text/binary requests. Updated measurements are in `../cache-evidence/measurements/`.
Both engines use their default tracing-cache setting; the report documents the differing
retention rules. Authentication remains disabled in this benchmark workload.

## Custom message-body converter extension

Implemented typed registrations for Spring HttpMessageConverter instances, including converters
for a supertype such as Object. The prototype captures the ordered registration list at startup,
selects the first matching reader/writer and preserves the existing default conversion fallback.
The Citrus HTTP converter remains between body conversion and adapter dispatch. Converted output
headers are retained with fresh Content-Length and without conflicting Transfer-Encoding.

HTTP tests reproduce the missing typed conversion and verify worker execution, list snapshots,
restart, headers, HEAD and 400/500 failures. A real Citrus action exchange combines a custom
body converter with a custom Citrus HTTP converter. Unit tests check registration validation,
selection order, default fallback and specific payload classes with general Object converters.
An initial full run was stopped to extend the registration type to accept supertype converters;
the complete clean verification below includes that correction. Servlet/MVC advice and generic
type-based selection remain outside scope. Production sources/dependencies are unchanged.

The complete clean HTTP build passed 317 unit tests and 156 integration tests with no failures
or skips, including 80 prototype unit tests and 116 prototype integration tests. All ran on JDK 21;
compiled bytecode remains Java 17. See [the build log](../converter-evidence/clean-http.log).
The broader production-migration gate remains open.

The final implementation completed the existing benchmark in 20 fresh JDK 21 processes with
20,000 verified text/binary requests. Results are in `../converter-evidence/measurements/`.
This workload uses default body conversion, not a custom-converter performance workload.


## Gzip verification

Added bounded JDK gzip decoding before Citrus message conversion and gzip response encoding
on the exchange workers after conversion. Shared Jetty/prototype text and binary exchanges
first reproduced missing prototype support, then passed. Tests also cover corrupt data,
expanded upload limits, unsupported request codings, response exclusions, framing, Vary,
quality values and a real Citrus receive/send exchange.

The clean JDK 21 HTTP build passed 319 unit tests and 160 integration tests, with no failures
or skips. Prototype class files retain Java 17 compatibility (major 61). See
[gzip evidence](../gzip-evidence/clean-http.log) and [behavior](../gzip-handling.md).


## Parsed multipart upload verification

Completed the opt-in request converter and immutable upload values. Initial tests reproduced
missing parsing. Tests then identified and covered empty-part preservation in Spring 7.0.9,
including input buffer segmentation, and converter selection for requests without Content-Type.
Focused HTTP and Citrus action tests passed. The clean JDK 21 HTTP build passed 324 unit tests
and 162 integration tests, with no failures or skips. Prototype classes retain Java 17 bytecode
compatibility (major 61). Evidence and source hashes are in [multipart evidence](../multipart-evidence/clean-http.log).

Coverage includes repeated fields, empty fields/files, binary files, headers, charset decoding,
malformed/truncated input, exact byte boundaries, byte/part/header limits, defensive copies,
ordinary-request fallback, gzip uploads, and a Citrus receive/send exchange. Default raw-body
behavior is retained. Parsed values do not implement Servlet Part or automatic file storage.
No production sources or dependency declarations were changed for this extension.


## Equivalent handler and filter verification

Implemented generation-owned registration snapshots and a synchronous worker pipeline. Four
initial failing tests demonstrated missing filter order, short-circuiting, continuation guards
and registration copying. The implementation passes those cases and socket tests for custom
handlers, fallback, error replies, framing, gzip, tracing, route/authentication/body-limit ordering,
restart snapshots and interruption of a blocked filter on stop. A real Citrus receive/send test
checks request/reply changes and restoration of reserved reply-correlation metadata.

The clean JDK 21 HTTP build passed 328 unit tests and 168 integration tests, with no
failures or skips. Prototype classes retain Java 17 bytecode compatibility (major 61). Evidence,
XML reports and source hashes are in [handler evidence](../handler-evidence/clean-http.log).
Self-review confirmed per-request continuation state, callback execution on owned workers,
unchanged transport limits and response ownership, and no added Servlet container or dependency.
The equivalent hooks do not implement Servlet binary compatibility.


## HTTPS/TLS verification

The initial real HTTPS test reproduced a plaintext-listener SSL failure. TLS configuration now
uses Vert.x PKCS12 options and the JDK engine, with TLS 1.2/1.3 and optional required client
certificates. Focused tests verify trust and hostname checks, required client identity, protocol
restriction, startup failure with a bad store password, successful corrected startup, restart
configuration and returning to plain HTTP. A Citrus receive/send exchange passes over TLS 1.3.
Test certificates are generated by the running JDK in temporary directories; clients use normal
trust stores and hostname verification.

The clean JDK 21 HTTP build passed 329 unit tests and 173 integration tests, with no
failures or skips. Prototype classes retain Java 17 bytecode compatibility (major 61). See
[TLS evidence](../tls-evidence/clean-http.log) for the build, XML reports and source manifest.
No Jetty connector or dependency change is introduced. HTTP/1.1 remains the evaluated protocol;
PEM/JKS inputs, SNI selection, hot reload and TLS performance are outside the verified scope.


## Roles, login services and path policy verification

Implemented immutable first-match rules, default deny, pluggable worker login and trusted identity
metadata. Initial tests reproduced missing role/method, public-route and credential validation.
Tests now cover roles, methods, public/denied/unmatched paths, malformed and duplicate credentials,
encoded and semicolon paths, immutable configuration, identity spoofing, provider exceptions,
worker execution, policy restart snapshots and mixed-mode startup rejection. Upload limits run
before login, and stop interrupts a blocked login callback. A required-role Citrus exchange passes
over TLS 1.3, with trusted metadata restored after a filter attempts to replace it.

The clean JDK 21 HTTP build passed 334 unit tests and 177 integration tests, with no
failures or skips. Prototype classes retain Java 17 bytecode compatibility (major 61). See
[security evidence](../security-evidence/clean-http.log) for the build, XML reports and source hashes.
The login-service interface is verified; no concrete LDAP/OIDC/JAAS/database connector or Jetty
LoginService compatibility is claimed. No production dependency changes were made.

## Native WebSocket verification

Completed endpoint integration, bounded queues and sessions, complete text/binary messages,
Citrus send/receive support, broadcast and targeted writes, transport ping/close and generation
cleanup. Initial compilation demonstrated the missing endpoint API. The first wire run exposed
Vert.x's requirement that the HTTP request remain unread until upgrade; the implementation now
pauses upgrades before worker authorization and upgrades them on the event loop. The focused
suite passed 2 unit tests and 19 integration tests. The clean JDK 21 HTTP build passed
336 unit and 182 integration tests, including
99 prototype unit and 142 prototype integration tests.
No failures or skips. Tests cover frame and aggregate size limits, session and queue limits,
restart, waiting receive termination, endpoint ownership, path rejection, HTTP coexistence,
trusted WSS, 401/403 denial and real Citrus actions. All 90 prototype classfiles
retain major version 61. Logs, XML results and source hashes are in
[WebSocket evidence](../websocket-evidence/verified-source-manifest.json).

The existing HTTP benchmark is retained with its date/scope and explicitly predates this
extension. No WebSocket performance or complete existing-module compatibility is claimed.

The existing Jetty WebSocket module also passed 28 unit and 2 integration tests on JDK 21,
with no failures or skips; see [regression log](../websocket-evidence/existing-websocket.log).
Markdown links and whitespace checks passed.

## Native SOAP verification

Completed exchange integration on the shared Vert.x listener: a SOAP 1.1/1.2 exchange
converts POST requests to Citrus `SoapMessage`s through the existing `WebServiceEndpoint`
and `SoapMessageConverter`, with SAAJ factories selected per content type. Initial runs
reproduced a missing test-jar dependency and an XML parser hardening attribute the Xerces
factory rejects; the parser now applies those attributes best-effort while DOCTYPE
rejection and disabled external entities still block entity expansion. The focused suite
passed 2 unit tests and 6 integration tests. Tests cover both SOAP versions through real
Citrus client send/receive actions, the explicit-status exchange with an empty body,
rejection of malformed envelopes, wrong media types and non-POST methods before the
adapter runs, the `MustUnderstand` fault and attachment rejection. No failures or skips.
Logs and XML results are in [SOAP evidence](../soap-evidence/). The fixture requires
`text/xml` and `application/soap+xml` as binary media types, which are not defaults.
Bisect probes showed the full `citrus-ws` module does not pass in one JVM with the
prototype test included: the pre-existing Spring tests bind fixed ports (13080/61616),
and merely extending `TestNGCitrusSupport` in the same JVM collides with them. This is a
pre-existing test-isolation limit, unrelated to Vert.x code; the decision report records
the verified counts.

The existing Jetty SOAP module and the shared HTTP cases are unaffected: no production
endpoint registration, Servlet mapping, WSDL publishing, WS-Security, WS-Addressing or
attachment/MTOM support is claimed. No SOAP performance is measured.
