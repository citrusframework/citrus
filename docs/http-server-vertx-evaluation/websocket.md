# WebSocket messages through Citrus

The test-only Vert.x listener can accept WebSocket connections and expose their messages through
Citrus's ordinary send and receive actions. It uses Vert.x directly and adds no Jetty dependency.

```java
var messages = new VertxWebSocketEndpoint("/ws");
var server = new VertxHttpServer();
server.setPort(8080);
server.setWebSocketEndpoint(messages);
server.initialize();
server.start();

// After a client connects to ws://localhost:8080/ws and sends a message:
receive().endpoint(messages).message().body("hello").build().execute(context);
send().endpoint(messages).message().body("welcome").build().execute(context);
```

A String payload produces a text message; byte[] produces a binary message. Incoming fragments
are assembled into a complete message. Each received message includes `citrus_websocket_id`,
`citrus_websocket_path` and `citrus_websocket_is_last` (true). To reply to one connection, copy
its ID into the outgoing message's `citrus_websocket_id` header. Without that header, send
broadcasts to the active connections. Sending to a missing connection, or with no active
connections, fails the Citrus action. Broadcast writes are not atomic: a failed send can leave
some clients with the message, so retrying may produce duplicates.

The endpoint uses one exact full path. With a server context of `/api`, configure `/api/ws`.
The listener's context and URL mapping must also admit that path. Ordinary HTTP requests still
use the normal HTTP pipeline. An upgrade to an unconfigured path receives 404. Upgrade requests
must be GET requests without a body or transfer encoding.

Routing, single-user Basic authentication and path/role policies apply before upgrading. Policy
login services run on the bounded workers while the request is paused. Successful policy login
adds the trusted username and roles to each incoming message. Authorization is established at
the handshake; it is not reevaluated for later frames. Configuring `server.setTls(...)` enables
`wss://` on the same listener. Tests exercise trusted WSS and missing credentials, wrong passwords
and insufficient roles, followed by a successful exchange using real Citrus actions.

The default limits are 16 sessions, 64 queued incoming messages and 65,536 bytes per message or
frame. To change them, use `new VertxWebSocketEndpoint("/ws", sessions, queueSize, messageBytes)`.
The session limit returns 503 at the handshake. A full incoming queue closes the sending
connection with 1013. Oversized frames or assembled messages close it with 1009. Outgoing
messages have the same size limit; at most one write per session is pending, and concurrent sends
fail while that write is pending. Send and receive use the endpoint timeout, initially 5 seconds.
A send timeout does not prove that the client received nothing.

Stopping the server closes sessions, clears queued messages and wakes waiting consumers. Restart
uses a fresh queue and new session IDs, so an old targeted reply cannot reach a new client. An
endpoint cannot belong to two running servers. The endpoint selection is captured at startup.

Ping/pong and close handshakes are handled by the transport. Control frames are not delivered as
Citrus messages. WebSocket messages do not use HTTP body converters, HTTP response tracing or the
HTTP handler/filter callbacks. This prototype provides no Servlet WebSocket API, dedicated
WebSocket client, public DSL/XML registration, message selectors, subprotocol negotiation,
compression configuration or streaming API. It is not a migration of the existing WebSocket
module or a claim of complete feature parity.

The focused tests are `VertxWebSocketEndpointTest`, `VertxWebSocketIT` and
`VertxHttpServerRoundtripIT.secureWebSocketUsesCitrusActions`. The existing HTTP performance
measurements do not measure WebSocket throughput, latency or memory use.
