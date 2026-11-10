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

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.websocket.endpoint.WebSocketEndpoint;
import org.citrusframework.websocket.message.WebSocketMessageConverter;
import org.citrusframework.websocket.server.WebSocketServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class WebSocketServerConfigParserTest extends AbstractUnitTest {

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

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();

        lenient().when(referenceResolver.resolve("messageConverter", WebSocketMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);

        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testWebSocketServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        Assertions.assertEquals("webSocketServer1", webSocketServer1.getName());
        Assertions.assertEquals(8080, webSocketServer1.getPort());
        Assertions.assertFalse(webSocketServer1.isAutoStart());
        Assertions.assertEquals(3, webSocketServer1.getWebSockets().size());

        WebSocketEndpoint webSocketEndpoint = webSocketServer1.getWebSockets().get(0);
        Assertions.assertEquals("websocket1", webSocketEndpoint.getName());
        Assertions.assertEquals("/test1", webSocketEndpoint.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(5000L, webSocketEndpoint.getEndpointConfiguration().getTimeout());

        webSocketEndpoint = webSocketServer1.getWebSockets().get(1);
        Assertions.assertEquals("websocket2", webSocketEndpoint.getName());
        Assertions.assertEquals("/test2", webSocketEndpoint.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(messageConverter, webSocketEndpoint.getEndpointConfiguration().getMessageConverter());

        webSocketEndpoint = webSocketServer1.getWebSockets().get(2);
        Assertions.assertNotNull(webSocketEndpoint.getActor());
        Assertions.assertEquals(testActor, webSocketEndpoint.getActor());
        Assertions.assertEquals("websocket3", webSocketEndpoint.getName());
        Assertions.assertEquals("/test3", webSocketEndpoint.getEndpointConfiguration().getEndpointUri());
        Assertions.assertEquals(10000L, webSocketEndpoint.getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("websocket.server").isPresent());
    }
}
