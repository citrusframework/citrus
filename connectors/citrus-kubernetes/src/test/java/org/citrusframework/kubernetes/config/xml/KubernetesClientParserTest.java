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

package org.citrusframework.kubernetes.config.xml;

import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        KubernetesClient client = clients.get("k8sClient1");
        Assert.assertNotNull(client.getClient());

        // 2nd client
        client = clients.get("k8sClient2");
        Assert.assertNotNull(client.getClient());
        Assert.assertEquals(client.getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl().toString(), "http://localhost:8843/");
        Assert.assertEquals(client.getEndpointConfiguration().getKubernetesClientConfig().getApiVersion(), "v1");
        Assert.assertEquals(client.getEndpointConfiguration().getKubernetesClientConfig().getUsername(), "user");
        Assert.assertEquals(client.getEndpointConfiguration().getKubernetesClientConfig().getPassword(), "s!cr!t");
        Assert.assertEquals(client.getEndpointConfiguration().getKubernetesClientConfig().getNamespace(), "user_namespace");
        Assert.assertEquals(client.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter", MessageConverter.class));
        Assert.assertEquals(client.getEndpointConfiguration().getObjectMapper(), beanDefinitionContext.getBean("objectMapper", ObjectMapper.class));

    }
}
