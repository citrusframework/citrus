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

package org.citrusframework.http.vertx;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import io.vertx.core.Vertx;
import io.vertx.core.Context;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;
import org.citrusframework.base.server.AbstractServer;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectMessageHeaders;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.vertx.VertxHttpMessageMapper.RequestSnapshot;
import org.citrusframework.http.vertx.VertxHttpMessageMapper.ResponsePlan;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;

/**
 * Test-only HTTP server prototype backed by Vert.x (evaluation Approach C).
 *
 * <p>Never used in production code paths: the class lives in test sources and exists solely to
 * gather evidence for the Jetty replacement decision. Wire behavior mirrors
 * {@code HttpMessageController} via {@link VertxHttpMessageMapper}.
 *
 * <p>Threading contract: the Vert.x event loop only snapshots the request and writes the
 * response. {@link EndpointAdapter#handleMessage(Message)} always runs on the endpoint-owned
 * {@link VertxExchangeExecutor}, responses are written back on the Vert.x context, and a
 * terminal guard guarantees at most one response per request.
 */
public class VertxHttpServer extends AbstractServer {

    private final VertxHttpServerOptions.Builder draft = VertxHttpServerOptions.builder();
    private final HttpEndpointConfiguration httpEndpointConfiguration = new HttpEndpointConfiguration();

    private volatile GenerationHandler currentGeneration;
    private final ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
    private volatile VertxExchangeExecutor exchangeExecutor;
    private volatile VertxHttpServerOptions activeOptions;
    private volatile int actualPort = -1;
    private volatile long generation;
    private volatile boolean prototypeAdapter = true;
    private int responseCacheSize = org.citrusframework.http.server.HttpServerSettings.responseCacheSize();
    private List<VertxMessageBodyConverter<?>> messageBodyConverters = List.of();
    private List<VertxHttpFilter> filters = List.of();
    private List<VertxHttpHandlerMapping> handlers = List.of();
    private List<VertxHttpInterceptor> vertxInterceptors = List.of();
    private VertxHttpTls tls;
    private VertxHttpSecurity security;
    private String contextPath = "/";
    private VertxBasicAuthentication basicAuthentication;
    private String servletMappingPath = "/*";

    private final AtomicLong exchangeSequence = new AtomicLong();

    public VertxHttpServer() {
        setName("vertxHttpServerPrototype");
    }

    // option accessors (mutate the draft; a snapshot is taken per generation at startup)

    /** Null restores plain HTTP on the next start. */
    public void setTls(VertxHttpTls tls) { this.tls = tls; }

    public void setHost(String host) {
        draft.host(host);
    }

    public String getHost() {
        return draft.build().getHost();
    }

    public void setPort(int port) {
        draft.port(port);
    }

    public int getPort() {
        return draft.build().getPort();
    }

    public void setBasicAuthentication(String username, String password, String realm) {
        basicAuthentication = new VertxBasicAuthentication(username, password, realm);
    }

    private VertxHttpMessageExchange messageExchange = (request, adapter) -> adapter.handleMessage(request);
    private BiConsumer<HttpServerRequest, Map<String, Object>> requestAttributes = (request, attributes) -> { };

    public void setMessageExchange(VertxHttpMessageExchange exchange) {
        this.messageExchange = java.util.Objects.requireNonNull(exchange);
    }

    /**
     * Stages per-request attributes on the event loop before dispatch. This is the Vert.x analog
     * of servlet container filters calling {@code HttpServletRequest.setAttribute()}: entries put
     * into the map are carried in the request snapshot and exposed as Citrus message headers
     * verbatim when {@code handleAttributeHeaders} is enabled, mirroring
     * {@code HttpMessageController}. The callback runs on the Vert.x event loop, must be
     * non-blocking, and like the message exchange is snapshotted at start.
     */
    public void setRequestAttributes(BiConsumer<HttpServerRequest, Map<String, Object>> requestAttributes) {
        this.requestAttributes = java.util.Objects.requireNonNull(requestAttributes);
    }

    private VertxWebSocketEndpoint webSocketEndpoint;

    public void setWebSocketEndpoint(VertxWebSocketEndpoint endpoint) { this.webSocketEndpoint = endpoint; }

    public void setSecurity(VertxHttpSecurity security) { this.security = security; }

    public void clearBasicAuthentication() {
        basicAuthentication = null;
    }

    public void setMessageBodyConverters(List<VertxMessageBodyConverter<?>> converters) {
        messageBodyConverters = List.copyOf(converters);
    }

    /** Registration changes take effect on the next start; callbacks must be thread-safe. */
    public void setFilters(List<VertxHttpFilter> filters) {
        this.filters = List.copyOf(filters);
    }

    public void setHandlers(List<VertxHttpHandlerMapping> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    /** Registration changes take effect on the next start; callbacks must be thread-safe. */
    @Override
    public void setInterceptors(List<Object> interceptors) {
        interceptors.forEach(interceptor -> {
            if (!(interceptor instanceof VertxHttpInterceptor)) {
                throw new IllegalArgumentException("Interceptors must implement VertxHttpInterceptor, got: "
                        + (interceptor == null ? "null" : interceptor.getClass().getName()));
            }
        });
        super.setInterceptors(interceptors);
        this.vertxInterceptors = List.copyOf(
                interceptors.stream().map(VertxHttpInterceptor.class::cast).toList());
    }

    public void setResponseCacheSize(int size) {
        if (size < 0) throw new IllegalArgumentException("Response cache size must not be negative");
        responseCacheSize = size;
    }

    public int getResponseCacheSize() { return responseCacheSize; }

    public Map<String, ResponsePlan> getCachedResponses() {
        GenerationHandler owner = currentGeneration;
        return owner == null ? Map.of() : owner.responseCache.snapshot();
    }

    public ResponsePlan getResponseCache(String exchangeId) {
        GenerationHandler owner = currentGeneration;
        return owner == null ? null : owner.responseCache.get(exchangeId);
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setServletMappingPath(String servletMappingPath) {
        this.servletMappingPath = servletMappingPath;
    }

    public String getServletMappingPath() {
        return servletMappingPath;
    }

    public void setDefaultStatusCode(int defaultStatusCode) {
        draft.defaultStatusCode(defaultStatusCode);
        httpEndpointConfiguration.setDefaultStatusCode(defaultStatusCode);
    }

    public void setReplyTimeoutMs(long replyTimeoutMs) {
        draft.replyTimeoutMs(replyTimeoutMs);
        super.setDefaultTimeout(replyTimeoutMs);
    }

    @Override
    public void setDefaultTimeout(long defaultTimeout) {
        super.setDefaultTimeout(defaultTimeout);
        draft.replyTimeoutMs(defaultTimeout);
    }

    public void setMaxBodySizeBytes(long maxBodySizeBytes) {
        draft.maxBodySizeBytes(maxBodySizeBytes);
    }

    public void setBodyReadTimeoutMs(long bodyReadTimeoutMs) {
        draft.bodyReadTimeoutMs(bodyReadTimeoutMs);
    }

    public void setExecutorThreads(int executorThreads) {
        draft.executorThreads(executorThreads);
    }

    public void setMaxConcurrentExchanges(int maxConcurrentExchanges) {
        draft.maxConcurrentExchanges(maxConcurrentExchanges);
    }

    public void setHandleCookies(boolean handleCookies) {
        draft.handleCookies(handleCookies);
        httpEndpointConfiguration.setHandleCookies(handleCookies);
    }

    public void setHandleAttributeHeaders(boolean handleAttributeHeaders) {
        draft.handleAttributeHeaders(handleAttributeHeaders);
        httpEndpointConfiguration.setHandleAttributeHeaders(handleAttributeHeaders);
    }

    public int getActualPort() {
        return actualPort;
    }

    public long getGeneration() {
        return generation;
    }

    public VertxHttpServerOptions getActiveOptions() {
        return activeOptions;
    }

    public HttpEndpointConfiguration getHttpEndpointConfiguration() {
        return httpEndpointConfiguration;
    }

    @Override
    public void setEndpointAdapter(EndpointAdapter endpointAdapter) {
        super.setEndpointAdapter(endpointAdapter);
        prototypeAdapter = false;
    }

    public void setShutdownTimeoutMs(long timeoutMs) {
        draft.shutdownTimeoutMs(timeoutMs);
    }

    /** Failures are retained for the evaluation harness, never included in HTTP replies. */
    public List<Throwable> getFailures() {
        return List.copyOf(failures);
    }

    @Override
    public synchronized void start() {
        if (isRunning()) {
            return;
        }
        if (currentGeneration != null) {
            shutdown();
        }
        super.start();
    }

    @Override
    public synchronized void stop() {
        try {
            if (isRunning()) {
                super.stop();
            } else if (currentGeneration != null) {
                shutdown();
            }
        } finally {
            setRunning(false);
        }
    }

    @Override
    public synchronized void destroy() {
        stop();
    }

    @Override
    protected void startup() {
        if (security != null && basicAuthentication != null)
            throw new IllegalArgumentException("Use either policy security or single-user Basic authentication");
        VertxHttpRoute route = new VertxHttpRoute(contextPath, servletMappingPath);
        VertxResponseCache responseCache = new VertxResponseCache(responseCacheSize);
        // The exposed HTTP configuration is the draft; running exchanges use a private copy.
        VertxHttpServerOptions options = draft
                .charset(httpEndpointConfiguration.getCharset())
                .contentType(httpEndpointConfiguration.getContentType())
                .binaryMediaTypes(httpEndpointConfiguration.getBinaryMediaTypes())
                .defaultStatusCode(httpEndpointConfiguration.getDefaultStatusCode())
                .handleCookies(httpEndpointConfiguration.isHandleCookies())
                .handleAttributeHeaders(httpEndpointConfiguration.isHandleAttributeHeaders())
                .removeSemicolonPathContent(httpEndpointConfiguration.isRemoveSemicolonPathContent())
                .build();
        if (getEndpointAdapter() == null || prototypeAdapter) {
            DirectSyncEndpointConfiguration configuration = new DirectSyncEndpointConfiguration();
            configuration.setQueue(new DefaultMessageQueue(getName() + DEFAULT_CHANNEL_ID_SUFFIX));
            configuration.setTimeout(options.getReplyTimeoutMs());
            DirectEndpointAdapter adapter = new DirectEndpointAdapter(configuration);
            adapter.getEndpoint().setName(getName());
            if (getReferenceResolver() != null
                    && !getReferenceResolver().resolveAll(TestContextFactory.class).isEmpty()) {
                adapter.setTestContextFactory(getReferenceResolver().resolve(TestContextFactory.class));
            }
            super.setEndpointAdapter(adapter);
            prototypeAdapter = true;
        }
        HttpServerOptions listenerOptions = new HttpServerOptions().setHttp2ClearTextEnabled(false);
        if (tls != null) tls.apply(listenerOptions);
        if (webSocketEndpoint != null) listenerOptions.setMaxWebSocketFrameSize(webSocketEndpoint.maxMessageBytes())
                .setMaxWebSocketMessageSize(webSocketEndpoint.maxMessageBytes());
        VertxWebSocketEndpoint.State sockets = webSocketEndpoint == null ? null : webSocketEndpoint.open();
        GenerationHandler candidate = new GenerationHandler(options, generation + 1,
                getEndpointAdapter(), snapshotConfiguration(), prototypeAdapter, route, basicAuthentication, responseCache, messageBodyConverters, new VertxHttpPipeline(filters, handlers), listenerOptions, security, sockets, messageExchange, requestAttributes, vertxInterceptors);
        currentGeneration = candidate;
        exchangeExecutor = candidate.executor;
        try {
            candidate.server.requestHandler(candidate::handle).listen(options.getPort(), options.getHost())
                    .toCompletionStage().toCompletableFuture().get(options.getStartupTimeoutMs(), TimeUnit.MILLISECONDS);
            activeOptions = options;
            actualPort = candidate.server.actualPort();
            generation = candidate.generation;
        } catch (Exception e) {
            try {
                shutdown();
            } catch (RuntimeException cleanup) {
                e.addSuppressed(cleanup);
            }
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new CitrusRuntimeException("Failed to bind Vert.x HTTP prototype", e);
        }
    }

    private HttpEndpointConfiguration snapshotConfiguration() {
        HttpEndpointConfiguration copy = new HttpEndpointConfiguration();
        copy.setCharset(httpEndpointConfiguration.getCharset());
        copy.setContentType(httpEndpointConfiguration.getContentType());
        copy.setBinaryMediaTypes(List.copyOf(httpEndpointConfiguration.getBinaryMediaTypes()));
        copy.setDefaultStatusCode(httpEndpointConfiguration.getDefaultStatusCode());
        copy.setHandleCookies(httpEndpointConfiguration.isHandleCookies());
        copy.setHandleAttributeHeaders(httpEndpointConfiguration.isHandleAttributeHeaders());
        copy.setRemoveSemicolonPathContent(httpEndpointConfiguration.isRemoveSemicolonPathContent());
        copy.setMessageConverter(httpEndpointConfiguration.getMessageConverter());
        copy.setHeaderMapper(httpEndpointConfiguration.getHeaderMapper());
        return copy;
    }

    @Override
    protected void shutdown() {
        GenerationHandler owner = currentGeneration;
        if (owner == null) return;
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(owner.options.getShutdownTimeoutMs());
        if (owner.closed.compareAndSet(false, true)) {
            owner.responseCache.close();
            if (owner.sockets != null) owner.sockets.close();
            owner.exchanges.values().forEach(VertxExchange::abandon);
            owner.listenerClosed = owner.server.close().toCompletionStage().toCompletableFuture();
            owner.runtimeClosed = owner.vertx.close().toCompletionStage().toCompletableFuture();
        }
        Throwable failure = null;
        try {
            if (!owner.executor.shutdown(remainingMillis(deadline))) {
                throw new TimeoutException("Owned exchange workers did not terminate within shutdown budget");
            }
        } catch (Exception e) {
            failure = e;
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
        }
        for (CompletableFuture<Void> close : List.of(owner.listenerClosed, owner.runtimeClosed)) {
            try {
                close.get(remainingMillis(deadline), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                if (failure == null) failure = e;
                else failure.addSuppressed(e);
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            }
        }
        actualPort = -1;
        if (failure != null) {
            failures.add(failure);
            throw new CitrusRuntimeException("Vert.x prototype shutdown failed", failure);
        }
        currentGeneration = null;
        exchangeExecutor = null;
    }

    private static long remainingMillis(long deadline) {
        return Math.max(1, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime()));
    }

    private static ResponsePlan minimalPlan(int status, VertxHttpServerOptions options) {
        return new ResponsePlan(status, Map.of("Content-Type", List.of(options.getContentType()),
                "Content-Length", List.of("0")), new byte[0], false);
    }

    private final class GenerationHandler {
        private final Vertx vertx = Vertx.vertx();
        // The evaluated contract is HTTP/1.1, matching the existing Jetty fixture.
        private final io.vertx.core.http.HttpServer server;
        private final VertxHttpServerOptions options;
        private final long generation;
        private final EndpointAdapter adapter;
        private final HttpEndpointConfiguration configuration;
        private final boolean defaultAdapter;
        private final VertxHttpRoute route;
        private final VertxResponseCache responseCache;
        private final List<VertxMessageBodyConverter<?>> converters;
        private final VertxHttpPipeline pipeline;
        private final VertxHttpMessageExchange messageExchange;
        private final BiConsumer<HttpServerRequest, Map<String, Object>> requestAttributes;
        private final List<VertxHttpInterceptor> interceptors;
        private final VertxBasicAuthentication authentication;
        private final VertxHttpSecurity security;
        private final VertxWebSocketEndpoint.State sockets;
        private final VertxExchangeExecutor executor;
        private final AtomicBoolean closed = new AtomicBoolean();
        private final Map<String, VertxExchange> exchanges = new ConcurrentHashMap<>();
        private CompletableFuture<Void> listenerClosed;
        private CompletableFuture<Void> runtimeClosed;

        private GenerationHandler(VertxHttpServerOptions options, long generation, EndpointAdapter adapter,
                                  HttpEndpointConfiguration configuration, boolean defaultAdapter, VertxHttpRoute route,
                                  VertxBasicAuthentication authentication, VertxResponseCache responseCache,
                                  List<VertxMessageBodyConverter<?>> converters, VertxHttpPipeline pipeline,
                                  HttpServerOptions listenerOptions, VertxHttpSecurity security, VertxWebSocketEndpoint.State sockets, VertxHttpMessageExchange messageExchange,
                                  BiConsumer<HttpServerRequest, Map<String, Object>> requestAttributes,
                                  List<VertxHttpInterceptor> interceptors) {
            this.interceptors = interceptors;
            this.requestAttributes = requestAttributes;
            this.messageExchange = messageExchange;
            this.sockets = sockets;
            this.security = security;
            this.server = vertx.createHttpServer(listenerOptions);
            this.pipeline = pipeline;
            this.converters = List.copyOf(converters);
            this.responseCache = responseCache;
            this.authentication = authentication;
            this.route = route;
            this.options = options;
            this.generation = generation;
            this.adapter = adapter;
            this.configuration = configuration;
            this.defaultAdapter = defaultAdapter;
            this.executor = new VertxExchangeExecutor(getName() + "-exchange-g" + generation,
                    options.getExecutorThreads(), options.getMaxConcurrentExchanges());
        }

        void handle(HttpServerRequest request) {
            int routeStatus = route.status(request.path());
            if (routeStatus != 200) {
                request.pause();
                HttpServerResponse response = request.response().setStatusCode(routeStatus)
                        .putHeader("Content-Length", "0").putHeader("Connection", "close");
                if (routeStatus == 301) {
                    response.putHeader("Location", route.contextPath() + "/"
                            + (request.query() == null ? "" : "?" + request.query()));
                }
                response.end().onComplete(ignored -> request.connection().close());
                return;
            }
            if (authentication != null && !authentication.accepts(request.headers().getAll("Authorization"))) {
                request.pause();
                request.response().setStatusCode(401).putHeader("WWW-Authenticate", authentication.challenge())
                        .putHeader("Content-Length", "0").putHeader("Connection", "close")
                        .end().onComplete(ignored -> request.connection().close());
                return;
            }
            if (closed.get() || !executor.tryAcquire()) {
                request.pause();
                request.response().setStatusCode(503).putHeader("Content-Length", "0")
                        .putHeader("Connection", "close").end().onComplete(ignored -> request.connection().close());
                return;
            }
            VertxExchange exchange = new VertxExchange(this, request);
            exchanges.put(exchange.id, exchange);
            request.response().closeHandler(ignored -> exchange.abandon());
            request.exceptionHandler(ignored -> exchange.abandon());
            if (closed.get()) {
                exchange.abandon();
                return;
            }
            exchange.timerId = vertx.setTimer(options.getBodyReadTimeoutMs(), ignored ->
                    exchange.respond(minimalPlan(408, options), true));
            if ("websocket".equalsIgnoreCase(request.getHeader("Upgrade"))) {
                request.pause();
                // Upgrade requests have no entity. Keep the request unread for Vert.x's handshake.
                if (request.method() != io.vertx.core.http.HttpMethod.GET
                        || request.headers().contains("Transfer-Encoding")
                        || request.headers().getAll("Content-Length").stream().anyMatch(value -> !value.equals("0"))) {
                    exchange.respond(minimalPlan(400, options), true); return;
                }
                dispatch(request, exchange);
                return;
            }
            request.handler(chunk -> {
                if (exchange.terminal.get()) return;
                if ((long) exchange.body.length() + chunk.length() > options.getMaxBodySizeBytes()) {
                    exchange.respond(minimalPlan(413, options), true);
                } else {
                    exchange.body.appendBuffer(chunk);
                }
            });
            request.endHandler(ignored -> {
                vertx.cancelTimer(exchange.timerId);
                if (exchange.terminal.get() || closed.get()) return;
                dispatch(request, exchange);
            });
        }

        private void dispatch(HttpServerRequest request, VertxExchange exchange) {
            Map<String, List<String>> headers = new LinkedHashMap<>();
            request.headers().forEach(entry -> headers.computeIfAbsent(entry.getKey(), key -> new ArrayList<>())
                    .add(entry.getValue()));
            Map<String, Object> attributes = new LinkedHashMap<>();
            try {
                requestAttributes.accept(request, attributes);
            } catch (Exception e) {
                failures.add(e);
                exchange.respond(minimalPlan(500, options), true);
                return;
            }
            RequestSnapshot snapshot = new RequestSnapshot(request.method().name(), request.uri(), request.path(),
                    request.query(), request.version() == HttpVersion.HTTP_1_0 ? "HTTP/1.0" : "HTTP/1.1",
                    headers, exchange.body.getBytes(), exchange.id, generation, attributes);
            exchange.body = null;
            exchange.workerFinished = false;
            try { executor.execute(() -> handleExchange(snapshot, exchange)); }
            catch (RejectedExecutionException e) {
                exchange.workerFinished = true;
                exchange.respond(minimalPlan(503, options), true);
                exchange.releaseIfFinished();
            }
        }

        private void handleExchange(RequestSnapshot snapshot, VertxExchange exchange) {
            if (!exchange.workerStarted.compareAndSet(false, true)) return;
            try {
                if (closed.get() || exchange.terminal.get()) return;
                VertxHttpSecurity.Decision decision = security == null ? new VertxHttpSecurity.Decision(200, null)
                        : security.authorize(snapshot.path(), snapshot.method(), snapshot.headers().entrySet().stream()
                                .filter(entry -> entry.getKey().equalsIgnoreCase("Authorization"))
                                .flatMap(entry -> entry.getValue().stream()).toList());
                if (decision.status() != 200) {
                    Map<String, List<String>> headers = new java.util.LinkedHashMap<>();
                    headers.put("Content-Length", List.of("0"));
                    if (decision.status() == 401) headers.put("WWW-Authenticate", List.of(security.challenge()));
                    exchange.respond(new ResponsePlan(decision.status(), headers, new byte[0], false), true);
                    return;
                }
                boolean upgrade = snapshot.headers().entrySet().stream()
                        .filter(entry -> entry.getKey().equalsIgnoreCase("Upgrade"))
                        .flatMap(entry -> entry.getValue().stream()).anyMatch(value -> value.equalsIgnoreCase("websocket"));
                if (upgrade) {
                    if (sockets == null || !sockets.matches(snapshot.path())) exchange.respond(minimalPlan(404, options), true);
                    else exchange.upgrade(decision.identity());
                    return;
                }
                HttpMessage request;
                try {
                    request = VertxHttpMessageMapper.toHttpMessage(
                            VertxHttpGzip.decode(snapshot, options.getMaxBodySizeBytes()), configuration, converters)
                            .contextPath(route.contextPath());
                } catch (Exception e) {
                    failures.add(e);
                    exchange.respond(minimalPlan(e instanceof VertxHttpGzip.Failure failure ? failure.status : 400, options), false);
                    return;
                }
                VertxHttpSecurity.applyIdentity(request, decision.identity());
                if (defaultAdapter) {
                    // Preserve Citrus's per-request reply queue and give shutdown a way to wake its waiter.
                    exchange.replyQueue = new DefaultMessageQueue(exchange.id + "-reply");
                    request.setHeader(DirectMessageHeaders.REPLY_QUEUE, exchange.replyQueue);
                }
                if (closed.get() || exchange.terminal.get()) return;
                // Interceptors wrap custom handlers and the Citrus adapter alike, mirroring the
                // shared handler-mapping interceptor chain; the reply still goes through response
                // conversion, gzip selection, framing and the tracing cache.
                Message reply = VertxHttpInterceptor.invoke(interceptors, request, handlerRequest ->
                        pipeline.handle(handlerRequest, filtered -> {
                    if (closed.get() || exchange.terminal.get()) return null;
                    // Filters may edit application headers, but cannot replace reply-correlation metadata.
                    filtered.setHeader(VertxHttpMessageMapper.EXCHANGE_ID_HEADER, snapshot.exchangeId());
                    filtered.setHeader(VertxHttpMessageMapper.GENERATION_HEADER, snapshot.generation());
                    if (defaultAdapter) filtered.setHeader(DirectMessageHeaders.REPLY_QUEUE, exchange.replyQueue);
                    VertxHttpSecurity.applyIdentity(filtered, decision.identity());
                    return messageExchange.exchange(filtered, adapter);
                }));
                ResponsePlan plan = VertxHttpMessageMapper.toResponsePlan(reply, configuration,
                        "HEAD".equalsIgnoreCase(snapshot.method()), converters);
                exchange.respond(VertxHttpGzip.encode(plan, snapshot), false);
            } catch (Exception e) {
                if (!closed.get() && !exchange.terminal.get()) {
                    failures.add(e);
                    exchange.respond(minimalPlan(500, options), false);
                }
            } finally {
                exchange.workerFinished = true;
                exchange.releaseIfFinished();
            }
        }
    }

    private final class VertxExchange {
        private final GenerationHandler owner;
        private final HttpServerRequest request;
        private final HttpServerResponse response;
        private final Context context = Vertx.currentContext();
        private final String id;
        private final AtomicBoolean terminal = new AtomicBoolean();
        private final AtomicBoolean released = new AtomicBoolean();
        private final AtomicBoolean workerStarted = new AtomicBoolean();
        private volatile boolean workerFinished = true;
        private volatile boolean responseFinished;
        private volatile DefaultMessageQueue replyQueue;
        private Buffer body = Buffer.buffer();
        private long timerId = -1;

        private VertxExchange(GenerationHandler owner, HttpServerRequest request) {
            this.owner = owner;
            this.request = request;
            this.response = request.response();
            this.id = "g" + owner.generation + "-x" + exchangeSequence.incrementAndGet();
        }

        private void upgrade(VertxHttpSecurity.Identity identity) {
            if (!owner.sockets.reserve()) { respond(minimalPlan(503, owner.options), true); return; }
            if (!terminal.compareAndSet(false, true)) { owner.sockets.releaseReservation(); return; }
            owner.vertx.cancelTimer(timerId);
            context.runOnContext(ignored -> {
                if (owner.closed.get()) {
                    owner.sockets.releaseReservation(); abandon(); return;
                }
                response.closeHandler(null);
                request.exceptionHandler(null);
                try {
                    request.toWebSocket().onComplete(result -> {
                        if (result.succeeded()) owner.sockets.attach(result.result(), identity);
                        else { owner.sockets.releaseReservation(); request.connection().close(); }
                        responseFinished = true; releaseIfFinished();
                    });
                } catch (Exception e) {
                    failures.add(e); owner.sockets.releaseReservation(); request.connection().close();
                    responseFinished = true; releaseIfFinished();
                }
            });
        }

        private void respond(ResponsePlan plan, boolean closeConnection) {
            if (!terminal.compareAndSet(false, true)) return;
            owner.vertx.cancelTimer(timerId);
            context.runOnContext(ignored -> {
                if (owner.closed.get()) { abandon(); return; }
                if (closeConnection) { request.pause(); request.handler(null); body = null; }
                try {
                    response.setStatusCode(plan.statusCode());
                    plan.headers().forEach((name, values) -> values.forEach(value -> response.headers().add(name, value)));
                    if (closeConnection) response.putHeader("Connection", "close");
                    owner.responseCache.put(id, plan);
                    response.end(plan.suppressBody() ? Buffer.buffer() : Buffer.buffer(plan.body())).onComplete(result -> {
                        if (result.failed() && !owner.closed.get()) failures.add(result.cause());
                        responseFinished = true;
                        releaseIfFinished();
                        if (closeConnection) request.connection().close();
                    });
                } catch (Exception e) {
                    failures.add(e);
                    abandon();
                }
            });
        }

        private void abandon() {
            terminal.set(true);
            owner.vertx.cancelTimer(timerId);
            DefaultMessageQueue waiter = replyQueue;
            if (waiter != null) waiter.send(new DefaultMessage(""));
            // A cancelled queued task must not run later or retain its admission permit.
            if (workerStarted.compareAndSet(false, true)) workerFinished = true;
            responseFinished = true;
            releaseIfFinished();
            context.runOnContext(ignored -> {
                body = null;
                request.pause();
                request.handler(null);
                request.connection().close();
            });
        }

        private void releaseIfFinished() {
            if (workerFinished && responseFinished && released.compareAndSet(false, true)) {
                owner.executor.release();
                owner.exchanges.remove(id);
            }
        }
    }
}
