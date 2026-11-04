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

package org.citrusframework.kubernetes.config.xml;

import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KubernetesClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testKubernetesClientParser() {
        Map<String, KubernetesClient> clients = beanDefinitionContext.getBeansOfType(KubernetesClient.class);

        assertEquals(3, clients.size());

        // 1st client
        KubernetesClient client = clients.get("k8sClient1");
        assertNotNull(client.getClient());

        // 2nd client
        client = clients.get("k8sClient2");
        assertNotNull(client.getClient());
        assertEquals("http://localhost:8843/", client.getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl());
        assertEquals("v1", client.getEndpointConfiguration().getKubernetesClientConfig().getApiVersion());
        assertEquals("user", client.getEndpointConfiguration().getKubernetesClientConfig().getUsername());
        assertEquals("s!cr!t", client.getEndpointConfiguration().getKubernetesClientConfig().getPassword());
        assertEquals("user_namespace", client.getEndpointConfiguration().getKubernetesClientConfig().getNamespace());
        assertEquals(beanDefinitionContext.getBean("messageConverter", MessageConverter.class), client.getEndpointConfiguration().getMessageConverter());
        assertEquals(beanDefinitionContext.getBean("jsonMapper", JsonMapper.class), client.getEndpointConfiguration().getJsonMapper());

        // 3rd client
        client = clients.get("k8sClient3");
        assertNotNull(client.getClient());
        assertEquals("http://localhost:8843/", client.getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl());
        assertEquals("v1", client.getEndpointConfiguration().getKubernetesClientConfig().getApiVersion());
        assertEquals("xx508xx63817x752xx74004x30705xx92x58349x5x78f5xx34xxxxx51", client.getEndpointConfiguration().getKubernetesClientConfig().getOauthToken());
        assertEquals("user_namespace", client.getEndpointConfiguration().getKubernetesClientConfig().getNamespace());
        assertEquals(beanDefinitionContext.getBean("messageConverter", MessageConverter.class), client.getEndpointConfiguration().getMessageConverter());
        assertEquals(beanDefinitionContext.getBean("jsonMapper", JsonMapper.class), client.getEndpointConfiguration().getJsonMapper());

    }
}
