# Roles, login services and path policies

The prototype supports a pluggable Basic-auth login service and ordered authorization rules.
This uses the existing Vert.x listener and exchange workers, without Jetty security handlers.

Supply a `VertxHttpSecurity.LoginService` implementation. Its `login(username, password)` method
returns an immutable `Identity` containing the canonical username and roles, returns null for
invalid credentials, or throws when authentication cannot be completed. The callback can call
an application's directory, database or authentication service. It must be thread-safe, set finite
timeouts for external calls and honor interruption. No particular LDAP, database, OIDC or JAAS
connector is bundled or verified; the implemented integration point is the login-service interface.

Configure policies before start, using your `loginService` instance:

```java
server.setSecurity(new VertxHttpSecurity("orders", loginService, List.of(
    new VertxHttpSecurity.Rule("/health", Set.of("GET"),
        VertxHttpSecurity.Access.PERMIT_ALL, Set.of()),
    new VertxHttpSecurity.Rule("/api/orders/*", Set.of("GET"),
        VertxHttpSecurity.Access.ANY_ROLE, Set.of("reader", "admin")),
    new VertxHttpSecurity.Rule("/api/orders/*", Set.of(),
        VertxHttpSecurity.Access.ANY_ROLE, Set.of("admin"))
)));
```

Rules use the full request path, including any configured context. They support the prototype's
exact, prefix, extension and default URL patterns, with its path decoding and semicolon handling.
The first matching path-and-method rule wins. Methods are uppercase; an empty method set matches
all methods. `ANY_ROLE` requires at least one listed role. `AUTHENTICATED` accepts any valid identity,
including one with no roles. `PERMIT_ALL` skips login and has no identity, even if credentials were
sent. `DENY_ALL` always denies access. Unmatched requests are denied; use an explicit rule when
anonymous access is wanted. Roles are case-sensitive.

Missing, malformed, duplicate or invalid Basic credentials on a protected path produce HTTP 401
with a realm challenge. Valid credentials without a required role produce 403. Explicitly denied
or unmatched requests also produce 403. A login-service exception produces an empty 500 and is
retained in `getFailures()`; it does not grant access or expose the exception in the response.
Credentials use printable ASCII, as in the existing single-user Basic mode. Configure
[HTTPS](tls.md) to protect credentials in transit.

The sequence is route validation, admission and bounded body reading, then worker authentication
and authorization, then decoding/conversion, filters, handlers and Citrus. External login work
never runs on an event loop. Unauthorized requests cannot reach converters, hooks or Citrus, but
their bodies are still read within the existing size and timeout limits before policy evaluation.
Body-limit errors can therefore precede authentication errors. Policies evaluate the original
request path and method, before any trusted filter changes them.

Successful protected requests carry `citrus_vertx_principal` (String) and `citrus_vertx_roles`
(immutable Set). Client-supplied values with those names are removed case-insensitively, including
on public requests and servers without policy security. Trusted values are restored before Citrus
fallback after filters. These are Citrus metadata, not Servlet principals or role APIs. The existing
Authorization header otherwise follows the normal message conversion rules.

Registration lists and policies are immutable and captured at startup. Configuration changes
require stop/start; the login service is called for each protected request and owns its account
state. There is no credential or role cache. Stop interrupts its worker; a provider that ignores
interruption is subject to the existing shutdown-failure and restart rules. Provider resources
remain caller-owned, with no automatic login-service init/destroy hooks.

Use either `setBasicAuthentication(...)` or `setSecurity(...)`. Configuring both fails startup
before listener allocation. Call `clearBasicAuthentication()` when switching to policy security;
`setSecurity(null)` removes policy security on the next start.

Verification covers public and protected routes, role/method checks, default deny, encoded and
semicolon paths, malformed/duplicate credentials, spoofed identity headers, provider errors,
worker execution, policy snapshots, upload limits and stop interruption. A real Citrus receive/send
exchange runs over HTTPS with a required role. See the [clean JDK 21 HTTP build](security-evidence/clean-http.log).

WebSocket upgrades use the same policies while the HTTP request is paused, before the upgrade.
They must be bodyless GET requests. The resulting identity is attached to messages for that
connection; later frames do not repeat authentication. See [WebSocket support](websocket.md).
