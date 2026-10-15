# HTTPS and client certificates

The prototype can serve HTTPS directly through Vert.x and the JDK TLS engine. It uses the same
Citrus adapters, conversion, filters, handlers and response tracing as plain HTTP, with no Jetty
connector or new dependency.

Configure a PKCS12 store containing the server's private key and certificate chain before start:

```java
server.setPort(8443);
server.setTls(new VertxHttpTls(Path.of(keyStorePath), keyStorePassword));
server.initialize();
server.start();
```

Clients connect to `https://` and must trust the certificate chain and validate its hostname.
TLS 1.2 and TLS 1.3 are enabled by default. Restrict protocols with
`configuration.withProtocols(Set.of("TLSv1.3"))` before passing the configuration to `setTls`.
The listener remains HTTP/1.1; ALPN and cleartext HTTP/2 are disabled. The handshake timeout is
10 seconds. Cipher selection uses the JDK/Vert.x defaults.

To require a trusted client certificate:

```java
server.setTls(new VertxHttpTls(Path.of(keyStorePath), keyStorePassword)
        .withClientAuthentication(Path.of(clientTrustStorePath), clientTrustStorePassword));
```

The client trust store is also PKCS12. Missing or untrusted client identities fail during the
TLS handshake, before any request reaches Citrus. This checks certificate trust; it does not
map client identities to Citrus roles or expose a Servlet security principal.

Configuration objects are immutable. Store files are loaded at startup, and configuration or
certificate changes require stop/start. Calling `setTls(null)` selects plain HTTP on the next
start. The listener uses the existing owned lifecycle and cleanup. A wrong store password fails
startup, leaves the server stopped and allows a corrected configuration to start successfully.
Store passwords are not included in the configuration's string representation.

Tests generate short-lived certificates with the JDK running the tests, then use actual trust
stores and normal hostname checks. They verify TLS 1.2/1.3, required client authentication,
untrusted server certificates, hostname mismatch, missing/untrusted client identities, protocol
restrictions, restart changes, invalid-store recovery and a real Citrus receive/send exchange.
No trust-all client or disabled hostname check is used.

This configuration currently accepts PKCS12 stores. PEM/JKS inputs, multiple TLS connectors,
SNI certificate selection, certificate hot reload, custom cipher configuration and HTTP/2 have
not been implemented or evaluated through this API. The footprint benchmark uses plain HTTP
and provides no TLS latency or throughput measurement. Jetty's existing HTTPS Cucumber scenario
remains separate regression evidence; this extension does not claim a shared TLS benchmark.

Evidence: [clean JDK 21 HTTP build](tls-evidence/clean-http.log).
Vert.x provides the underlying [TLS support](https://vertx.io/docs/4.5.31/vertx-core/java/#_ssl_tls).
