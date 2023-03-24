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

package org.citrusframework.websocket.endpoint;

import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.websocket.handler.CitrusWebSocketHandler;
import org.citrusframework.websocket.message.WebSocketMessage;
import org.citrusframework.websocket.server.WebSocketServerEndpointConfiguration;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.web.socket.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class WebSocketEndpointTest extends AbstractTestNGUnitTest {

    private WebSocketSession session = Mockito.mock(WebSocketSession.class);
    private WebSocketSession session2 = Mockito.mock(WebSocketSession.class);
    private WebSocketSession session3 = Mockito.mock(WebSocketSession.class);

    @Test
    public void testWebSocketEndpoint() throws Exception {
        WebSocketServerEndpointConfiguration endpointConfiguration = new WebSocketServerEndpointConfiguration();
        WebSocketEndpoint webSocketEndpoint = new WebSocketEndpoint(endpointConfiguration);
        String endpointUri = "/test";

        CitrusWebSocketHandler handler = new CitrusWebSocketHandler();
        endpointConfiguration.setHandler(handler);

        final String requestBody = "<TestRequest><Message>Hello World!</Message></TestRequest>";

        final Message responseMessage = new DefaultMessage("<TestResponse><Message>Hello World!</Message></TestResponse>");

        endpointConfiguration.setEndpointUri(endpointUri);

        reset(session);

        when(session.getId()).thenReturn("test-socket-1");
        when(session.isOpen()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                org.springframework.web.socket.WebSocketMessage request = (org.springframework.web.socket.WebSocketMessage) invocation.getArguments()[0];

                Assert.assertTrue(TextMessage.class.isInstance(request));
                Assert.assertEquals(((TextMessage)request).getPayload(), responseMessage.getPayload(String.class));
                Assert.assertTrue(request.isLast());
                return null;
            }
        }).when(session).sendMessage(any(org.springframework.web.socket.WebSocketMessage.class));

        handler.afterConnectionEstablished(session);
        handler.handleMessage(session, new TextMessage(requestBody));

        WebSocketMessage requestMessage = (WebSocketMessage) webSocketEndpoint.createConsumer().receive(context);
        Assert.assertEquals(requestMessage.getPayload(), requestBody);
        Assert.assertTrue(requestMessage.isLast());

        webSocketEndpoint.createProducer().send(responseMessage, context);

    }

    @Test
    public void testWebSocketEndpointMultipleSessions() throws Exception {
        WebSocketServerEndpointConfiguration endpointConfiguration = new WebSocketServerEndpointConfiguration();
        WebSocketEndpoint webSocketEndpoint = new WebSocketEndpoint(endpointConfiguration);
        String endpointUri = "/test";

        CitrusWebSocketHandler handler = new CitrusWebSocketHandler();
        endpointConfiguration.setHandler(handler);

        final String requestBody = "<TestRequest><Message>Hello World!</Message></TestRequest>";

        final Message responseMessage = new DefaultMessage("<TestResponse><Message>Hello World!</Message></TestResponse>");

        endpointConfiguration.setEndpointUri(endpointUri);

        reset(session, session2, session3);

        when(session.getId()).thenReturn("test-socket-1");
        when(session2.getId()).thenReturn("test-socket-2");
        when(session3.getId()).thenReturn("test-socket-3");
        when(session.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                org.springframework.web.socket.WebSocketMessage request = (org.springframework.web.socket.WebSocketMessage) invocation.getArguments()[0];

                Assert.assertTrue(TextMessage.class.isInstance(request));
                Assert.assertEquals(((TextMessage)request).getPayload(), responseMessage.getPayload(String.class));
                Assert.assertTrue(request.isLast());
                return null;
            }
        }).when(session).sendMessage(any(org.springframework.web.socket.WebSocketMessage.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                org.springframework.web.socket.WebSocketMessage request = (org.springframework.web.socket.WebSocketMessage) invocation.getArguments()[0];

                Assert.assertTrue(TextMessage.class.isInstance(request));
                Assert.assertEquals(((TextMessage)request).getPayload(), responseMessage.getPayload(String.class));
                Assert.assertTrue(request.isLast());
                return null;
            }
        }).when(session2).sendMessage(any(org.springframework.web.socket.WebSocketMessage.class));

        handler.afterConnectionEstablished(session);
        handler.afterConnectionEstablished(session2);
        handler.afterConnectionEstablished(session3);
        handler.afterConnectionClosed(session3, CloseStatus.NORMAL);

        handler.handleMessage(session, new TextMessage(requestBody));

        WebSocketMessage requestMessage = (WebSocketMessage) webSocketEndpoint.createConsumer().receive(context);
        Assert.assertEquals(requestMessage.getPayload(), requestBody);
        Assert.assertTrue(requestMessage.isLast());

        webSocketEndpoint.createProducer().send(responseMessage, context);

    }

    @Test
    public void testWebSocketEndpointTimeout() throws Exception {
        WebSocketServerEndpointConfiguration endpointConfiguration = new WebSocketServerEndpointConfiguration();
        WebSocketEndpoint webSocketEndpoint = new WebSocketEndpoint(endpointConfiguration);
        String endpointUri = "/test";

        CitrusWebSocketHandler handler = new CitrusWebSocketHandler();
        endpointConfiguration.setHandler(handler);
        endpointConfiguration.setEndpointUri(endpointUri);
        endpointConfiguration.setTimeout(1000L);

        reset(session);
        when(session.getId()).thenReturn("test-socket-1");
        handler.afterConnectionEstablished(session);

        try {
            webSocketEndpoint.createConsumer().receive(context, endpointConfiguration.getTimeout());
            Assert.fail("Missing timeout exception on web socket endpoint");
        } catch (ActionTimeoutException e) {
            Assert.assertTrue(e.getMessage().contains(endpointUri));
        }

    }
}
