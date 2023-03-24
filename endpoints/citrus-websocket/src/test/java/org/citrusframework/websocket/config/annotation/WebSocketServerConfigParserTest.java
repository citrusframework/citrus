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

package org.citrusframework.websocket.config.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.websocket.endpoint.WebSocketEndpoint;
import org.citrusframework.websocket.message.WebSocketMessageConverter;
import org.citrusframework.websocket.server.WebSocketServer;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private WebSocketMessageConverter messageConverter;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("messageConverter", WebSocketMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
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

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("websocket.server").isPresent());
    }
}
