/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.vertx.factory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.vertx.endpoint.VertxEndpointConfiguration;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Vertx instance factory provides basic method for creating a new Vertx instance. By default, waits for
 * instance to start up properly.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public abstract class AbstractVertxInstanceFactory implements VertxInstanceFactory {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(AbstractVertxInstanceFactory.class);

    /**
     * Creates new Vert.x instance with default factory. Subclasses may overwrite this
     * method in order to provide special Vert.x instance.
     * @return
     */
    protected Vertx createVertx(VertxEndpointConfiguration endpointConfiguration) {
        final CompletableFuture<Vertx> loading = new CompletableFuture<>();

        Handler<AsyncResult<Vertx>> asyncLoadingHandler = event -> {
            loading.complete(event.result());
            logger.info("Vert.x instance started");
        };

        if (logger.isDebugEnabled()) {
            logger.debug("Creating new Vert.x instance ...");
        }

        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setClusterManager(new HazelcastClusterManager());
        Vertx.clusteredVertx(vertxOptions, asyncLoadingHandler);

        // Wait for full loading
        int maxAttempts = 25;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                Vertx vertx = loading.get(500, TimeUnit.MILLISECONDS);
                if (vertx != null) {
                    return vertx;
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("Interrupted while waiting for Vert.x instance startup", e);
            } catch (TimeoutException e) {
                logger.debug("Waiting for Vert.x instance to startup ...");
            }
        }

        throw new CitrusRuntimeException("Failed to start Vert.x instance");
    }
}
