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
import org.citrusframework.websocket.endpoint.WebSocketEndpoint;
import org.citrusframework.websocket.server.WebSocketServer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class WebSocketEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testWebSocketEndpointParser() {
        Map<String, WebSocketServer> servers = beanDefinitionContext.getBeansOfType(WebSocketServer.class);
        
        Assert.assertEquals(servers.size(), 1);
        
        // 1st message sender
        WebSocketServer server = servers.get("webSocketServer1");
        Assert.assertEquals(server.getName(), "webSocketServer1");
        Assert.assertEquals(server.getPort(), 8080);
        Assert.assertFalse(server.isAutoStart());
        Assert.assertEquals(server.getWebSockets().size(), 3);

        WebSocketEndpoint webSocketEndpoint = server.getWebSockets().get(0);
        Assert.assertEquals(webSocketEndpoint.getName(), "websocket1");
        Assert.assertEquals(webSocketEndpoint.getEndpointConfiguration().getEndpointUri(), "/test1");
        Assert.assertEquals(webSocketEndpoint.getEndpointConfiguration().getTimeout(), 5000L);

        webSocketEndpoint = server.getWebSockets().get(1);
        Assert.assertEquals(webSocketEndpoint.getName(), "websocket2");
        Assert.assertEquals(webSocketEndpoint.getEndpointConfiguration().getEndpointUri(), "/test2");
        Assert.assertEquals(webSocketEndpoint.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));

        webSocketEndpoint = server.getWebSockets().get(2);
        Assert.assertNotNull(webSocketEndpoint.getActor());
        Assert.assertEquals(webSocketEndpoint.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
        Assert.assertEquals(webSocketEndpoint.getName(), "websocket3");
        Assert.assertEquals(webSocketEndpoint.getEndpointConfiguration().getEndpointUri(), "/test3");
        Assert.assertEquals(webSocketEndpoint.getEndpointConfiguration().getTimeout(), 10000L);

    }

}
