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

package org.citrusframework.websocket.config.xml;

import org.citrusframework.TestActor;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.websocket.client.WebSocketClientEndpointConfiguration;
import org.citrusframework.websocket.endpoint.WebSocketEndpoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;

import java.util.Map;

public class WebSocketClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testWebSocketClientParser() {
        Map<String, WebSocketEndpoint> clients = beanDefinitionContext.getBeansOfType(WebSocketEndpoint.class);

        Assertions.assertEquals(3, clients.size());

        // 1st message sender
        WebSocketEndpoint webSocketClient = clients.get("webSocketClient1");
        Assertions.assertEquals("ws://localhost:8080/test", webSocketClient.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(5000L, webSocketClient.getEndpointConfiguration().getTimeout());

        // 2nd message sender
        webSocketClient = clients.get("webSocketClient2");
        Assertions.assertEquals("ws://localhost:8080/test/uri", webSocketClient.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(beanDefinitionContext.getBean("webSocketHttpHeaders"), ((WebSocketClientEndpointConfiguration)webSocketClient.getEndpointConfiguration()).getWebSocketHttpHeaders());
        Assertions.assertEquals(beanDefinitionContext.getBean("messageConverter"), webSocketClient.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals(beanDefinitionContext.getBean("endpointResolver"), webSocketClient.getEndpointConfiguration().getEndpointUriResolver());
        Assertions.assertEquals(10000L, webSocketClient.getEndpointConfiguration().getTimeout());

        // 3rd message sender
        webSocketClient = clients.get("webSocketClient3");
        Assertions.assertNotNull(webSocketClient.getActor());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), webSocketClient.getActor());
        Assertions.assertEquals("ws://localhost:8080/test", webSocketClient.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(250L, webSocketClient.getEndpointConfiguration().getPollingInterval());
    }

    @Test
    public void testMissingUrlOrEndpointResolver() {
        try {
            createApplicationContext("failed");
            Assertions.fail("Missing bean creation exception due to missing url or endpoint resolver");
        } catch (BeanDefinitionParsingException e) {
            Assertions.assertTrue(e.getMessage().contains("One of the properties 'url' or 'endpoint-resolver' is required"));
        }
    }
}
