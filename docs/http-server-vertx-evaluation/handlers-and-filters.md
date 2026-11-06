# Custom handlers and filters

The prototype provides synchronous request handlers and filters without a Servlet container.
They use Citrus HTTP messages and run on the existing exchange workers. They do not add a Jetty
integration or any dependency.

Configure them before starting the server:

```java
server.setFilters(List.of((request, next) -> {
    if (request.getPath().equals("/private")) {
        return new HttpMessage("denied").status(HttpStatus.FORBIDDEN);
    }
    request.setHeader("X-Checked", "yes");
    Message response = next.proceed();
    if (response == null) {
        return null;
    }
    HttpMessage reply = new HttpMessage(response);
    reply.setHeader("X-Filtered", "yes");
    return reply;
}));

server.setHandlers(List.of(new VertxHttpHandlerMapping(
    request -> request.getPath().equals("/health"),
    request -> new HttpMessage("healthy").status(HttpStatus.OK)
)));
```

Filters run in registration order. Calling `next.proceed()` invokes the next filter; after the
last filter, the first matching custom handler runs. If no handler matches, the request goes to
the server's Citrus endpoint adapter. The reply returns through the filters in reverse order.
A filter may return immediately to stop processing, and handlers do not fall through after they
match. A null reply follows the server's existing empty-response/default-status behavior.

Each continuation is valid only during its filter call, on the same worker, and at most once.
Repeated, deferred or cross-thread calls throw `IllegalStateException`. A callback exception that
escapes the pipeline is retained in `getFailures()` and produces an empty HTTP 500 response.
Filters can catch downstream exceptions themselves if they want to choose a different reply.

Routing, authentication, admission, body limits, gzip decoding and request conversion run before
the pipeline. Hooks therefore cannot intercept unmatched paths or authentication failures, read
an unbounded body, or replace the transport's admission checks. Matching predicates see the
converted request after filter changes. Every handler/filter reply still passes through response
conversion, HEAD handling, gzip selection, framing and the tracing cache.

Filters can edit application headers and payloads. The server restores its exchange ID, generation
and default reply queue immediately before invoking the Citrus adapter so edits cannot replace
its reply-correlation metadata. A real Citrus receive/send test verifies this behavior.

Registration lists are copied and captured at startup. Changes take effect after stop/start;
callback objects remain caller-owned and must be thread-safe. There are no automatic Servlet-style
`init` or `destroy` callbacks. Stop interrupts workers and uses the existing shutdown budget. User
code that ignores interruption remains subject to the prototype's existing shutdown-failure and
restart rules.

These are equivalent extension points, not binary-compatible Servlet APIs. Existing Servlet
filters, Servlet handlers, dispatchers, request wrappers and asynchronous Servlet code need adapting.
The hooks operate on buffered, converted messages and do not expose a socket or streaming response.

## Request attributes

A request-attribute hook stages per-request attributes on the event loop before dispatch. It is
the analog of Servlet container filters calling `HttpServletRequest.setAttribute()`: staged entries
travel in the request snapshot and become Citrus message headers only when the existing
`handleAttributeHeaders` flag is enabled, mirroring `HttpMessageController`.

```java
server.setHandleAttributeHeaders(true);
server.setRequestAttributes((request, attributes) -> {
    attributes.put("app.user", currentUser(request));
});
```

Semantics match the Jetty path: every staged entry is copied with its verbatim name and raw
object value, overwriting any header of the same name. `null` names and values are skipped, since
`setAttribute(name, null)` removes rather than stores. The callback runs on the Vert.x event loop,
so it must be non-blocking; a throwing callback is retained in `getFailures()` and produces an
empty HTTP 500 response. Like the message exchange and filters, the hook is captured at startup
and changes take effect after stop/start. Gzip decoding carries attributes into the decoded
snapshot, so compressed requests keep their staged entries. Existing `HttpServletRequest`
attributes cannot be read without a Servlet container; only hook-staged entries are exposed.

Verification covers the enabled/disabled flag, verbatim names with raw non-string values,
attribute-over-wire-header overwrite, null skipping, gzip preservation, a throwing hook, and real
Citrus send/receive actions. See the [focused build](handler-evidence/attribute-headers.log).

## Interceptors

An interceptor chain runs on the exchange worker around custom-handler and Citrus-adapter
invocation, after routing, authentication, admission, body limits, gzip decoding, identity and
request conversion. It mirrors Spring's `HandlerExecutionChain` ordering: `preHandle` callbacks
run in registration order, `postHandle` in reverse order, and `afterCompletion` always runs in
reverse order for every interceptor whose `preHandle` ran, including short-circuits and failures.

```java
server.setInterceptors(List.of(
    VertxMappedInterceptor.include("/api/**", new VertxHttpInterceptor() {
        @Override
        public Message preHandle(HttpMessage request) {
            request.setHeader("X-Intercepted", "yes");
            return null; // continue; return a message to short-circuit with that reply
        }

        @Override
        public Message postHandle(HttpMessage request, Message reply) {
            HttpMessage wrapped = new HttpMessage(reply);
            wrapped.setHeader("X-Wrapped", "yes");
            return wrapped;
        }
    })));
```

`VertxMappedInterceptor` applies a delegate only to matching request paths using Ant patterns,
mirroring Spring's `MappedInterceptor`: at least one include pattern must match (an empty include
list matches every path) and no exclude pattern may match. A short-circuit reply skips the
handler and every `postHandle` but still completes previously applied interceptors, then flows
through the normal response conversion, gzip selection, framing and tracing cache.

Differences from `HandlerInterceptor`: there is no `HttpServletRequest`, `HttpServletResponse`,
handler object or `ModelAndView`, only the converted `HttpMessage`. A short-circuit returns the
reply message directly (in Spring MVC the interceptor writes the raw response itself). An
`afterCompletion` callback that throws propagates instead of being logged, failing the exchange
with an empty 500 response; a completion failure during error handling is attached as a
suppressed exception so the original error is never masked. Like filters and handlers, the list
is captured at startup and changes take effect after stop/start. Existing Spring interceptor
classes need adapting to this interface; Spring web contexts, `DispatcherServlet` and
annotation-driven `@Controller` detection remain unavailable.

Verification covers Spring ordering, short-circuit completion, handler and callback failures,
include/exclude patterns, reply replacement, and real Citrus send/receive actions for scoped
adapter traffic, skipped paths and custom-handler traffic. See the
[focused build](handler-evidence/interceptor.log).

Verification covers filter order, short-circuiting, first-match handlers, Citrus fallback,
continuation guards, startup snapshots, authentication/routing/body-limit ordering, callback errors,
HEAD and gzip responses, response tracing, stop interruption and real Citrus actions. See the
[clean JDK 21 HTTP build](handler-evidence/clean-http.log).
