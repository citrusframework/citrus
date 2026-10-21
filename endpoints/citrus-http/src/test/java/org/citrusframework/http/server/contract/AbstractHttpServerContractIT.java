/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.http.server.contract;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import jakarta.servlet.http.Cookie;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.AbstractCommonHttpServerBuilder;
import org.citrusframework.http.server.AbstractHttpServer;
import org.citrusframework.message.Message;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Shared contract test suite for all HTTP server implementations. The suite verifies the documented
 * behavior of the Citrus HTTP server on the wire level. Implementations extend this suite and only
 * provide the server under test.
 */
public abstract class AbstractHttpServerContractIT extends TestNGCitrusSpringSupport {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHttpServerContractIT.class);

    /** Servers started by the current test */
    private final List<AbstractHttpServer> runningServers = new ArrayList<>();

    /** Endpoint adapter recording the requests that reached the server */
    private ContractEndpointAdapter endpointAdapter;

    /**
     * Creates the server under test and applies the given options to its builder.
     *
     * @param options the options to apply.
     * @return the server under test.
     */
    protected abstract AbstractHttpServer createServer(Consumer<AbstractCommonHttpServerBuilder<?, ?>> options);

    /**
     * Whether the server implementation answers TRACE requests itself instead of delivering them to
     * Citrus.
     *
     * @return true if the server answers TRACE requests on its own.
     */
    protected abstract boolean isTraceAnsweredByServer();

    /**
     * Whether the server implementation rejects a request path with duplicate slashes instead of
     * normalizing it.
     *
     * @return true if the server rejects such a request.
     */
    protected abstract boolean isDuplicateSlashRejected();

    @AfterMethod(alwaysRun = true)
    public void stopServers() {
        for (AbstractHttpServer server : runningServers) {
            try {
                server.stop();
            } catch (Exception e) {
                logger.warn("Failed to stop server: {}", e.getMessage());
            }
        }

        runningServers.clear();
        endpointAdapter = null;
    }

    /**
     * Starts the server under test with a recording endpoint adapter attached.
     *
     * @param options the options to apply to the server builder.
     * @return the started server.
     */
    protected AbstractHttpServer startServer(Consumer<AbstractCommonHttpServerBuilder<?, ?>> options) {
        endpointAdapter = new ContractEndpointAdapter();

        AbstractHttpServer server = createServer(builder -> {
            options.accept(builder);
            builder.endpointAdapter(endpointAdapter);
        });

        startServer(server);
        return server;
    }

    /**
     * Starts the server under test with the default endpoint adapter. Requests are forwarded to the
     * inbound queue of the server and time out when no reply is sent.
     *
     * @param options the options to apply to the server builder.
     * @return the started server.
     */
    protected AbstractHttpServer startServerWithDefaultAdapter(Consumer<AbstractCommonHttpServerBuilder<?, ?>> options) {
        AbstractHttpServer server = createServer(options);
        startServer(server);
        return server;
    }

    private void startServer(AbstractHttpServer server) {
        runningServers.add(server);
        server.initialize();
        server.start();
    }

    /**
     * Gets the endpoint adapter recording the requests that reached the server.
     *
     * @return the endpoint adapter.
     */
    protected ContractEndpointAdapter getEndpointAdapter() {
        return endpointAdapter;
    }

    /**
     * HSRV-001: Requests under the context path reach Citrus.
     */
    @Test
    public void shouldDeliverRequestUnderContextPath() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/api");
        });

        getEndpointAdapter().setReplyProvider(request -> new HttpMessage("ok"));

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/api/orders/42");

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBodyAsString()).isEqualTo("ok");

        HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD)).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/api/orders/42");
    }

    /**
     * HSRV-001: Requests under the context path reach Citrus.
     */
    @Test
    public void shouldAnswerRequestOutsideContextPathWithNotFound() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/api");
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/other");

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(getEndpointAdapter().hasReceivedRequests()).isFalse();
    }

    /**
     * HSRV-001: Requests under the context path reach Citrus.
     */
    @Test
    public void shouldRedirectBareContextPath() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/api");
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/api?x=1");

        assertThat(response.getStatusCode()).isEqualTo(301);
        assertThat(response.getHeader("Location")).isEqualTo("/api/?x=1");
        assertThat(getEndpointAdapter().hasReceivedRequests()).isFalse();
    }

    /**
     * HSRV-002: An HTTP request becomes a Citrus HTTP message.
     */
    @Test
    public void shouldMapRequestToCitrusMessage() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/orders/42?type=gold&id=7",
                Map.of("X-Trace", "abc", "X-Empty", ""), new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(200);

        HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD)).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/orders/42");
        assertThat(request.getUri()).isEqualTo("/orders/42");
        assertThat(request.getContextPath()).isEqualTo("");
        assertThat(request.getHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS)).isEqualTo("type=gold,id=7");
        assertThat(request.getHeader(HttpMessageHeaders.HTTP_VERSION)).isEqualTo("HTTP/1.1");
        assertThat(request.getHeader("X-Trace")).isEqualTo("abc");
        assertThat(request.getHeader("X-Empty")).isEqualTo("");
    }

    /**
     * HSRV-002: An HTTP request becomes a Citrus HTTP message.
     */
    @Test
    public void shouldMapEncodedPathAndQueryParameters() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET",
                "/hello%20world;key=a,b?key=a%20b&key=c&flag", new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(200);

        HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPath()).isEqualTo("/hello world");
        assertThat(request.getUri()).isEqualTo("/hello world");
        assertThat(request.getContextPath()).isEqualTo("");
        assertThat(request.getHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS)).isEqualTo("key=a%20b,key=c,flag");
    }

    /**
     * HSRV-002: An HTTP request becomes a Citrus HTTP message.
     * <p>
     * A request path with duplicate slashes is ambiguous. Jetty rejects such a path with
     * {@code 400 Bad Request} before the request reaches Citrus, Vert.x normalizes the path
     * and delivers it with the duplicate slashes collapsed.
     */
    @Test
    public void shouldHandleDuplicateSlashesInRequestPath() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/orders//42", new byte[0]);

        if (isDuplicateSlashRejected()) {
            assertThat(response.getStatusCode()).isEqualTo(400);
            assertThat(getEndpointAdapter().hasReceivedRequests()).isFalse();
        } else {
            assertThat(response.getStatusCode()).isEqualTo(200);

            HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getPath()).isEqualTo("/orders/42");
        }
    }

    /**
     * HSRV-003: Standard request methods reach Citrus.
     */
    @Test
    public void shouldDeliverStandardRequestMethods() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        for (String method : List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")) {
            ContractClient.Response response = ContractClient.send(server.getPort(), method, "/methods", Map.of(), new byte[0]);

            assertThat(response.getStatusCode()).isEqualTo(200);

            HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD)).isEqualTo(method);
        }
    }

    /**
     * HSRV-004: TRACE is answered by the server.
     */
    @Test
    public void shouldAnswerTraceRequest() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "TRACE", "/trace", new byte[0]);

        if (isTraceAnsweredByServer()) {
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getHeader("Content-Type")).contains("message/http");
            assertThat(response.getBodyAsString()).contains("TRACE /trace HTTP/1.1");
            assertThat(getEndpointAdapter().hasReceivedRequests()).isFalse();
        } else {
            assertThat(response.getStatusCode()).isEqualTo(200);

            HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD)).isEqualTo("TRACE");
        }
    }

    /**
     * HSRV-005: The test's reply becomes the HTTP response.
     */
    @Test
    public void shouldWriteExplicitReplyStatusHeadersAndBody() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        getEndpointAdapter().setReplyProvider(request -> {
            HttpMessage reply = new HttpMessage("created");
            reply.status(HttpStatus.CREATED);
            reply.header("Location", "/orders/42");
            return reply;
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/orders/42", new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(201);
        assertThat(response.getHeader("Location")).isEqualTo("/orders/42");
        assertThat(response.getBodyAsString()).isEqualTo("created");
    }

    /**
     * HSRV-005: The test's reply becomes the HTTP response.
     */
    @Test
    public void shouldUseDefaultStatusWhenReplyHasNoStatus() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
            builder.defaultStatus(HttpStatus.ACCEPTED);
        });

        getEndpointAdapter().setReplyProvider(request -> new HttpMessage("accepted"));

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/orders/42", new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(202);
        assertThat(response.getBodyAsString()).isEqualTo("accepted");
    }

    /**
     * HSRV-006: A missing reply yields an empty default-status response.
     */
    @Test
    public void shouldAnswerWithEmptyBodyWhenAdapterReturnsNoReply() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
            builder.defaultStatus(HttpStatus.ACCEPTED);
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/orders/42", new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(202);
        assertThat(response.getBody()).isEmpty();
    }

    /**
     * HSRV-006: A missing reply yields an empty default-status response.
     */
    @Test
    public void shouldAnswerWithEmptyBodyWhenReplyTimesOut() {
        AbstractHttpServer server = startServerWithDefaultAdapter(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
            builder.timeout(1000L);
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/orders/42", new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    /**
     * HSRV-007: HEAD responses carry no body.
     */
    @Test
    public void shouldNotSendBodyForHeadRequest() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        getEndpointAdapter().setReplyProvider(request -> {
            HttpMessage reply = new HttpMessage("ignored");
            reply.status(HttpStatus.ACCEPTED);
            return reply;
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "HEAD", "/orders", new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(202);
        assertThat(response.getBody()).isEmpty();
        assertThat(response.getHeader("Content-Length")).isEqualTo("7");
    }

    /**
     * HSRV-008: Binary media types keep their bytes.
     */
    @Test
    public void shouldKeepBinaryRequestBytes() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        byte[] payload = new byte[]{0, 1, 2, 13, 10, 127, -1, -2, 65, 66};

        ContractClient.Response response = ContractClient.send(server.getPort(), "POST", "/binary",
                Map.of("Content-Type", "application/octet-stream"), payload);

        assertThat(response.getStatusCode()).isEqualTo(200);

        HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPayload()).isInstanceOf(byte[].class);
        assertThat((byte[]) request.getPayload()).isEqualTo(payload);
    }

    /**
     * HSRV-008: Binary media types keep their bytes.
     */
    @Test
    public void shouldMapTextRequestBodyWithCharset() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        ContractClient.send(server.getPort(), "POST", "/text",
                Map.of("Content-Type", "text/plain;charset=ISO-8859-1"), "Grüße".getBytes(ISO_8859_1));

        HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPayload()).isInstanceOf(String.class);
        assertThat(request.getPayload()).isEqualTo("Grüße");

        ContractClient.send(server.getPort(), "POST", "/text",
                Map.of("Content-Type", "text/plain;charset=UTF-8"), "Grüße 世界".getBytes(StandardCharsets.UTF_8));

        request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPayload()).isInstanceOf(String.class);
        assertThat(request.getPayload()).isEqualTo("Grüße 世界");
    }

    /**
     * HSRV-009: Cookies are mapped when cookie handling is enabled.
     */
    @Test
    public void shouldMapRequestAndReplyCookies() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
            builder.handleCookies(true);
        });

        getEndpointAdapter().setReplyProvider(request -> {
            HttpMessage reply = new HttpMessage("ok");
            Cookie cookie = new Cookie("session", "abc");
            cookie.setPath("/");
            cookie.setMaxAge(60);
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            reply.cookie(cookie);
            return reply;
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/cookies",
                Map.of("Cookie", "request=xyz"), new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeaders("Set-Cookie")).anySatisfy(setCookie -> {
            assertThat(setCookie).contains("session=abc");
            assertThat(setCookie).contains("Path=/");
            assertThat(setCookie).contains("Max-Age=60");
            assertThat(setCookie).contains("Secure");
            assertThat(setCookie).contains("HttpOnly");
        });

        HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getCookies()).hasSize(1);
        assertThat(request.getCookies().get(0).getName()).isEqualTo("request");
        assertThat(request.getCookies().get(0).getValue()).isEqualTo("xyz");
    }

    /**
     * HSRV-010: Matrix parameters are removed from the request path by default.
     */
    @Test
    public void shouldRemoveMatrixParametersFromRequestPath() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/orders;version=2/42", new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(200);

        HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPath()).isEqualTo("/orders/42");
    }

    /**
     * HSRV-018: The server can be started, stopped and restarted.
     */
    @Test
    public void shouldRestartServerOnSamePort() {
        int port = SocketUtils.findAvailableTcpPort();
        AbstractHttpServer server = startServer(builder -> {
            builder.port(port);
            builder.contextPath("/");
        });

        getEndpointAdapter().setReplyProvider(request -> new HttpMessage("ok"));

        ContractClient.Response response = ContractClient.send(port, "GET", "/orders", new byte[0]);
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBodyAsString()).isEqualTo("ok");

        server.stop();

        getEndpointAdapter().setReplyProvider(request -> new HttpMessage("restarted"));
        server.start();

        response = ContractClient.send(port, "GET", "/orders", new byte[0]);
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBodyAsString()).isEqualTo("restarted");
    }

    /**
     * HSRV-018: The server can be started, stopped and restarted.
     */
    @Test
    public void shouldFailToStartWhenPortIsInUse() {
        int port = SocketUtils.findAvailableTcpPort();
        startServer(builder -> {
            builder.port(port);
            builder.contextPath("/");
        });

        AbstractHttpServer conflictingServer = createServer(builder -> {
            builder.port(port);
            builder.contextPath("/");
        });

        runningServers.add(conflictingServer);

        assertThatThrownBy(() -> {
            conflictingServer.initialize();
            conflictingServer.start();
        }).isInstanceOf(CitrusRuntimeException.class);
    }

    /**
     * HSRV-019: Concurrent requests are answered independently.
     */
    @Test
    public void shouldAnswerPendingRequestsIndependently() throws Exception {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        Map<String, BlockingQueue<Message>> replyQueues = new ConcurrentHashMap<>();
        replyQueues.put("A", new LinkedBlockingQueue<>());
        replyQueues.put("B", new LinkedBlockingQueue<>());

        getEndpointAdapter().setReplyProvider(request -> {
            String requestId = String.valueOf(request.getHeader("X-Request-Id"));
            try {
                return replyQueues.get(requestId).take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        });

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<ContractClient.Response> responseA = executor.submit(
                    () -> ContractClient.send(server.getPort(), "GET", "/a", Map.of("X-Request-Id", "A"), new byte[0]));
            Future<ContractClient.Response> responseB = executor.submit(
                    () -> ContractClient.send(server.getPort(), "GET", "/b", Map.of("X-Request-Id", "B"), new byte[0]));

            HttpMessage requestA = getEndpointAdapter().takeRequest(10, TimeUnit.SECONDS);
            HttpMessage requestB = getEndpointAdapter().takeRequest(10, TimeUnit.SECONDS);

            Map<String, HttpMessage> requestsById = new LinkedHashMap<>();
            requestsById.put(String.valueOf(requestA.getHeader("X-Request-Id")), requestA);
            requestsById.put(String.valueOf(requestB.getHeader("X-Request-Id")), requestB);

            assertThat(requestsById.get("A").getPath()).isEqualTo("/a");
            assertThat(requestsById.get("B").getPath()).isEqualTo("/b");

            replyQueues.get("B").add(new HttpMessage("reply for B"));
            replyQueues.get("A").add(new HttpMessage("reply for A"));

            assertThat(responseB.get(10, TimeUnit.SECONDS).getBodyAsString()).isEqualTo("reply for B");
            assertThat(responseA.get(10, TimeUnit.SECONDS).getBodyAsString()).isEqualTo("reply for A");
        } finally {
            executor.shutdownNow();
        }
    }
}
