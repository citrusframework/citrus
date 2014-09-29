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

package com.consol.citrus.vertx.factory;

import com.consol.citrus.vertx.endpoint.VertxEndpointConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.*;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Abstract Vertx instance factory provides basic method for creating a new Vertx instance. By default waits for
 * instance to start up properly.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public abstract class AbstractVertxInstanceFactory implements VertxInstanceFactory {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(AbstractVertxInstanceFactory.class);

    /**
     * Creates new Vert.x instance with default factory. Subclasses may overwrite this
     * method in order to provide special Vert.x instance.
     * @return
     */
    protected Vertx createVertx(VertxEndpointConfiguration endpointConfiguration) {
        final Vertx[] vertx = new Vertx[1];
        final Future loading = new DefaultFutureResult();

        Handler<AsyncResult<Vertx>> asyncLoadingHandler = new Handler<AsyncResult<Vertx>>() {
            @Override
            public void handle(AsyncResult<Vertx> event) {
                vertx[0] = event.result();
                loading.setResult(Boolean.TRUE);
                log.info("Vert.x instance successfully started");
            }
        };

        if (endpointConfiguration.getPort() > 0) {
            log.info(String.format("Creating new Vert.x instance '%s:%s' ...", endpointConfiguration.getHost(), endpointConfiguration.getPort()));
            VertxFactory.newVertx(endpointConfiguration.getPort(), endpointConfiguration.getHost(), asyncLoadingHandler);
        } else {
            log.info(String.format("Creating new Vert.x instance '%s:%s' ...", endpointConfiguration.getHost(), 0L));
            VertxFactory.newVertx(0, endpointConfiguration.getHost(), asyncLoadingHandler);
        }

        // Wait for full loading
        while (!loading.complete()) {
            try {
                log.debug("Waiting for Vert.x instance to startup");
                Thread.sleep(250L);
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for Vert.x instance startup", e);
            }
        }

        return vertx[0];
    }
}
