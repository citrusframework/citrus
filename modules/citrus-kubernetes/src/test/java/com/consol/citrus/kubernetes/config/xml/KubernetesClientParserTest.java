/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.kubernetes.config.xml;

import com.consol.citrus.kubernetes.client.KubernetesClient;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class KubernetesClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testKubernetesClientParser() {
        Map<String, KubernetesClient> clients = beanDefinitionContext.getBeansOfType(KubernetesClient.class);

        Assert.assertEquals(clients.size(), 2);

        // 1st client
        KubernetesClient kubernetesClient = clients.get("kubernetesClient1");
        Assert.assertNotNull(kubernetesClient.getEndpointConfiguration().getKubernetesClient());

        // 2nd client
        kubernetesClient = clients.get("kubernetesClient2");
        Assert.assertNotNull(kubernetesClient.getEndpointConfiguration().getKubernetesClient());
        Assert.assertEquals(kubernetesClient.getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl().toString(), "http://localhost:8843/");
        Assert.assertEquals(kubernetesClient.getEndpointConfiguration().getKubernetesClientConfig().getApiVersion(), "v1");
        Assert.assertEquals(kubernetesClient.getEndpointConfiguration().getKubernetesClientConfig().getUsername(), "user");
        Assert.assertEquals(kubernetesClient.getEndpointConfiguration().getKubernetesClientConfig().getPassword(), "s!cr!t");
        Assert.assertEquals(kubernetesClient.getEndpointConfiguration().getKubernetesClientConfig().getNamespace(), "user_namespace");
        Assert.assertEquals(kubernetesClient.getEndpointConfiguration().getKubernetesClientConfig().getCaCertFile(), "/path/to/some/cert/ca.cert");
    }
}
