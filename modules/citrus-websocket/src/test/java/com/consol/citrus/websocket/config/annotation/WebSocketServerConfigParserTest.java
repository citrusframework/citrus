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

package com.consol.citrus.websocket.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.websocket.endpoint.WebSocketEndpoint;
import com.consol.citrus.websocket.message.WebSocketMessageConverter;
import com.consol.citrus.websocket.server.WebSocketServer;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class WebSocketServerConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "webSocketServer1")
    @WebSocketServerConfig(autoStart=false,
            port=8080,
            timeout = 3000,
            actor = "testActor",
            webSockets = { @WebSocketConfig(id="websocket1", path="/test1"),
                    @WebSocketConfig(id="websocket2", path="/test2", messageConverter = "messageConverter"),
                    @WebSocketConfig(id="websocket3", path="/test3", timeout = 10000L)
            })
    private WebSocketServer webSocketServer1;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private WebSocketMessageConverter messageConverter = Mockito.mock(WebSocketMessageConverter.class);
    @Mock
    private EndpointAdapter endpointAdapter = Mockito.mock(EndpointAdapter.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("messageConverter", WebSocketMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testWebSocketServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        Assert.assertEquals(webSocketServer1.getName(), "webSocketServer1");
        Assert.assertEquals(webSocketServer1.getPort(), 8080);
        Assert.assertFalse(webSocketServer1.isAutoStart());
        Assert.assertEquals(webSocketServer1.getWebSockets().size(), 3);

        WebSocketEndpoint webSocketEndpoint = webSocketServer1.getWebSockets().get(0);
        Assert.assertEquals(webSocketEndpoint.getName(), "websocket1");
        Assert.assertEquals(webSocketEndpoint.getEndpointConfiguration().getEndpointUri(), "/test1");
        Assert.assertEquals(webSocketEndpoint.getEndpointConfiguration().getTimeout(), 5000L);

        webSocketEndpoint = webSocketServer1.getWebSockets().get(1);
        Assert.assertEquals(webSocketEndpoint.getName(), "websocket2");
        Assert.assertEquals(webSocketEndpoint.getEndpointConfiguration().getEndpointUri(), "/test2");
        Assert.assertEquals(webSocketEndpoint.getEndpointConfiguration().getMessageConverter(), messageConverter);

        webSocketEndpoint = webSocketServer1.getWebSockets().get(2);
        Assert.assertNotNull(webSocketEndpoint.getActor());
        Assert.assertEquals(webSocketEndpoint.getActor(), testActor);
        Assert.assertEquals(webSocketEndpoint.getName(), "websocket3");
        Assert.assertEquals(webSocketEndpoint.getEndpointConfiguration().getEndpointUri(), "/test3");
        Assert.assertEquals(webSocketEndpoint.getEndpointConfiguration().getTimeout(), 10000L);
    }
}
