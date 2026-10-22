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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import jakarta.servlet.http.Cookie;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.http.server.AbstractCommonHttpServerBuilder;
import org.citrusframework.http.server.AbstractHttpServer;
import org.citrusframework.message.Message;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.spi.Resource;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.spi.Resources.create;

/**
 * Shared contract test suite for all HTTP server implementations. The suite verifies the documented
 * behavior of the Citrus HTTP server on the wire level. Implementations extend this suite and only
 * provide the server under test.
 */
public abstract class AbstractHttpServerContractIT extends TestNGCitrusSpringSupport {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHttpServerContractIT.class);

    /** Path spec protected by basic authentication */
    protected static final String SECURED_PATH = "/secured/*";

    /** Realm used by the basic authentication contract cases */
    protected static final String AUTH_REALM = "citrus-realm";

    /** User holding the required role */
    protected static final String AUTH_USER = "citrus";

    /** Password of the user holding the required role */
    protected static final String AUTH_PASSWORD = "secr3t";

    /** Role required to access the secured path */
    protected static final String AUTH_ROLE = "citrus";

    /** User without any role */
    protected static final String AUTH_USER_WITHOUT_ROLE = "guest";

    /** Password of the user without any role */
    protected static final String AUTH_PASSWORD_WITHOUT_ROLE = "guest";

    /** Password of the test key stores */
    protected static final String KEY_STORE_PASSWORD = "secret";

    /** Key store in JKS format */
    protected static final Resource JKS_KEY_STORE = create("classpath:org/citrusframework/http/integration/http-server.jks");

    /** Key store in PKCS12 format */
    protected static final Resource PKCS12_KEY_STORE = create("classpath:org/citrusframework/http/server/contract/http-server.p12");

    /** Servers started by the current test */
    private final List<AbstractHttpServer> runningServers = new ArrayList<>();

    /** The server started last */
    private AbstractHttpServer lastServer;

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

    /**
     * Enables basic authentication for {@link #SECURED_PATH} on the server under test. The server must
     * know the contract users {@link #AUTH_USER} and {@link #AUTH_USER_WITHOUT_ROLE}.
     *
     * @param server the server under test.
     */
    protected abstract void configureAuthentication(AbstractHttpServer server);

    /**
     * Adds the tracing interceptor reporting the exchanged messages to the given message listeners.
     *
     * @param server the server under test.
     * @param messageListeners the message listeners to report to.
     */
    protected abstract void configureTracing(AbstractHttpServer server, MessageListeners messageListeners);

    /**
     * Optional server features that are not supported by every server implementation.
     */
    public enum Feature {
        GZIP,
        AUTHENTICATION,
        TLS,
        TRACING,
        INTERCEPTORS
    }

    /**
     * Checks if the given feature is supported by this server implementation.
     *
     * @param feature the feature to check.
     * @return true if the feature is supported.
     */
    protected boolean isSupported(Feature feature) {
        return true;
    }

    /**
     * Skips the test when the given feature is not supported by this server implementation.
     *
     * @param feature the feature required by the test.
     */
    protected void assumeSupported(Feature feature) {
        if (!isSupported(feature)) {
            throw new SkipException("Feature not supported by this server implementation: " + feature);
        }
    }

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
        return startServer(options, server -> {
        });
    }

    /**
     * Starts the server under test with a recording endpoint adapter attached.
     *
     * @param options the options to apply to the server builder.
     * @param serverConfigurer configures the created server before it is started.
     * @return the started server.
     */
    protected AbstractHttpServer startServer(Consumer<AbstractCommonHttpServerBuilder<?, ?>> options, Consumer<AbstractHttpServer> serverConfigurer) {
        endpointAdapter = new ContractEndpointAdapter();

        AbstractHttpServer server = createServer(builder -> {
            options.accept(builder);
            builder.endpointAdapter(endpointAdapter);
        });

        serverConfigurer.accept(server);
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

    /**
     * Starts the server under test with basic authentication enabled for {@link #SECURED_PATH}.
     *
     * @return the started server.
     */
    protected AbstractHttpServer startSecuredServer() {
        return startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        }, this::configureAuthentication);
    }

    /**
     * Starts the server under test with a TLS connection using the given key store.
     *
     * @param securePort the port to listen on with TLS.
     * @param keyStore the key store holding the server certificate.
     * @return the started server.
     */
    protected AbstractHttpServer startSecuredServer(int securePort, Resource keyStore) {
        return startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
            builder.secured(securePort, HttpSecureConnection.ssl().keyStore(keyStore, KEY_STORE_PASSWORD));
        });
    }

    /**
     * Starts the server under test with a tracing interceptor reporting the exchanged messages to the
     * returned message listener.
     *
     * @param options the options to apply to the server builder.
     * @return the message listener receiving the traced messages.
     */
    protected ContractMessageListener startServerWithTracing(Consumer<AbstractCommonHttpServerBuilder<?, ?>> options) {
        ContractMessageListener messageListener = new ContractMessageListener();

        MessageListeners messageListeners = new MessageListeners();
        messageListeners.addMessageListener(messageListener);

        startServer(options, server -> configureTracing(server, messageListeners));
        return messageListener;
    }

    private void startServer(AbstractHttpServer server) {
        runningServers.add(server);
        lastServer = server;
        server.initialize();
        server.start();
    }

    /**
     * Gets the server that has been started last.
     *
     * @return the server under test.
     */
    protected AbstractHttpServer getServer() {
        return lastServer;
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
     * HSRV-023: All cookies of the reply are sent to the client. Headers that occur multiple
     * times in the reply must not be reduced to their last value.
     */
    @Test
    public void shouldSendAllReplyCookies() {
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
            builder.handleCookies(true);
        });

        getEndpointAdapter().setReplyProvider(request -> {
            HttpMessage reply = new HttpMessage("ok");
            reply.cookie(new Cookie("session", "abc"));
            reply.cookie(new Cookie("csrf", "xyz"));
            return reply;
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/cookies", new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeaders("Set-Cookie")).hasSize(2);
        assertThat(response.getHeaders("Set-Cookie")).anySatisfy(setCookie -> assertThat(setCookie).contains("session=abc"));
        assertThat(response.getHeaders("Set-Cookie")).anySatisfy(setCookie -> assertThat(setCookie).contains("csrf=xyz"));
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

    /**
     * HSRV-011: Gzip bodies are decoded and responses compressed.
     */
    @Test
    public void shouldDecodeGzipRequestBody() {
        assumeSupported(Feature.GZIP);
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        getEndpointAdapter().setReplyProvider(request -> new HttpMessage("ok"));

        ContractClient.Response response = ContractClient.send(server.getPort(), "POST", "/gzip",
                Map.of("Content-Type", "text/plain", "Content-Encoding", "gzip"), gzip("Hello gzip".getBytes(StandardCharsets.UTF_8)));

        assertThat(response.getStatusCode()).isEqualTo(200);

        HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPayload()).isEqualTo("Hello gzip");
        assertThat(request.getHeader("Content-Encoding")).isEqualTo("gzip");
    }

    /**
     * HSRV-011: Gzip bodies are decoded and responses compressed.
     */
    @Test
    public void shouldCompressResponseWhenClientAcceptsGzip() {
        assumeSupported(Feature.GZIP);
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        getEndpointAdapter().setReplyProvider(request -> new HttpMessage("Hello gzip response"));

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/gzip",
                Map.of("Accept-Encoding", "gzip"), new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeader("Content-Encoding")).isEqualTo("gzip");
        assertThat(gunzip(response.getBody())).isEqualTo("Hello gzip response".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * HSRV-011: Gzip bodies are decoded and responses compressed.
     * <p>
     * The server applies no other response content coding than gzip.
     */
    @Test
    public void shouldNotCompressResponseForOtherContentCodings() {
        assumeSupported(Feature.GZIP);
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        getEndpointAdapter().setReplyProvider(request -> new HttpMessage("Hello plain response"));

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/gzip",
                Map.of("Accept-Encoding", "deflate"), new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeader("Content-Encoding")).isNull();
        assertThat(response.getBodyAsString()).isEqualTo("Hello plain response");
    }

    /**
     * HSRV-011: Gzip bodies are decoded and responses compressed.
     * <p>
     * A 204 response is not compressed.
     */
    @Test
    public void shouldNotCompressNoContentResponse() {
        assumeSupported(Feature.GZIP);
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        getEndpointAdapter().setReplyProvider(request -> {
            HttpMessage reply = new HttpMessage("");
            reply.status(HttpStatus.NO_CONTENT);
            return reply;
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/gzip",
                Map.of("Accept-Encoding", "gzip"), new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(204);
        assertThat(response.getHeader("Content-Encoding")).isNull();
        assertThat(response.getBody()).isEmpty();
    }

    /**
     * HSRV-011: Gzip bodies are decoded and responses compressed.
     * <p>
     * A request body that is marked as gzip encoded but does not hold a valid gzip stream fails the
     * request before it reaches Citrus.
     */
    @Test
    public void shouldFailOnCorruptGzipRequestBody() {
        assumeSupported(Feature.GZIP);
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "POST", "/gzip",
                Map.of("Content-Type", "text/plain", "Content-Encoding", "gzip"), "no gzip at all".getBytes(StandardCharsets.UTF_8));

        assertThat(response.getStatusCode()).isEqualTo(500);
        assertThat(getEndpointAdapter().hasReceivedRequests()).isFalse();
    }

    /**
     * HSRV-012: Basic authentication protects a path spec.
     */
    @Test
    public void shouldRejectRequestWithoutCredentials() {
        assumeSupported(Feature.AUTHENTICATION);
        AbstractHttpServer server = startSecuredServer();

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/secured/data", new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getHeader("WWW-Authenticate")).contains("Basic realm=\"" + AUTH_REALM + "\"");
        assertThat(getEndpointAdapter().hasReceivedRequests()).isFalse();
    }

    /**
     * HSRV-012: Basic authentication protects a path spec.
     */
    @Test
    public void shouldRejectRequestWithWrongCredentials() {
        assumeSupported(Feature.AUTHENTICATION);
        AbstractHttpServer server = startSecuredServer();

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/secured/data",
                Map.of("Authorization", basicAuthorization(AUTH_USER, "wrong")), new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getHeader("WWW-Authenticate")).contains("Basic realm=\"" + AUTH_REALM + "\"");
        assertThat(getEndpointAdapter().hasReceivedRequests()).isFalse();
    }

    /**
     * HSRV-012: Basic authentication protects a path spec.
     */
    @Test
    public void shouldDeliverRequestWithValidCredentials() {
        assumeSupported(Feature.AUTHENTICATION);
        AbstractHttpServer server = startSecuredServer();

        getEndpointAdapter().setReplyProvider(request -> new HttpMessage("secured"));

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/secured/data",
                Map.of("Authorization", basicAuthorization(AUTH_USER, AUTH_PASSWORD)), new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBodyAsString()).isEqualTo("secured");

        HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPath()).isEqualTo("/secured/data");
    }

    /**
     * HSRV-012: Basic authentication protects a path spec.
     * <p>
     * Requests to paths that do not match the secured path spec are not authenticated.
     */
    @Test
    public void shouldLeaveUnmatchedPathsUnauthenticated() {
        assumeSupported(Feature.AUTHENTICATION);
        AbstractHttpServer server = startSecuredServer();

        getEndpointAdapter().setReplyProvider(request -> new HttpMessage("open"));

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/other", new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBodyAsString()).isEqualTo("open");

        HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPath()).isEqualTo("/other");
    }

    /**
     * HSRV-012: Basic authentication protects a path spec.
     * <p>
     * An authenticated user that does not hold the required role is forbidden.
     */
    @Test
    public void shouldAnswerForbiddenForUserWithoutRole() {
        assumeSupported(Feature.AUTHENTICATION);
        AbstractHttpServer server = startSecuredServer();

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/secured/data",
                Map.of("Authorization", basicAuthorization(AUTH_USER_WITHOUT_ROLE, AUTH_PASSWORD_WITHOUT_ROLE)), new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(403);
        assertThat(getEndpointAdapter().hasReceivedRequests()).isFalse();
    }

    /**
     * HSRV-013: A secured server accepts HTTPS on the secure port.
     */
    @Test
    public void shouldAcceptHttpsOnSecurePortWithJks() {
        assumeSupported(Feature.TLS);
        int securePort = findAvailablePort(-1);

        AbstractHttpServer server = startSecuredServer(securePort, JKS_KEY_STORE);

        getEndpointAdapter().setReplyProvider(request -> new HttpMessage("secure"));

        ContractClient.Response response = ContractClient.sendSecure(securePort, "GET", "/orders", Map.of(), new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBodyAsString()).isEqualTo("secure");

        HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPath()).isEqualTo("/orders");
    }

    /**
     * HSRV-013: A secured server accepts HTTPS on the secure port.
     */
    @Test
    public void shouldAcceptHttpsOnSecurePortWithPkcs12() {
        assumeSupported(Feature.TLS);
        int securePort = findAvailablePort(-1);

        AbstractHttpServer server = startSecuredServer(securePort, PKCS12_KEY_STORE);

        getEndpointAdapter().setReplyProvider(request -> new HttpMessage("secure"));

        ContractClient.Response response = ContractClient.sendSecure(securePort, "GET", "/orders", Map.of(), new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBodyAsString()).isEqualTo("secure");

        HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPath()).isEqualTo("/orders");
    }

    /**
     * HSRV-013: A secured server accepts HTTPS on the secure port.
     * <p>
     * A secured server does not accept plain HTTP on the plain port.
     */
    @Test
    public void shouldNotListenOnPlainPortWhenSecured() {
        assumeSupported(Feature.TLS);
        int securePort = findAvailablePort(-1);

        AbstractHttpServer server = startSecuredServer(securePort, JKS_KEY_STORE);

        assertThatThrownBy(() -> ContractClient.send(server.getPort(), "GET", "/orders", new byte[0]))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * HSRV-015: Exchanged messages are logged and can be reported to message listeners.
     */
    @Test
    public void shouldReportExchangeToMessageListener() {
        assumeSupported(Feature.TRACING);
        ContractMessageListener messageListener = startServerWithTracing(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
        });

        getEndpointAdapter().setReplyProvider(request -> new HttpMessage("ok"));

        ContractClient.Response response = ContractClient.send(getServer().getPort(), "POST", "/orders",
                Map.of("Content-Type", "text/plain"), "Hello".getBytes(StandardCharsets.UTF_8));

        assertThat(response.getStatusCode()).isEqualTo(200);

        Message inboundMessage = messageListener.takeInboundMessage(5, TimeUnit.SECONDS);
        assertThat(inboundMessage.getPayload().toString()).contains("POST /orders");

        Message outboundMessage = messageListener.takeOutboundMessage(5, TimeUnit.SECONDS);
        assertThat(outboundMessage.getPayload().toString()).contains("HTTP/1.1 200");
    }

    /**
     * HSRV-022: Citrus server interceptors run around request handling.
     */
    @Test
    public void shouldShortCircuitRequestWithInterceptor() {
        assumeSupported(Feature.INTERCEPTORS);
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
            builder.interceptor((request, chain) -> new HttpMessage("short circuit"));
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/orders", new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBodyAsString()).isEqualTo("short circuit");
        assertThat(getEndpointAdapter().hasReceivedRequests()).isFalse();
    }

    /**
     * HSRV-022: Citrus server interceptors run around request handling.
     */
    @Test
    public void shouldDecorateRequestWithInterceptor() {
        assumeSupported(Feature.INTERCEPTORS);
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
            builder.interceptor((request, chain) -> chain.proceed(request.header("X-Intercepted", "true")));
        });

        getEndpointAdapter().setReplyProvider(request -> new HttpMessage("ok"));

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/orders", new byte[0]);
        assertThat(response.getStatusCode()).isEqualTo(200);

        HttpMessage request = getEndpointAdapter().takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getHeader("X-Intercepted")).isEqualTo("true");
    }

    /**
     * HSRV-022: Citrus server interceptors run around request handling.
     */
    @Test
    public void shouldReplaceReplyWithInterceptor() {
        assumeSupported(Feature.INTERCEPTORS);
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
            builder.interceptor((request, chain) -> {
                chain.proceed(request);
                return new HttpMessage("replaced").status(HttpStatus.ACCEPTED);
            });
        });

        getEndpointAdapter().setReplyProvider(request -> new HttpMessage("original"));

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/orders", new byte[0]);
        assertThat(response.getStatusCode()).isEqualTo(202);
        assertThat(response.getBodyAsString()).isEqualTo("replaced");
    }

    /**
     * HSRV-022: Citrus server interceptors run around request handling.
     */
    @Test
    public void shouldAnswerWithServerErrorWhenInterceptorFails() {
        assumeSupported(Feature.INTERCEPTORS);
        AbstractHttpServer server = startServer(builder -> {
            builder.port(SocketUtils.findAvailableTcpPort());
            builder.contextPath("/");
            builder.interceptor((request, chain) -> {
                throw new IllegalStateException("Interceptor failed");
            });
        });

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/orders", new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(500);
        assertThat(response.getBodyAsString()).isEmpty();
        assertThat(getEndpointAdapter().hasReceivedRequests()).isFalse();
    }

    private static String basicAuthorization(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(ISO_8859_1));
    }

    private static int findAvailablePort(int excludedPort) {
        int port = SocketUtils.findAvailableTcpPort();
        while (port == excludedPort) {
            port = SocketUtils.findAvailableTcpPort();
        }
        return port;
    }

    private static byte[] gzip(byte[] content) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
            gzipOutputStream.write(content);
            gzipOutputStream.finish();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to gzip content", e);
        }
    }

    private static byte[] gunzip(byte[] content) {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(content))) {
            return gzipInputStream.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to gunzip content", e);
        }
    }
}
