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

package com.consol.citrus.camel.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpointComponent;
import com.consol.citrus.endpoint.Endpoint;
import org.apache.camel.CamelContext;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelEndpointComponent extends AbstractEndpointComponent {
    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        CamelEndpoint endpoint;
        if (resourcePath.startsWith("sync:")) {
            endpoint = new CamelSyncEndpoint();
            endpoint.getEndpointConfiguration().setEndpointUri(resourcePath.substring("sync:".length()) + getParameterString(parameters, CamelSyncEndpointConfiguration.class));
        } else {
            endpoint = new CamelEndpoint();
            endpoint.getEndpointConfiguration().setEndpointUri(resourcePath + getParameterString(parameters, CamelEndpointConfiguration.class));
        }

        if (context.getApplicationContext() != null) {
            if (context.getApplicationContext().getBeansOfType(CamelContext.class).size() == 1) {
                endpoint.getEndpointConfiguration().setCamelContext(context.getApplicationContext().getBean(CamelContext.class));
            } else if (context.getApplicationContext().containsBean("camelContext")) {
                endpoint.getEndpointConfiguration().setCamelContext(context.getApplicationContext().getBean("camelContext", CamelContext.class));
            }
        }

        enrichEndpointConfiguration(endpoint.getEndpointConfiguration(),
                getEndpointConfigurationParameters(parameters, endpoint.getEndpointConfiguration().getClass()), context);

        return endpoint;
    }
}
