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

package com.consol.citrus.vertx.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpointComponent;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.vertx.factory.VertxInstanceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxEndpointComponent extends AbstractEndpointComponent {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(VertxEndpointComponent.class);

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        VertxEndpoint endpoint;

        if (resourcePath.startsWith("sync:")) {
            endpoint = new VertxEndpoint(); //TODO use sync endpoint
        } else {
            endpoint = new VertxEndpoint();
        }

        if (resourcePath.contains("pubSub:")) {
            endpoint.getEndpointConfiguration().setPubSubDomain(true);
        }

        // set event bus address
        if (resourcePath.indexOf(":") > 0) {
            endpoint.getEndpointConfiguration().setAddress(resourcePath.substring(resourcePath.lastIndexOf(":") + 1));
        } else {
            endpoint.getEndpointConfiguration().setAddress(resourcePath);
        }

        // set vert.x factory if set
        if (parameters.containsKey("vertxInstanceFactory")) {
            parameters.remove("vertxInstanceFactory");

            if (context.getApplicationContext() != null) {
                endpoint.setVertxInstanceFactory(context.getApplicationContext().getBean("vertxInstanceFactory", VertxInstanceFactory.class));
            } else {
                log.warn("Unable to set custom Vert.x instance factory as Spring application context is not accessible!");
            }
        }

        enrichEndpointConfiguration(endpoint.getEndpointConfiguration(), parameters, context);

        return endpoint;
    }
}
