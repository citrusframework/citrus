/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.agent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;
import org.citrusframework.agent.util.ConfigurationHelper;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.currentThread;

public class CitrusAgentServer implements Server {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CitrusAgentServer.class);

    private String name = "citrus-agent-server";

    /** Endpoint configuration */
    private final CitrusAgentConfiguration configuration;

    /** Router customizations */
    private final List<Consumer<Router>> routerCustomizations;

    private CitrusAgentApplication application;

    /** Completed future marking completed state */
    protected final CompletableFuture<Boolean> completed = new CompletableFuture<>();

    /**
     * Default constructor uses configuration from envVars only.
     */
    public CitrusAgentServer() {
        this(ConfigurationHelper.fromEnvVars());
    }

    /**
     * Constructor uses given configuration and no router customizations.
     */
    public CitrusAgentServer(CitrusAgentConfiguration configuration) {
        this(configuration, Collections.emptyList());
    }

    /**
     * Constructor uses the configuration and the given router customizations.
     */
    public CitrusAgentServer(CitrusAgentConfiguration configuration, List<Consumer<Router>> routerCustomizations) {
        this.configuration = configuration;
        this.routerCustomizations = routerCustomizations;
    }

    /**
     * Constructor uses command line arguments and envVars.
     */
    public CitrusAgentServer(String[] args, List<Consumer<Router>> routerCustomizations) {
        this(new CitrusAgentOptions().apply(ConfigurationHelper.fromEnvVars(), args), routerCustomizations);
    }

    /**
     * Main method
     */
    public static void main(String[] args) {
        entrypoint(args, Collections.emptyList());
    }

    /**
     * Entrypoint method
     */
    public static void entrypoint(String[] args, List<Consumer<Router>> routerCustomizations) {
        CitrusAgentServer server = new CitrusAgentServer(args, routerCustomizations);

        if (server.configuration.getTimeToLive() > 0) {
            CompletableFuture.runAsync(() -> {
                try {
                    new CompletableFuture<Void>().get(server.configuration.getTimeToLive(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    server.stop();
                    currentThread().interrupt();
                }
            });
        }

        server.start();

        if (server.configuration.isSystemExit()) {
            if (server.waitForCompletion()) {
                System.exit(0);
            } else {
                System.exit(-1);
            }
        } else {
            server.waitForCompletion();
        }
    }

    /**
     * Start server instance and listen for incoming requests.
     */
    @Override
    public void start() {
        // Load additional artifacts from agent configuration
        ConfigurationHelper.resolveArtifacts(configuration);

        application = new CitrusAgentApplication(configuration, routerCustomizations);
        Vertx.vertx(new VertxOptions()
                        .setMaxEventLoopExecuteTime(600)
                        .setMaxEventLoopExecuteTimeUnit(TimeUnit.SECONDS)
                )
                .deployVerticle(application);

        configuration.getDefaultProperties().putIfAbsent("citrus.default.message.type", "JSON");
        configuration.setDefaultProperties();

        if (configuration.isSkipTests()) {
            logger.info("Skip tests on startup - waiting for requests to run tests");
        } else if (configuration.hasTests()) {
            logger.info("Run tests on startup ...");
            new RunService().run(configuration);

            if (configuration.isSystemExit()) {
                System.exit(0);
            } else {
                logger.info("Startup done - waiting for requests to run further tests");
            }
        } else {
            logger.info("Startup done - waiting for requests to run tests");
        }

        if (configuration.getTimeToLive() == 0) {
            stop();
        }
    }

    @Override
    public void run() {
        try {
            start();
            boolean healthyShutdown = waitForCompletion();

            if (!healthyShutdown) {
                logger.error("Citrus agent server has been shutdown");
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to start Citrus agent application", e);
        } finally {
            stop();
        }
    }

    /**
     * Stops the server instance.
     */
    @Override
    public void stop() {
        if (application != null) {
            application.stop();
        }

        complete();
    }

    @Override
    public boolean isRunning() {
        return !completed.isDone();
    }

    /**
     * Completes this application.
     */
    public void complete() {
        completed.complete(true);
    }

    /**
     * Waits for completed state of application.
     */
    public boolean waitForCompletion() {
        try {
            return completed.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Failed to wait for server completion", e);
            currentThread().interrupt();
        }

        return false;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
