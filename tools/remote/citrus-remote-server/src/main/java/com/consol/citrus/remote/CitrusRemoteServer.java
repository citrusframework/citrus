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

package com.consol.citrus.remote;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.consol.citrus.remote.controller.RunController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import static spark.Spark.port;

/**
 * @author Christoph Deppisch
 */
public class CitrusRemoteServer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusRemoteServer.class);

    /** Endpoint configuration */
    private final CitrusRemoteConfiguration configuration;

    private CitrusRemoteApplication application;

    /** Completed future marking completed state */
    protected final CompletableFuture<Boolean> completed = new CompletableFuture<>();

    /**
     * Default constructor initializing controller and configuration.
     */
    public CitrusRemoteServer() {
        this(new CitrusRemoteConfiguration());
    }

    /**
     * Default constructor using controller and configuration.
     * @param configuration
     */
    public CitrusRemoteServer(CitrusRemoteConfiguration configuration) {
        this.configuration = configuration;
    }

    public CitrusRemoteServer(String[] args) {
        this(new CitrusRemoteOptions().apply(new CitrusRemoteConfiguration(), args));
    }

    /**
     * Main method
     * @param args
     */
    public static void main(String[] args) {
        CitrusRemoteServer server = new CitrusRemoteServer(args);

        if (server.configuration.getTimeToLive() > 0) {
            CompletableFuture.runAsync(() -> {
                try {
                    new CompletableFuture<Void>().get(server.configuration.getTimeToLive(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    server.stop();
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
    public void start() {
        application = new CitrusRemoteApplication(configuration);
        port(configuration.getPort());
        application.init();

        if (!configuration.isSkipTests()) {
            new RunController(configuration).run();
        }

        if (configuration.getTimeToLive() == 0) {
            stop();
        }
    }

    /**
     * Stops the server instance.
     */
    public void stop() {
        application.destroy();
        complete();
        Spark.stop();
    }

    /**
     * Completes this application.
     */
    public void complete() {
        completed.complete(true);
    }

    /**
     * Waits for completed state of application.
     * @return
     */
    public boolean waitForCompletion() {
        try {
            return completed.get();
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Failed to wait for server completion", e);
        }

        return false;
    }
}
