/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.kubernetes.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.kubernetes.client.KubernetesClient;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesEndpointComponent extends AbstractEndpointComponent {

    /**
     * Default constructor using the name for this component.
     */
    public KubernetesEndpointComponent() {
        super("k8s");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        KubernetesClient client = new KubernetesClient();

        if (resourcePath.startsWith("https://") || resourcePath.startsWith("http://")) {
            client.getEndpointConfiguration().getKubernetesClientConfig().setMasterUrl(resourcePath);
        } else {
            client.getEndpointConfiguration().getKubernetesClientConfig().setMasterUrl("https://" + resourcePath);
        }

        if (parameters.containsKey("version")) {
            client.getEndpointConfiguration().getKubernetesClientConfig().setApiVersion(parameters.remove("version"));
        }

        if (parameters.containsKey("username")) {
            client.getEndpointConfiguration().getKubernetesClientConfig().setUsername(parameters.remove("username"));
        }

        if (parameters.containsKey("password")) {
            client.getEndpointConfiguration().getKubernetesClientConfig().setPassword(parameters.remove("password"));
        }

        if (parameters.containsKey("namespace")) {
            client.getEndpointConfiguration().getKubernetesClientConfig().setNamespace(parameters.remove("namespace"));
        }

        if (parameters.containsKey("cert-file")) {
            client.getEndpointConfiguration().getKubernetesClientConfig().setCaCertFile(parameters.remove("cert-file"));
        }

        enrichEndpointConfiguration(client.getEndpointConfiguration(),
                getEndpointConfigurationParameters(parameters, KubernetesEndpointConfiguration.class), context);
        return client;
    }
}
