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
import org.citrusframework.websocket.endpoint.WebSocketEndpoint;
import org.citrusframework.websocket.server.WebSocketServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class WebSocketEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testWebSocketEndpointParser() {
        Map<String, WebSocketServer> servers = beanDefinitionContext.getBeansOfType(WebSocketServer.class);

        Assertions.assertEquals(1, servers.size());

        // 1st message sender
        WebSocketServer server = servers.get("webSocketServer1");
        Assertions.assertEquals("webSocketServer1", server.getName());
        Assertions.assertEquals(8080, server.getPort());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertEquals(3, server.getWebSockets().size());

        WebSocketEndpoint webSocketEndpoint = server.getWebSockets().get(0);
        Assertions.assertEquals("websocket1", webSocketEndpoint.getName());
        Assertions.assertEquals("/test1", webSocketEndpoint.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(5000L, webSocketEndpoint.getEndpointConfiguration().getTimeout());

        webSocketEndpoint = server.getWebSockets().get(1);
        Assertions.assertEquals("websocket2", webSocketEndpoint.getName());
        Assertions.assertEquals("/test2", webSocketEndpoint.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(beanDefinitionContext.getBean("messageConverter"), webSocketEndpoint.getEndpointConfiguration().getMessageConverter());

        webSocketEndpoint = server.getWebSockets().get(2);
        Assertions.assertNotNull(webSocketEndpoint.getActor());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), webSocketEndpoint.getActor());
        Assertions.assertEquals("websocket3", webSocketEndpoint.getName());
        Assertions.assertEquals("/test3", webSocketEndpoint.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(10000L, webSocketEndpoint.getEndpointConfiguration().getTimeout());

    }
}
