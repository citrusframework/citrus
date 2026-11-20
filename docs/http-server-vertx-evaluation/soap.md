# SOAP messages through Citrus

The test-only Vert.x listener can accept SOAP 1.1 and 1.2 requests and expose them through
Citrus's ordinary SOAP endpoint machinery. It uses Vert.x directly and adds no Jetty or
Servlet dependency. It reuses the existing `WebServiceEndpoint` and `SoapMessageConverter`
with a per-request SAAJ message factory; the existing SOAP module and its public
configuration remain unchanged.

```java
var server = new VertxHttpServer();
server.setPort(8080);
server.getHttpEndpointConfiguration().setBinaryMediaTypes(List.of(
        MediaType.APPLICATION_OCTET_STREAM,
        MediaType.valueOf("text/xml"),
        MediaType.valueOf("application/soap+xml")));
server.setMessageExchange(new VertxSoapMessageExchange());
server.setEndpointAdapter(myAdapter);
server.initialize();
server.start();
```

Only POST requests reach Citrus; other methods receive 405 with an `Allow: POST` header.
`text/xml` selects SOAP 1.1 with the `SOAPAction` header, while `application/soap+xml`
selects SOAP 1.2 with its `action` parameter. Other media types receive 415, and requests
that do not parse as a matching SOAP envelope receive 400. The exchange needs the wire
bytes, so both SOAP media types must be configured as binary media types; they are not in
the default set.

Requests are parsed as namespace-aware DOM with document types rejected and external
entities disabled. The envelope namespace must match the SOAP version implied by the
content type, the body must carry exactly one payload element, and non-blank text outside
elements is rejected. A `mustUnderstand` header targeted at the ultimate receiver or the
next role is answered with a 500 `MustUnderstand` fault before the adapter runs.

Citrus receives a `SoapMessage` whose `citrus_soap_action` header carries the unquoted
action. HTTP metadata arrives with the `citrus_soap_http_` prefix (`request_uri`,
`request_method`, `content_type`), alongside the inbound HTTP headers. Envelope headers
cannot supply internal routing metadata: `citrus_`-prefixed values taken from HTTP pass
through except the framework-managed message ID, which is left untouched.

Replies serialize the returned `SoapMessage`, using 500 for faults and 200 otherwise.
If the adapter returns no reply, the exchange ends with an empty 202. A reply carrying
the `citrus_soap_http_status_code` header ends the exchange with that status and an
empty body instead of a SOAP envelope. Replies with attachments or MTOM enabled are
rejected with 500: attachments, WSDL publishing, WS-Security, WS-Addressing, endpoint
mappings and Spring-WS annotation handling are not part of this fixture. It is not a
migration of the existing SOAP module or a claim of complete feature parity.

The focused tests are `VertxSoapMessageExchangeTest` and `VertxSoapIT`. They cover both
SOAP versions through real Citrus client send/receive actions, the explicit-status
exchange, rejection of malformed requests, wrong media types and non-POST methods before
the adapter runs, the `MustUnderstand` fault and attachment rejection. SOAP performance
has not been measured.
