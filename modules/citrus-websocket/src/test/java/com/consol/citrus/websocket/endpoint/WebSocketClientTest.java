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

package com.consol.citrus.websocket.endpoint;

import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.websocket.handler.CitrusWebSocketHandler;
import com.consol.citrus.websocket.message.WebSocketMessage;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

public class WebSocketClientTest extends AbstractTestNGUnitTest {

    private WebSocketClient client = EasyMock.createMock(WebSocketClient.class);
    private WebSocketSession session = EasyMock.createMock(WebSocketSession.class);

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

        expect(client.doHandshake(anyObject(CitrusWebSocketHandler.class), eq(endpointUri)))
                .andAnswer(new IAnswer<ListenableFuture<WebSocketSession>>() {
                    @Override
                    public ListenableFuture<WebSocketSession> answer() throws Throwable {
                        CitrusWebSocketHandler handler = (CitrusWebSocketHandler) getCurrentArguments()[0];
                        handler.afterConnectionEstablished(session);

                        handler.handleMessage(session, new TextMessage(responseBody));

                        return future;
                    }
                }).once();

        expect(session.getId()).andReturn("test-socket-1").atLeastOnce();
        expect(session.isOpen()).andReturn(true).once();

        session.sendMessage(anyObject(org.springframework.web.socket.WebSocketMessage.class));
        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                org.springframework.web.socket.WebSocketMessage request = (org.springframework.web.socket.WebSocketMessage) getCurrentArguments()[0];

                Assert.assertTrue(TextMessage.class.isInstance(request));
                Assert.assertEquals(((TextMessage)request).getPayload(), requestMessage.getPayload(String.class));
                Assert.assertTrue(request.isLast());
                return null;
            }
        }).once();

        replay(client, session);

        webSocketEndpoint.createProducer().send(requestMessage, context);

        WebSocketMessage responseMessage = (WebSocketMessage) webSocketEndpoint.createConsumer().receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(responseMessage.getPayload(), responseBody);
        Assert.assertTrue(responseMessage.isLast());

        verify(client, session);
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

        expect(client.doHandshake(anyObject(CitrusWebSocketHandler.class), eq(endpointUri)))
                .andAnswer(new IAnswer<ListenableFuture<WebSocketSession>>() {
                    @Override
                    public ListenableFuture<WebSocketSession> answer() throws Throwable {
                        CitrusWebSocketHandler handler = (CitrusWebSocketHandler) getCurrentArguments()[0];
                        handler.afterConnectionEstablished(session);
                        return future;
                    }
                }).once();

        expect(session.getId()).andReturn("test-socket-1").atLeastOnce();
        expect(session.isOpen()).andReturn(true).once();

        session.sendMessage(anyObject(org.springframework.web.socket.WebSocketMessage.class));
        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                org.springframework.web.socket.WebSocketMessage request = (org.springframework.web.socket.WebSocketMessage) getCurrentArguments()[0];

                Assert.assertTrue(TextMessage.class.isInstance(request));
                Assert.assertEquals(((TextMessage)request).getPayload(), requestMessage.getPayload(String.class));
                Assert.assertTrue(request.isLast());
                return null;
            }
        }).once();

        replay(client, session);

        webSocketEndpoint.createProducer().send(requestMessage, context);

        try {
            webSocketEndpoint.createConsumer().receive(context, endpointConfiguration.getTimeout());
            Assert.fail("Missing timeout exception on web socket client");
        } catch (ActionTimeoutException e) {
            Assert.assertTrue(e.getMessage().contains(endpointUri));
        }

        verify(client, session);
    }
}