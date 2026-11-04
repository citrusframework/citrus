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

package org.citrusframework.kubernetes.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.citrusframework.http.client.HttpEndpointComponent;
import org.citrusframework.http.client.HttpsEndpointComponent;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.7
 */
public class KubernetesEndpointComponentTest {

    private final TestContext context = new TestContext();

    @Test
    public void testCreateClientEndpoint() {
        KubernetesEndpointComponent component = new KubernetesEndpointComponent();

        Endpoint endpoint = component.createEndpoint("k8s:localhost:8443/", context);

        Assertions.assertEquals(KubernetesClient.class, endpoint.getClass());

        Assertions.assertEquals("https://localhost:8443/", ((KubernetesClient)endpoint).getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl());

        endpoint = component.createEndpoint("k8s:http://localhost:8443/", context);

        Assertions.assertEquals(KubernetesClient.class, endpoint.getClass());

        Assertions.assertEquals("http://localhost:8443/", ((KubernetesClient)endpoint).getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl());
        Assertions.assertEquals(5000L, ((KubernetesClient) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateClientEndpointWithParameters() {
        KubernetesEndpointComponent component = new KubernetesEndpointComponent();

        Endpoint endpoint = component.createEndpoint("k8s:localhost:8443?namespace=myNamespace&timeout=10000", context);

        Assertions.assertEquals(KubernetesClient.class, endpoint.getClass());

        Assertions.assertEquals("https://localhost:8443/", ((KubernetesClient)endpoint).getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl());
        Assertions.assertEquals("myNamespace", ((KubernetesClient)endpoint).getEndpointConfiguration().getKubernetesClientConfig().getNamespace());
        Assertions.assertEquals(10000L, ((KubernetesClient) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assertions.assertEquals(5L, validators.size());
        Assertions.assertNotNull(validators.get("direct"));
        Assertions.assertEquals(DirectEndpointComponent.class, validators.get("direct").getClass());
        Assertions.assertNotNull(validators.get("message-store"));
        Assertions.assertEquals(MessageStoreEndpointComponent.class, validators.get("message-store").getClass());
        Assertions.assertNotNull(validators.get("http"));
        Assertions.assertEquals(HttpEndpointComponent.class, validators.get("http").getClass());
        Assertions.assertNotNull(validators.get("https"));
        Assertions.assertEquals(HttpsEndpointComponent.class, validators.get("https").getClass());
        Assertions.assertNotNull(validators.get("k8s"));
        Assertions.assertEquals(KubernetesEndpointComponent.class, validators.get("k8s").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("k8s").isPresent());
    }
}
