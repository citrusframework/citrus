# Parsed multipart uploads

The prototype can deliver a multipart upload as a `VertxMultipartUpload` payload to Citrus.
Enable it before starting the server:

```java
server.setMessageBodyConverters(List.of(new VertxMessageBodyConverter<>(
        VertxMultipartUpload.class,
        new VertxMultipartUploadConverter(1024 * 1024, 128, 8192))));
```

The limits above are one MiB for the complete decoded multipart body, 128 parts and 8192 bytes
for each part's headers. The server's upload-size and gzip expansion limits still apply. The
converter is request-only, and existing converter ordering decides which reader handles a request.
Without this registration, multipart uploads keep their existing raw-body behavior.

Use a Citrus receive action's validation callback to inspect the payload:

```java
http().server(server).receive().post("/upload").message()
    .validate((message, context) -> {
        VertxMultipartUpload upload = message.getPayload(VertxMultipartUpload.class);
        var file = upload.parts("file").get(0);
        assertEquals(file.filename(), "a.bin");
        assertEquals(file.bytes(), expectedBytes);
        assertEquals(upload.parts("tag").get(0).text(), "one");
    });
```

`parts()` retains wire order, and `parts(name)` returns every part with that name. Parts expose
read-only, case-insensitive headers, a filename when supplied, and copied bytes. Empty fields
and empty files are retained. File bytes remain binary; form fields use Spring's text decoding.
`text()` uses the part's declared charset or UTF-8. Filenames are metadata only: the converter
does not create files or resolve client-supplied paths.

Parsing runs on the existing exchange workers. The complete input is bounded before parsing,
and no part can exceed the in-memory threshold, so the parser does not spill to disk. Returned
values have no parser buffers or temporary files attached and remain usable after the exchange.
Malformed or truncated uploads and converter-limit failures return HTTP 400 before the adapter
is called. The server's separate overall upload and gzip expansion limits return HTTP 413.

The implementation uses Spring's existing multipart reader. Spring 7.0.9 can drop empty parts
when a delimiter falls wholly within one input buffer. Splitting buffers inside delimiters
avoids that behavior without modifying the input. Regression tests cover empty parts and limits.
See the [Spring reader API](https://docs.spring.io/spring-framework/docs/7.0.9/javadoc-api/org/springframework/http/codec/multipart/DefaultPartHttpMessageReader.html).

This supports parsed upload values, not Servlet `Part`, `request.getParts()`, Servlet multipart
configuration, nested multipart parsing or automatic disk storage. There is no new Jetty parsed-part
comparison; its existing raw-body upload cases remain covered. Tests exercise repeated fields,
custom headers, binary and empty files, charset handling, malformed bodies, resource limits,
gzip uploads and an actual Citrus receive/send exchange.

Verification: [clean JDK 21 HTTP build](multipart-evidence/clean-http.log).
