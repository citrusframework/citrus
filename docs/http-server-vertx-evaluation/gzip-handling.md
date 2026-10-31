# Gzip requests and responses

The Vert.x prototype decodes gzip request bodies before converting them to Citrus messages.
It compresses eligible replies when the request accepts gzip. This is automatic and uses JDK
gzip streams on the exchange workers. It does not require a Servlet filter or a new dependency.

Text and binary exchanges are compared with the current Jetty server over HTTP. A separate
integration test sends a compressed request through the default Citrus adapter, validates its
decoded body with a Citrus receive action, and compresses the reply from a Citrus send action.

The upload limit applies separately to bytes received from the network and bytes produced by
decompression. The decoder checks the expanded size before appending each block. Invalid or
corrupt gzip returns 400; exceeding the expanded limit returns 413; unsupported or stacked
request content codings return 415. These requests do not reach the Citrus adapter. Original
request headers remain available to Citrus, so Content-Length describes the compressed wire
body even though the message payload has been decoded.

Response selection recognizes case-insensitive gzip tokens, quality values and wildcard fallback.
An explicit `gzip;q=0` overrides a wildcard. Eligible responses include `Vary: Accept-Encoding`,
merged with any existing Vary value. Compression happens after message conversion; Content-Length
is recalculated and Transfer-Encoding removed. The tracing cache therefore receives the final
compressed bytes. Existing Content-Encoding is preserved without applying compression again.
HEAD, empty bodies, informational responses, statuses 204/205/304, and range responses are not
compressed automatically.

This is a gzip capability rather than full content-coding negotiation: other response codecs,
relative preferences between identity and gzip, and 406 handling when identity is forbidden are
not implemented. The existing Jetty filter selects gzip using a substring check; the prototype
instead respects `gzip;q=0`. They should not be described as identical in all negotiation cases.

Evidence: [clean HTTP test run](gzip-evidence/clean-http.log). The footprint workload uses ordinary
uncompressed requests and does not measure gzip throughput or compression ratios.
