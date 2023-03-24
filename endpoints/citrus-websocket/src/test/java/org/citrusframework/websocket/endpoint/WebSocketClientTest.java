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
import org.citrusframework.websocket.client.WebSocketClientEndpointConfiguration;
import org.citrusframework.websocket.handler.CitrusWebSocketHandler;
import org.citrusframework.websocket.message.WebSocketMessage;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;

import static org.mockito.Mockito.*;

public class WebSocketClientTest extends AbstractTestNGUnitTest {

    private WebSocketClient client = Mockito.mock(WebSocketClient.class);
    private WebSocketSession session = Mockito.mock(WebSocketSession.class);

    @Test
    public void testWebSocketClient() throws Exception {
        WebSocketClientEndpointConfiguration endpointConfiguration = new WebSocketClientEndpointConfiguration();
        WebSocketEndpoint webSocketEndpoint = new WebSocketEndpoint(endpointConfiguration);
        String endpointUri = "ws://localhost:8088/test";

        final String responseBody = "<TestResponse><Message>Hello World!</Message></TestResponse>";

        endpointConfiguration.setEndpointUri(endpointUri);

        final Message requestMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        endpointConfiguration.setClient(client);

        reset(client, session);

        final SettableListenableFuture<WebSocketSession> future = new SettableListenableFuture<>();
        future.set(session);

        doAnswer(new Answer<ListenableFuture<WebSocketSession>>() {
            @Override
            public ListenableFuture<WebSocketSession> answer(InvocationOnMock invocation) throws Throwable {
                CitrusWebSocketHandler handler = (CitrusWebSocketHandler) invocation.getArguments()[0];
                handler.afterConnectionEstablished(session);

                handler.handleMessage(session, new TextMessage(responseBody));

                return future;
            }
        }).when(client).doHandshake(any(CitrusWebSocketHandler.class), any(WebSocketHttpHeaders.class), any(URI.class));

        when(session.getId()).thenReturn("test-socket-1");
        when(session.isOpen()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                org.springframework.web.socket.WebSocketMessage request = (org.springframework.web.socket.WebSocketMessage) invocation.getArguments()[0];

                Assert.assertTrue(TextMessage.class.isInstance(request));
                Assert.assertEquals(((TextMessage)request).getPayload(), requestMessage.getPayload(String.class));
                Assert.assertTrue(request.isLast());
                return null;
            }
        }).when(session).sendMessage(any(org.springframework.web.socket.WebSocketMessage.class));

        webSocketEndpoint.createProducer().send(requestMessage, context);

        WebSocketMessage responseMessage = (WebSocketMessage) webSocketEndpoint.createConsumer().receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertTrue(responseMessage.isLast());

    }

    @Test
    public void testWebSocketClientTimeout() throws Exception {
        WebSocketClientEndpointConfiguration endpointConfiguration = new WebSocketClientEndpointConfiguration();
        WebSocketEndpoint webSocketEndpoint = new WebSocketEndpoint(endpointConfiguration);
        String endpointUri = "ws://localhost:8088/test";

        endpointConfiguration.setEndpointUri(endpointUri);

        final Message requestMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        endpointConfiguration.setClient(client);
        endpointConfiguration.setTimeout(1000L);

        reset(client, session);

        final SettableListenableFuture<WebSocketSession> future = new SettableListenableFuture<>();
        future.set(session);

        doAnswer(new Answer<ListenableFuture<WebSocketSession>>() {
            @Override
            public ListenableFuture<WebSocketSession> answer(InvocationOnMock invocation) throws Throwable {
                CitrusWebSocketHandler handler = (CitrusWebSocketHandler) invocation.getArguments()[0];
                handler.afterConnectionEstablished(session);
                return future;
            }
        }).when(client).doHandshake(any(CitrusWebSocketHandler.class), any(WebSocketHttpHeaders.class), any(URI.class));

        when(session.getId()).thenReturn("test-socket-1");
        when(session.isOpen()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                org.springframework.web.socket.WebSocketMessage request = (org.springframework.web.socket.WebSocketMessage) invocation.getArguments()[0];

                Assert.assertTrue(TextMessage.class.isInstance(request));
                Assert.assertEquals(((TextMessage)request).getPayload(), requestMessage.getPayload(String.class));
                Assert.assertTrue(request.isLast());
                return null;
            }
        }).when(session).sendMessage(any(org.springframework.web.socket.WebSocketMessage.class));

        webSocketEndpoint.createProducer().send(requestMessage, context);

        try {
            webSocketEndpoint.createConsumer().receive(context, endpointConfiguration.getTimeout());
            Assert.fail("Missing timeout exception on web socket client");
        } catch (ActionTimeoutException e) {
            Assert.assertTrue(e.getMessage().contains(endpointUri));
        }

    }
}
