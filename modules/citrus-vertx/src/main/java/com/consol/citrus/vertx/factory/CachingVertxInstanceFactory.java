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
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Vert.x instance factory that caches created instances in memory. Ensures that same cluster host and port is
 * instance is created only once.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CachingVertxInstanceFactory implements VertxInstanceFactory {

    /** Cache holds Vert.x instances identified by cluster hostname port combination */
    private Map<String, Vertx> instanceCache = new HashMap<String, Vertx>();

    @Override
    public synchronized Vertx newInstance(VertxEndpointConfiguration endpointConfiguration) {
        if (endpointConfiguration.getClusterPort() > 0) {
            String instanceKey = endpointConfiguration.getClusterHost() + ":" + endpointConfiguration.getClusterPort();
            if (instanceCache.containsKey(instanceKey)) {
                return instanceCache.get(instanceKey);
            } else {
                Vertx vertx = VertxFactory.newVertx(endpointConfiguration.getClusterPort(), endpointConfiguration.getClusterHost());
                instanceCache.put(instanceKey, vertx);
                return vertx;
            }
        }

        if (instanceCache.containsKey(endpointConfiguration.getClusterHost())) {
            return instanceCache.get(endpointConfiguration.getClusterHost());
        } else {
            Vertx vertx = VertxFactory.newVertx(endpointConfiguration.getClusterHost());
            instanceCache.put(endpointConfiguration.getClusterHost(), vertx);
            return vertx;
        }
    }
}
