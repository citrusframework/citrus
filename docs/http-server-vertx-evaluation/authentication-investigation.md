# Basic authentication

The prototype supports an optional username, password and realm for all requests selected by its
URL rule. It checks credentials before reading the body or reserving an exchange worker. Missing,
wrong, malformed or duplicated credentials receive HTTP 401 with a `WWW-Authenticate` challenge
and an empty response body. The connection closes after that response, including when an upload
is incomplete. Accepted requests continue through the existing Citrus receive/send actions.

```java
server.setBasicAuthentication("test-user", "test-password", "citrus-tests");
server.start();
```

Authentication is disabled by default. `clearBasicAuthentication()` disables it at the next start.
Changes to credentials or the realm also take effect at the next start. All fields must contain
printable ASCII; usernames must be nonempty and cannot contain a colon. Passwords may contain
colons or be empty. Quotes and backslashes in realm names are escaped in the challenge.

This single-user mode does not provide roles or path policies; use the separate
[policy security configuration](security-policies.md) for those and a login-service callback.
Neither mode provides Servlet principals. Requests rejected
or redirected by the URL rule are handled before authentication. Authenticated requests retain
their Authorization header through the normal Citrus message mapping.

Basic authentication encodes credentials rather than encrypting them; the
[Basic authentication specification](https://www.rfc-editor.org/rfc/rfc7617) calls for transport
protection such as TLS. Configure the prototype's [HTTPS listener](tls.md) when transport protection
is required. Use disposable credentials in local tests. This remains a test-only endpoint.

## Comparison and verification

Sixteen socket cases exercise both engines under a custom `/api` context: valid credentials,
missing credentials, wrong user or password, a different scheme, malformed Base64, missing colon,
and case-insensitive scheme names. Correct credentials work in both engines. Missing and wrong
credentials receive 401. For `Authorization: Basic %%%`, Jetty's decoder throws an exception and
returns 500; the prototype returns 401. This difference is asserted explicitly.

Additional tests cover invalid configuration, empty passwords, escaped realm names, duplicate
headers, incomplete uploads, credential changes across restart and a real Citrus receive/send
exchange. Existing HTTP tests continue to exercise authentication disabled by default.
The [initial failing tests](auth-evidence/red.log) and [complete HTTP verification](auth-evidence/clean-http.log)
record the results on JDK 21. Production code and dependency versions are unchanged.


## Roles and external login services

The single-user setup remains available. For roles or path/method policies, use the separate
[security configuration](security-policies.md) with a login-service callback. It runs on exchange
workers, supports first-match access rules and denies unmatched paths. The two configuration modes
cannot be combined. A required-role Citrus receive/send exchange is verified over HTTPS.
