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

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.vertx.endpoint.VertxEndpointConfiguration;
import io.vertx.core.Vertx;

/**
 * Vert.x instance factory that caches created instances in memory. Ensures that same cluster host and port is
 * instance is created only once.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CachingVertxInstanceFactory extends AbstractVertxInstanceFactory {

    /** Cache holds Vert.x instances identified by cluster hostname port combination */
    private final Map<String, Vertx> instanceCache = new HashMap<>();

    @Override
    public synchronized Vertx newInstance(VertxEndpointConfiguration endpointConfiguration) {
        String instanceKey;
        if (endpointConfiguration.getPort() > 0) {
            instanceKey = endpointConfiguration.getHost() + ":" + endpointConfiguration.getPort();
        } else {
            instanceKey = endpointConfiguration.getHost();
        }

        if (instanceCache.containsKey(instanceKey)) {
            return instanceCache.get(instanceKey);
        } else {
            Vertx vertx = createVertx(endpointConfiguration);
            instanceCache.put(instanceKey, vertx);
            return vertx;
        }
    }
}
