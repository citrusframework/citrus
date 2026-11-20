# Response tracing cache

The Vert.x prototype retains recent converted responses for inspection. This is a tracing cache,
not an HTTP response-reuse cache: repeated requests still invoke the Citrus adapter and obtain
new replies. The implementation lives entirely in test sources.

```java
server.setResponseCacheSize(100); // Configure before start; zero disables caching.
server.start();

// After a request has produced a response:
var recent = server.getCachedResponses();
var response = server.getResponseCache(exchangeId);
```

The default capacity comes from `HttpServerSettings.responseCacheSize()`, using the same Citrus
system property/environment setting as the existing server: `citrus.http.server.response.cache.size`
or `CITRUS_HTTP_SERVER_RESPONSE_CACHE_SIZE`, defaulting to 100. A negative capacity is rejected.
Capacity changes take effect at the next start. The cache retains at most the configured number
of responses and evicts the oldest insertion when it exceeds that number. The limit counts
entries, not bytes; each retained response includes a copy of its body.

The exchange ID is available in the received Citrus message's `citrus_vertx_exchange_id` header.
Lookup returns a `ResponsePlan` containing status, headers, body bytes and the HEAD suppression
flag, or null when absent. `getCachedResponses()` returns a snapshot ordered by insertion.
Headers and map structure are read-only, and body arrays are copied on storage and retrieval,
so callers cannot modify retained data.

An entry is recorded when an admitted exchange submits its converted response to Vert.x. It is
not proof that the client received the response. Admitted error responses, such as body-limit
rejections, are included. Route, authentication and admission rejections happen before an exchange
exists and are not cached. HEAD entries contain the suppressed body and the computed response
headers. Stop closes and clears that generation's cache, and late work cannot repopulate it.
Restart begins with an empty cache; separate server instances own separate caches.

## Relationship to the existing Citrus controller

`HttpMessageController` caches `ResponseEntity` objects by `HttpServletRequest` so its Servlet
logging interceptor can inspect response content. The prototype instead exposes serialized
response snapshots by exchange ID; it does not provide Servlet request objects or wire itself
into `LoggingHandlerInterceptor`. Citrus receive/send actions still work normally.

The existing controller evicts while its queue size is greater than or equal to the configured
capacity. A comparison test confirms that capacity two leaves one entry after two responses.
The prototype retains up to the full configured capacity, including one entry at capacity one,
and explicitly supports zero to disable storage. This difference is intentional and tested.

## Verification

The [initial HTTP test](cache-evidence/red.log) reproduced the missing cache. Unit tests check
capacity one/zero/negative values, defensive copies, concurrent writers, closed-cache behavior
and the existing controller's eviction threshold. HTTP tests check response content, FIFO
eviction, absence of response reuse, configuration snapshots and restart cleanup, as well as
HEAD/error entries and exclusion of unauthenticated requests. A real Citrus receive/send exchange
produces a reply whose status, headers and body are then retrieved from the cache.

See the [focused tests](cache-evidence/citrus-actions.log) and
[clean HTTP module build](cache-evidence/clean-http.log), run on JDK 21.
