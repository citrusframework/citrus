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
import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.websocket.client.WebSocketClient;
import com.consol.citrus.websocket.message.WebSocketMessageConverter;
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
public class WebSocketClientConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "webSocketClient1")
    @WebSocketClientConfig(requestUrl = "ws://localhost:8080/test")
    private WebSocketClient webSocketClient1;

    @CitrusEndpoint
    @WebSocketClientConfig(requestUrl = "ws://localhost:8080/test/uri",
            timeout=10000L,
            messageConverter="messageConverter",
            endpointResolver="endpointResolver")
    private WebSocketClient webSocketClient2;

    @CitrusEndpoint
    @WebSocketClientConfig(requestUrl = "ws://localhost:8080/test",
            pollingInterval=250,
            actor="testActor")
    private WebSocketClient webSocketClient3;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private WebSocketMessageConverter messageConverter = Mockito.mock(WebSocketMessageConverter.class);
    @Mock
    private EndpointUriResolver endpointResolver = Mockito.mock(EndpointUriResolver.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("messageConverter", WebSocketMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("endpointResolver", EndpointUriResolver.class)).thenReturn(endpointResolver);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testWebSocketClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        Assert.assertEquals(webSocketClient1.getEndpointConfiguration().getEndpointUri(), "ws://localhost:8080/test");
        Assert.assertEquals(webSocketClient1.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd message sender
        Assert.assertEquals(webSocketClient2.getEndpointConfiguration().getEndpointUri(), "ws://localhost:8080/test/uri");
        Assert.assertEquals(webSocketClient2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(webSocketClient2.getEndpointConfiguration().getEndpointUriResolver(), endpointResolver);
        Assert.assertEquals(webSocketClient2.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd message sender
        Assert.assertNotNull(webSocketClient3.getActor());
        Assert.assertEquals(webSocketClient3.getActor(), testActor);
        Assert.assertEquals(webSocketClient3.getEndpointConfiguration().getEndpointUri(), "ws://localhost:8080/test");
        Assert.assertEquals(webSocketClient3.getEndpointConfiguration().getPollingInterval(), 250L);
    }
}
