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

package org.citrusframework.websocket.config.annotation;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.config.annotation.HttpClientConfigParser;
import org.citrusframework.http.config.annotation.HttpServerConfigParser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.websocket.client.WebSocketClient;
import org.citrusframework.websocket.message.WebSocketMessageConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class WebSocketClientConfigParserTest extends AbstractUnitTest {

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

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();

        lenient().when(referenceResolver.resolve("messageConverter", WebSocketMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("endpointResolver", EndpointUriResolver.class)).thenReturn(endpointResolver);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);

        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testWebSocketClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        Assertions.assertEquals("ws://localhost:8080/test", webSocketClient1.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(5000L, webSocketClient1.getEndpointConfiguration().getTimeout());

        // 2nd message sender
        Assertions.assertEquals("ws://localhost:8080/test/uri", webSocketClient2.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(messageConverter, webSocketClient2.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals(endpointResolver, webSocketClient2.getEndpointConfiguration().getEndpointUriResolver());
        Assertions.assertEquals(10000L, webSocketClient2.getEndpointConfiguration().getTimeout());

        // 3rd message sender
        Assertions.assertNotNull(webSocketClient3.getActor());
        Assertions.assertEquals(testActor, webSocketClient3.getActor());
        Assertions.assertEquals("ws://localhost:8080/test", webSocketClient3.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(250L, webSocketClient3.getEndpointConfiguration().getPollingInterval());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals(6L, validators.size());
        Assertions.assertNotNull(validators.get("direct.async"));
        Assertions.assertEquals(DirectEndpointConfigParser.class, validators.get("direct.async").getClass());
        Assertions.assertNotNull(validators.get("direct.sync"));
        Assertions.assertEquals(DirectSyncEndpointConfigParser.class, validators.get("direct.sync").getClass());
        Assertions.assertNotNull(validators.get("http.client"));
        Assertions.assertEquals(HttpClientConfigParser.class, validators.get("http.client").getClass());
        Assertions.assertNotNull(validators.get("http.server"));
        Assertions.assertEquals(HttpServerConfigParser.class, validators.get("http.server").getClass());
        Assertions.assertNotNull(validators.get("websocket.client"));
        Assertions.assertEquals(WebSocketClientConfigParser.class, validators.get("websocket.client").getClass());
        Assertions.assertNotNull(validators.get("websocket.server"));
        Assertions.assertEquals(WebSocketServerConfigParser.class, validators.get("websocket.server").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("websocket.client").isPresent());
    }
}
