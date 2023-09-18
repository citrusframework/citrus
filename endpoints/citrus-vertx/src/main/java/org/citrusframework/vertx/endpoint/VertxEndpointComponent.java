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

package org.citrusframework.vertx.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.vertx.factory.VertxInstanceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxEndpointComponent extends AbstractEndpointComponent {

    public static final String VERTX_INSTANCE_FACTORY = "vertxInstanceFactory";

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(VertxEndpointComponent.class);

    /**
     * Default constructor using the name for this component.
     */
    public VertxEndpointComponent() {
        super("vertx");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        VertxEndpoint endpoint;

        if (resourcePath.startsWith("sync:")) {
            endpoint = new VertxSyncEndpoint();
        } else {
            endpoint = new VertxEndpoint();
        }

        if (resourcePath.contains("pubSub:")) {
            endpoint.getEndpointConfiguration().setPubSubDomain(true);
        }

        // set event bus address
        if (resourcePath.indexOf(':') > 0) {
            endpoint.getEndpointConfiguration().setAddress(resourcePath.substring(resourcePath.lastIndexOf(':') + 1));
        } else {
            endpoint.getEndpointConfiguration().setAddress(resourcePath);
        }

        // set vert.x factory if set
        if (parameters.containsKey(VERTX_INSTANCE_FACTORY)) {
            String vertFactoryBean = parameters.remove(VERTX_INSTANCE_FACTORY);

            if (context.getReferenceResolver() != null) {
                endpoint.setVertxInstanceFactory(context.getReferenceResolver().resolve(vertFactoryBean, VertxInstanceFactory.class));
            } else {
                logger.warn("Unable to set custom Vert.x instance factory as Spring application context is not accessible!");
            }
        } else {
            // set default jms connection factory
            if (context.getReferenceResolver() != null && context.getReferenceResolver().isResolvable(VERTX_INSTANCE_FACTORY)) {
                endpoint.setVertxInstanceFactory(context.getReferenceResolver().resolve(VERTX_INSTANCE_FACTORY, VertxInstanceFactory.class));
            } else {
                logger.warn("Unable to set default Vert.x instance factory as Spring application context is not accessible or default factory bean is not available!");
            }
        }

        enrichEndpointConfiguration(endpoint.getEndpointConfiguration(), parameters, context);

        return endpoint;
    }
}
