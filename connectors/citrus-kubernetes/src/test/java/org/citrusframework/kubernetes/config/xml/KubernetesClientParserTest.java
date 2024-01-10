/*
 * Copyright 2006-2024 the original author or authors.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Christoph Deppisch
 */
public class KubernetesClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testKubernetesClientParser() {
        Map<String, KubernetesClient> clients = beanDefinitionContext.getBeansOfType(KubernetesClient.class);

        assertEquals(clients.size(), 3);

        // 1st client
        KubernetesClient client = clients.get("k8sClient1");
        assertNotNull(client.getClient());

        // 2nd client
        client = clients.get("k8sClient2");
        assertNotNull(client.getClient());
        assertEquals(client.getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl(), "http://localhost:8843/");
        assertEquals(client.getEndpointConfiguration().getKubernetesClientConfig().getApiVersion(), "v1");
        assertEquals(client.getEndpointConfiguration().getKubernetesClientConfig().getUsername(), "user");
        assertEquals(client.getEndpointConfiguration().getKubernetesClientConfig().getPassword(), "s!cr!t");
        assertEquals(client.getEndpointConfiguration().getKubernetesClientConfig().getNamespace(), "user_namespace");
        assertEquals(client.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter", MessageConverter.class));
        assertEquals(client.getEndpointConfiguration().getObjectMapper(), beanDefinitionContext.getBean("objectMapper", ObjectMapper.class));

        // 3rd client
        client = clients.get("k8sClient3");
        assertNotNull(client.getClient());
        assertEquals(client.getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl(), "http://localhost:8843/");
        assertEquals(client.getEndpointConfiguration().getKubernetesClientConfig().getApiVersion(), "v1");
        assertEquals(client.getEndpointConfiguration().getKubernetesClientConfig().getOauthToken(), "xx508xx63817x752xx74004x30705xx92x58349x5x78f5xx34xxxxx51");
        assertEquals(client.getEndpointConfiguration().getKubernetesClientConfig().getNamespace(), "user_namespace");
        assertEquals(client.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter", MessageConverter.class));
        assertEquals(client.getEndpointConfiguration().getObjectMapper(), beanDefinitionContext.getBean("objectMapper", ObjectMapper.class));

    }
}
