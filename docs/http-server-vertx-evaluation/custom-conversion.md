# Custom message conversion

The Vert.x prototype supports two conversion layers. A Spring HTTP body converter reads or writes
the payload bytes. The existing Citrus HTTP message converter maps that payload and HTTP headers
to or from a Citrus message. Both layers can be customized, and their combined operation is tested
through real Citrus receive/send actions.

Register Spring converters before starting the prototype:

```java
server.setMessageBodyConverters(List.of(
    new VertxMessageBodyConverter<>(MyPayload.class, new MyPayloadConverter())
));
server.start();
```

`MyPayloadConverter` implements Spring's `HttpMessageConverter<MyPayload>` (a converter for a
supertype such as Object is also accepted). Each registration supplies the concrete class to pass
to its read method. Registrations are tried in order. Incoming selection uses `canRead` with that
class and the request media type; outgoing selection checks the payload's type and `canWrite`
with the response media type. The first match wins. If no converter matches, the prototype uses
its existing text, binary, form or multipart conversion. Null replies retain the default empty
response behavior.

The registration list is copied when configured and captured at startup. Changing the list takes
effect after restart. Converter instances themselves are not cloned: configure them before use,
make them safe for concurrent calls, and do not mutate them while the server is running.
Converters run on the exchange worker, outside the Vert.x event loop. Input is subject to the
server's body/admission limits before conversion; output is buffered before writing.

Custom headers from writers are retained, while Content-Length is recalculated from the actual
bytes and Transfer-Encoding is removed. HEAD keeps the calculated length but sends no body.
Read failures produce HTTP 400; write failures produce an empty HTTP 500. Both failures are kept
in the prototype's failure collection, and successful response plans are available in its trace
cache.

The existing `server.getHttpEndpointConfiguration().setMessageConverter(...)` API continues to
configure the Citrus HTTP converter. It receives the custom reader's typed payload on input;
its output entity is passed to the selected writer on output. As in the existing prototype bridge,
this converter is invoked without a Citrus TestContext argument.

## Scope and verification

This is explicit Class/media-type selection, not a Spring MVC runtime. It does not implement
controller method argument resolution, RequestBodyAdvice/ResponseBodyAdvice, Accept-based content
negotiation, generic Type/ResolvableType dispatch, or Servlet-aware converter contexts. It also
does not register a public Citrus endpoint or XML/YAML configuration for the prototype.

The [initial HTTP test](converter-evidence/red.log) reproduced missing typed conversion. Focused
tests cover first-match ordering, unmatched fallback, media-type charset parameters, general
Object converters used with a specific payload class, defensive list capture, restart behavior,
worker-thread execution, custom headers, HEAD and conversion errors. A real Citrus exchange
combines a custom body reader/writer with a custom Citrus HTTP converter and validates the result.
See [the Citrus action tests](converter-evidence/citrus-actions.log) and
[the complete HTTP build](converter-evidence/clean-http.log), run on JDK 21.
