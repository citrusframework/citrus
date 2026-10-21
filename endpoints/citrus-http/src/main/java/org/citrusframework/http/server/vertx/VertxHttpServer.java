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

package org.citrusframework.http.server.vertx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.interceptor.HttpServerInterceptor;
import org.citrusframework.http.security.BasicAuthentication;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.http.security.SSLConnection;
import org.citrusframework.http.server.AbstractHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Vert.x based implementation of an Http server. The server accepts client connections and
 * receives messages via the Http transport protocol. Next to the default Jetty based
 * {@link org.citrusframework.http.server.HttpServer} this server implementation is opt-in and
 * shares all transport neutral configuration options with the Jetty based server.
 *
 * @since 5.2
 */
public class VertxHttpServer extends AbstractHttpServer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(VertxHttpServer.class);

    /** Timeout in seconds waiting for the server to start listening */
    public static final long LISTEN_TIMEOUT_SECONDS = 30L;

    /** Timeout in seconds waiting for a resource to close */
    public static final long CLOSE_TIMEOUT_SECONDS = 10L;

    /** Additional time added to the server timeout to calculate the maximum execution time of a worker task */
    public static final long WORKER_EXECUTION_TIME_OFFSET_MILLIS = 60000L;

    /** Default size of the worker pool handling the endpoint adapter */
    public static final int DEFAULT_WORKER_POOL_SIZE = 200;

    /** Vert.x instance used by this server. May be supplied by the user. */
    private Vertx vertx;

    /** Flag indicating that this server created the Vert.x instance on its own */
    private boolean ownsVertx;

    /** The Vert.x Http server handling the client connections */
    private HttpServer vertxHttpServer;

    /** Worker executor running the endpoint adapter off the event loop */
    private WorkerExecutor workerExecutor;

    /** Size of the worker pool running the endpoint adapter */
    private int workerPoolSize = DEFAULT_WORKER_POOL_SIZE;

    /** Interceptors as configured by the user. Used to detect configuration that is bound to another server technology. */
    private List<Object> configuredInterceptors = new ArrayList<>();

    @Override
    public void initialize() {
        validateConfiguration();
        super.initialize();
    }

    /**
     * Validates the configuration and rejects options that are bound to a specific server technology.
     * The error message always names the offending configuration and the replacement to use.
     */
    public void validateConfiguration() {
        for (Object interceptor : configuredInterceptors) {
            if (!(interceptor instanceof HttpServerInterceptor)) {
                throw new CitrusRuntimeException("Invalid interceptor configuration for Vert.x Http server: %s is not a %s. Use a %s implementation instead."
                        .formatted(interceptor.getClass().getName(), HttpServerInterceptor.class.getName(), HttpServerInterceptor.class.getName()));
            }
        }

        HttpAuthentication authentication = getAuthentication();
        if (authentication != null && authentication.getServerAuthentication(getSecuredPath()).isEmpty()) {
            throw new CitrusRuntimeException("Invalid authentication configuration for Vert.x Http server: %s does not provide a transport neutral server authentication. Use a %s implementation instead."
                    .formatted(authentication.getClass().getName(), BasicAuthentication.class.getName()));
        }

        HttpSecureConnection secureConnection = getSecureConnection();
        if (secureConnection != null && secureConnection.getServerTlsSettings().isEmpty()) {
            throw new CitrusRuntimeException("Invalid secure connection configuration for Vert.x Http server: %s does not provide transport neutral server TLS settings. Use a %s implementation instead."
                    .formatted(secureConnection.getClass().getName(), SSLConnection.class.getName()));
        }

        if (workerPoolSize < 1) {
            throw new CitrusRuntimeException("Invalid worker pool size for Vert.x Http server: %d. The worker pool size must be a value greater than 0."
                    .formatted(workerPoolSize));
        }

        if (getContextPath() == null || !getContextPath().startsWith("/")) {
            throw new CitrusRuntimeException("Invalid context path for Vert.x Http server: %s. The context path must start with a '/'."
                    .formatted(getContextPath()));
        }
    }

    @Override
    protected void startup() {
        if (vertx == null) {
            vertx = Vertx.vertx();
            ownsVertx = true;
        }

        workerExecutor = vertx.createSharedWorkerExecutor("citrus-http-" + getName(), workerPoolSize,
                getDefaultTimeout() + WORKER_EXECUTION_TIME_OFFSET_MILLIS);

        HttpServerOptions options = new HttpServerOptions()
                .setHost("0.0.0.0")
                .setPort(resolvePort());

        try {
            vertxHttpServer = vertx.createHttpServer(options)
                    .requestHandler(createRouter());

            vertxHttpServer.listen().toCompletionStage().toCompletableFuture().get(LISTEN_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            logger.info("Started Vert.x Http server on port {}", getActualPort());
        } catch (Exception e) {
            closeCreatedResources();
            throw new CitrusRuntimeException("Failed to start Vert.x Http server on port %d".formatted(resolvePort()), e);
        }
    }

    @Override
    protected void shutdown() {
        if (vertxHttpServer != null) {
            awaitClose(vertxHttpServer.close());
            vertxHttpServer = null;
        }

        if (workerExecutor != null) {
            awaitClose(workerExecutor.close());
            workerExecutor = null;
        }

        if (ownsVertx && vertx != null) {
            awaitClose(vertx.close());
            vertx = null;
            ownsVertx = false;
        }
    }

    /**
     * Creates the router handling the incoming requests. All requests below the context path are
     * routed to the Citrus endpoint adapter, requests outside the context path are answered with
     * a not found error.
     */
    private Router createRouter() {
        Router router = Router.router(vertx);
        Router contextRouter = Router.router(vertx);

        contextRouter.route().handler(this::handleContextRequest);
        router.mountSubRouter(getContextPath(), contextRouter);
        router.route().handler(context -> context.response().setStatusCode(404).end());

        return router;
    }

    /**
     * Handles a request that matches the context path.
     */
    private void handleContextRequest(RoutingContext context) {
        String path = context.request().path();
        String contextPath = getContextPath();

        if (!"/".equals(contextPath)) {
            if (contextPath.equals(path)) {
                String query = context.request().query();
                String location = contextPath + "/";

                if (query != null && !query.isEmpty()) {
                    location = location + "?" + query;
                }

                context.response().setStatusCode(301).putHeader("Location", location).end();
                return;
            }

            if (path == null || !path.startsWith(contextPath + "/")) {
                context.response().setStatusCode(404).end();
                return;
            }
        }

        context.response().setStatusCode(501).end();
    }

    /**
     * Determines the port to listen on. A secure connection is served on the secure port.
     */
    private int resolvePort() {
        return getSecureConnection() != null ? getSecurePort() : getPort();
    }

    /**
     * Closes all resources that have been created within the current startup attempt.
     */
    private void closeCreatedResources() {
        if (vertxHttpServer != null) {
            vertxHttpServer.close();
            vertxHttpServer = null;
        }

        if (workerExecutor != null) {
            workerExecutor.close();
            workerExecutor = null;
        }

        if (ownsVertx && vertx != null) {
            vertx.close();
            vertx = null;
            ownsVertx = false;
        }
    }

    /**
     * Waits for the given close operation to complete.
     */
    private void awaitClose(Future<Void> closeFuture) {
        try {
            closeFuture.toCompletionStage().toCompletableFuture().get(CLOSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Failed to close Vert.x Http server resource: {}", e.getMessage());
        }
    }

    /**
     * Gets the Vert.x instance used by this server.
     *
     * @return the Vert.x instance.
     */
    public Vertx getVertx() {
        return vertx;
    }

    /**
     * Sets the Vert.x instance to use. A Vert.x instance supplied by the user is never closed by
     * this server.
     *
     * @param vertx the Vert.x instance to use.
     */
    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
        this.ownsVertx = false;
    }

    /**
     * Gets the size of the worker pool running the endpoint adapter.
     *
     * @return the worker pool size.
     */
    public int getWorkerPoolSize() {
        return workerPoolSize;
    }

    /**
     * Sets the size of the worker pool running the endpoint adapter.
     *
     * @param workerPoolSize the worker pool size to set.
     */
    public void setWorkerPoolSize(int workerPoolSize) {
        this.workerPoolSize = workerPoolSize;
    }

    /**
     * Gets the worker executor running the endpoint adapter.
     *
     * @return the worker executor.
     */
    public WorkerExecutor getWorkerExecutor() {
        return workerExecutor;
    }

    /**
     * Gets the interceptors as configured by the user.
     *
     * @return the configured interceptors.
     */
    public List<Object> getConfiguredInterceptors() {
        return configuredInterceptors;
    }

    /**
     * Sets the interceptors as configured by the user.
     *
     * @param configuredInterceptors the configured interceptors to set.
     */
    public void setConfiguredInterceptors(List<Object> configuredInterceptors) {
        this.configuredInterceptors = configuredInterceptors;
    }

    /**
     * Gets the port the server is actually listening on.
     *
     * @return the actual port.
     */
    public int getActualPort() {
        if (vertxHttpServer != null) {
            return vertxHttpServer.actualPort();
        }

        return resolvePort();
    }
}
