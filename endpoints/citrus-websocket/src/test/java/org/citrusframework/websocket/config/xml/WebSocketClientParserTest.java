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

package org.citrusframework.websocket.config.xml;

import org.citrusframework.TestActor;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.citrusframework.websocket.client.WebSocketClientEndpointConfiguration;
import org.citrusframework.websocket.endpoint.WebSocketEndpoint;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class WebSocketClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testWebSocketClientParser() {
        Map<String, WebSocketEndpoint> clients = beanDefinitionContext.getBeansOfType(WebSocketEndpoint.class);

        Assert.assertEquals(clients.size(), 3);

        // 1st message sender
        WebSocketEndpoint webSocketClient = clients.get("webSocketClient1");
        Assert.assertEquals(webSocketClient.getEndpointConfiguration().getEndpointUri(), "ws://localhost:8080/test");
        Assert.assertEquals(webSocketClient.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd message sender
        webSocketClient = clients.get("webSocketClient2");
        Assert.assertEquals(webSocketClient.getEndpointConfiguration().getEndpointUri(), "ws://localhost:8080/test/uri");
        Assert.assertEquals(((WebSocketClientEndpointConfiguration)webSocketClient.getEndpointConfiguration()).getWebSocketHttpHeaders(), beanDefinitionContext.getBean("webSocketHttpHeaders"));
        Assert.assertEquals(webSocketClient.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        Assert.assertEquals(webSocketClient.getEndpointConfiguration().getEndpointUriResolver(), beanDefinitionContext.getBean("endpointResolver"));
        Assert.assertEquals(webSocketClient.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd message sender
        webSocketClient = clients.get("webSocketClient3");
        Assert.assertNotNull(webSocketClient.getActor());
        Assert.assertEquals(webSocketClient.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
        Assert.assertEquals(webSocketClient.getEndpointConfiguration().getEndpointUri(), "ws://localhost:8080/test");
        Assert.assertEquals(webSocketClient.getEndpointConfiguration().getPollingInterval(), 250L);
    }

    @Test
    public void testMissingUrlOrEndpointResolver() {
        try {
            createApplicationContext("failed");
            Assert.fail("Missing bean creation exception due to missing url or endpoint resolver");
        } catch (BeanDefinitionParsingException e) {
            Assert.assertTrue(e.getMessage().contains("One of the properties 'url' or 'endpoint-resolver' is required"));
        }
    }

}
