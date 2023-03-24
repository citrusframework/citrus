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
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.http.client.HttpEndpointComponent;
import org.citrusframework.http.client.HttpsEndpointComponent;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesEndpointComponentTest {

    private TestContext context = new TestContext();

    @Test
    public void testCreateClientEndpoint() throws Exception {
        KubernetesEndpointComponent component = new KubernetesEndpointComponent();

        Endpoint endpoint = component.createEndpoint("k8s:localhost:8443/", context);

        Assert.assertEquals(endpoint.getClass(), KubernetesClient.class);

        Assert.assertEquals(((KubernetesClient)endpoint).getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl(), "https://localhost:8443/");

        endpoint = component.createEndpoint("k8s:http://localhost:8443/", context);

        Assert.assertEquals(endpoint.getClass(), KubernetesClient.class);

        Assert.assertEquals(((KubernetesClient)endpoint).getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl(), "http://localhost:8443/");
        Assert.assertEquals(((KubernetesClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateClientEndpointWithParameters() throws Exception {
        KubernetesEndpointComponent component = new KubernetesEndpointComponent();

        Endpoint endpoint = component.createEndpoint("k8s:localhost:8443?namespace=myNamespace&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), KubernetesClient.class);

        Assert.assertEquals(((KubernetesClient)endpoint).getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl(), "https://localhost:8443");
        Assert.assertEquals(((KubernetesClient)endpoint).getEndpointConfiguration().getKubernetesClientConfig().getNamespace(), "myNamespace");
        Assert.assertEquals(((KubernetesClient) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 4L);
        Assert.assertNotNull(validators.get("direct"));
        Assert.assertEquals(validators.get("direct").getClass(), DirectEndpointComponent.class);
        Assert.assertNotNull(validators.get("http"));
        Assert.assertEquals(validators.get("http").getClass(), HttpEndpointComponent.class);
        Assert.assertNotNull(validators.get("https"));
        Assert.assertEquals(validators.get("https").getClass(), HttpsEndpointComponent.class);
        Assert.assertNotNull(validators.get("k8s"));
        Assert.assertEquals(validators.get("k8s").getClass(), KubernetesEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("k8s").isPresent());
    }

}
