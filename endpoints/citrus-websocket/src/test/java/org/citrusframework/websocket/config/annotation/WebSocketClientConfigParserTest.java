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

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.config.annotation.HttpClientConfigParser;
import org.citrusframework.http.config.annotation.HttpServerConfigParser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.websocket.client.WebSocketClient;
import org.citrusframework.websocket.message.WebSocketMessageConverter;
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

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private WebSocketMessageConverter messageConverter;
    @Mock
    private EndpointUriResolver endpointResolver;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("messageConverter", WebSocketMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("endpointResolver", EndpointUriResolver.class)).thenReturn(endpointResolver);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
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

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 6L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("http.client"));
        Assert.assertEquals(validators.get("http.client").getClass(), HttpClientConfigParser.class);
        Assert.assertNotNull(validators.get("http.server"));
        Assert.assertEquals(validators.get("http.server").getClass(), HttpServerConfigParser.class);
        Assert.assertNotNull(validators.get("websocket.client"));
        Assert.assertEquals(validators.get("websocket.client").getClass(), WebSocketClientConfigParser.class);
        Assert.assertNotNull(validators.get("websocket.server"));
        Assert.assertEquals(validators.get("websocket.server").getClass(), WebSocketServerConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("websocket.client").isPresent());
    }
}
