# Form and multipart conversion

The test-only Vert.x/Citrus bridge uses Spring's existing `FormHttpMessageConverter` for
URL-encoded forms and multipart output. No dependency or production endpoint changed.

Nonempty `application/x-www-form-urlencoded` requests become a `MultiValueMap<String, String>`
inside the Citrus message. Repeated names keep all values, plus signs decode as spaces, percent
escapes decode using the declared charset, and empty or valueless fields remain distinguishable.
Without an explicit charset, the prototype uses its configured charset (UTF-8 by default).
An empty request body retains the existing empty-string behavior. A configured binary media type
takes precedence and preserves raw bytes.

For responses, supply a `MultiValueMap` payload and the form or multipart content type. Existing
String and byte-array response payloads remain raw, so already-encoded form text is not encoded
a second time. Form maps are URL-encoded; multipart maps generate a matching body boundary and
Content-Type header. Text, byte arrays and Spring `Resource` parts are supported, including named
resources and an `HttpEntity` wrapper for custom part headers. The complete response length is
computed after conversion; HEAD responses keep that length and omit the body.

```java
var fields = new LinkedMultiValueMap<String, Object>();
fields.add("name", "a b");
fields.add("name", "c+d");
var reply = new HttpMessage(fields)
        .status(HttpStatus.OK)
        .contentType("application/x-www-form-urlencoded");

// Existing Citrus response action:
http().server(server).send().response(HttpStatus.OK).message().from(reply);
```

For multipart responses, use `multipart/form-data`, with text, byte arrays or resources in the
map. `multipart/mixed` is also accepted by the writer, but the shared wire comparison exercises
`multipart/form-data`. The Spring form converter's supported direction is described in its
[API documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/converter/FormHttpMessageConverter.html).

Incoming multipart uploads remain raw bodies by default, as in the current Citrus upload tests.
An opt-in converter provides [parsed upload values](multipart-uploads.md). Raw uploads are
not parsed into Servlet `Part` objects or written to disk. For binary uploads, include
`MediaType.MULTIPART_FORM_DATA` in the endpoint's binary media types so Citrus receives the entire
multipart envelope as bytes. Text-mode upload bodies use the declared or configured charset;
that mode is not suitable for preserving arbitrary binary bytes.

## Differences observed against Jetty

For the plain form `name=a+b&name=c%2Bd&empty=&flag`, the current Jetty/Spring server delivered
only the first `name` value and treated `flag` as an empty value. The prototype preserves both
name values and represents a valueless field with null, as Spring's direct form reader does.
With `charset=UTF-8` added to the form content type, the Jetty comparison delivered a String;
the prototype still decodes the form into a map. These cases are asserted separately rather than
reported as identical behavior.

## Verification

Shared socket cases cover form reading/writing, parameterized form content types, raw multipart
uploads and multipart responses. Assertions check repeated fields, file names, custom part
headers, boundary framing and exact binary file bytes. Additional tests cover ISO-8859-1,
invalid escapes, binary multipart preservation, raw output and HEAD behavior.

Real Citrus client/server action tests validate form fields with the existing form validator's
XML control format, and send/receive multipart resources in both directions. Invalid form
encoding returns 400, oversized multipart uploads return 413 before conversion, and an unsupported
outbound part returns an empty 500 with the failure retained by the prototype.
See the [initial failures](form-evidence/red-detail.log), [Citrus action tests](form-evidence/citrus-actions.log)
and [clean HTTP module build](form-evidence/clean-http.log), all on JDK 21.
