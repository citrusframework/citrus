# Context paths and URL mappings

A custom context path and a dispatcher URL mapping are routing features. They do not require
running Java Servlet classes. The evaluation prototype now implements one context and one
mapping per server, configured through `setContextPath` and `setServletMappingPath`.

| Configuration | Matching example | Nonmatching example |
|---|---|---|
| Context `/api`, mapping `/*` | `/api/orders` | `/apix/orders` |
| Context `/api`, exact `/orders` | `/api/orders` | `/api/orders/42` |
| Context `/api`, prefix `/orders/*` | `/api/orders/42` | `/api/ordersXYZ` |
| Context `/api`, extension `*.json` | `/api/nested/order.json` | `/api/order.json/extra` |
| Context `/api`, default `/` | `/api/anything` | `/other` |
| Context `/api`, empty mapping | `/api/` | `/api/other` |

Nonmatching requests receive HTTP 404 without entering Citrus or reserving an exchange worker.
A bare nonroot context, such as `/api?x=1`, receives HTTP 301 with a location of `/api/?x=1`.
The context header is `/api`, while Citrus's path and URI headers retain the full request path.
Settings are captured when the server starts and changes take effect after a restart.

The mapping categories follow the [Servlet specification's mapping rules](https://jakarta.ee/specifications/servlet/6.0/jakarta-servlet-spec-6.0).
This implementation selects only one Citrus dispatcher route. It does not implement precedence
between multiple Servlet registrations, request forwarding, filters, Servlet attributes or a
Servlet lifecycle. The production server remains Jetty.

## Observed difference and limits

For `/api/orders;key=a,b/42` with context `/api` and mapping `/orders/*`, the current Jetty/Spring
server returns HTTP 500. Spring rejects the combined context and Servlet prefix while parsing
the request path: `Invalid contextPath '/api/orders': must match to full path segments`.
The adapter is never called. The prototype returns HTTP 200 and supplies `/api/orders/42`
to Citrus. The comparison test explicitly expects this difference.

The prototype rejects dot segments, encoded separators, encoded semicolons, backslashes and
control characters with HTTP 400. A context consists of ASCII letters, digits, underscores and
hyphens in slash-separated segments, without a trailing slash; `/` and an empty string mean
root. This is deliberately narrower than full Servlet URI handling. URL mappings cannot contain
query strings, fragments, encoded characters or embedded wildcards except the supported prefix
and extension forms. Ordinary encoded spaces in request paths are covered by the comparison.

## Verification

The [initial test run](routing-evidence/red.log) demonstrates the missing routing behavior.
It also records the Jetty 301 redirect and Spring error described above. The comparison contains
38 socket cases across the two engines. Additional tests check configuration validation,
ambiguous paths, settings across restart, and a real Citrus receive/send exchange under `/api`.
The [clean HTTP build](routing-evidence/clean-http.log) records the complete module run on JDK 21.
