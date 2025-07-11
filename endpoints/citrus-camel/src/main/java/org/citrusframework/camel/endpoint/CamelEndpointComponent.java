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

package org.citrusframework.camel.endpoint;

import java.util.Map;

import org.citrusframework.camel.util.CamelUtils;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;

/**
 * @since 1.4.1
 */
public class CamelEndpointComponent extends AbstractEndpointComponent {
    /**
     * Default constructor using the name for this component.
     */
    public CamelEndpointComponent() {
        super("camel");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        CamelEndpoint endpoint;
        if (resourcePath.startsWith("sync:")) {
            endpoint = new CamelSyncEndpoint();
            endpoint.getEndpointConfiguration().setEndpointUri(resourcePath.substring("sync:".length()) + getParameterString(parameters, CamelSyncEndpointConfiguration.class));
        } else if (resourcePath.startsWith("inOut:")) {
            endpoint = new CamelSyncEndpoint();
            endpoint.getEndpointConfiguration().setEndpointUri(resourcePath.substring("inOut:".length()) + getParameterString(parameters, CamelSyncEndpointConfiguration.class));
        } else if (resourcePath.startsWith("inOnly:")) {
            endpoint = new CamelEndpoint();
            endpoint.getEndpointConfiguration().setEndpointUri(
                    resourcePath.substring("inOnly:".length()) + getParameterString(parameters, CamelEndpointConfiguration.class));
        } else {
            endpoint = new CamelEndpoint();
            endpoint.getEndpointConfiguration().setEndpointUri(
                    resourcePath + getParameterString(parameters, CamelEndpointConfiguration.class));
        }

        if (context.getReferenceResolver() != null) {
            endpoint.getEndpointConfiguration().setCamelContext(
                    CamelUtils.resolveCamelContext(context.getReferenceResolver(), endpoint.getEndpointConfiguration()));
        }

        enrichEndpointConfiguration(endpoint.getEndpointConfiguration(),
                getEndpointConfigurationParameters(parameters, endpoint.getEndpointConfiguration().getClass()), context);

        return endpoint;
    }
}
