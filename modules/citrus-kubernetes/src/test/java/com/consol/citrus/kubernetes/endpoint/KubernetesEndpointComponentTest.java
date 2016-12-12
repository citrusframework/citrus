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

package com.consol.citrus.kubernetes.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.kubernetes.client.KubernetesClient;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesEndpointComponentTest {

    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
    }

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

        reset(applicationContext);
        Endpoint endpoint = component.createEndpoint("k8s:localhost:8443?namespace=myNamespace&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), KubernetesClient.class);

        Assert.assertEquals(((KubernetesClient)endpoint).getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl(), "https://localhost:8443");
        Assert.assertEquals(((KubernetesClient)endpoint).getEndpointConfiguration().getKubernetesClientConfig().getNamespace(), "myNamespace");
        Assert.assertEquals(((KubernetesClient) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

}