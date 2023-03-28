/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.vertx.endpoint;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.vertx.factory.SingleVertxInstanceFactory;
import org.citrusframework.vertx.message.CitrusVertxMessageHeaders;
import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxSyncEndpointTest extends AbstractTestNGUnitTest {

    private Vertx vertx = Mockito.mock(Vertx.class);
    private EventBus eventBus = Mockito.mock(EventBus.class);
    private MessageConsumer messageConsumer = Mockito.mock(MessageConsumer.class);
    private MessageListeners messageListeners = Mockito.mock(MessageListeners.class);
    private AsyncResult asyncResult = Mockito.mock(AsyncResult.class);
    private io.vertx.core.eventbus.Message messageMock = Mockito.mock(io.vertx.core.eventbus.Message.class);

    private final SingleVertxInstanceFactory instanceFactory = new SingleVertxInstanceFactory();

    @BeforeClass
    public void setup() {
        instanceFactory.setVertx(vertx);
    }

    @Test
    public void testVertxSyncEndpointProducer() {
        String eventBusAddress = "news-feed";
        VertxSyncEndpointConfiguration endpointConfiguration = new VertxSyncEndpointConfiguration();
        endpointConfiguration.setAddress(eventBusAddress);

        VertxSyncEndpoint vertxEndpoint = new VertxSyncEndpoint(endpointConfiguration);
        vertxEndpoint.setVertxInstanceFactory(instanceFactory);

        Message requestMessage = new DefaultMessage("Hello from Citrus!");

        reset(vertx, eventBus, messageMock, asyncResult);

        when(asyncResult.result()).thenReturn(messageMock);

        when(vertx.eventBus()).thenReturn(eventBus);
        doAnswer(new Answer<EventBus>() {
            @Override
            public EventBus answer(InvocationOnMock invocation) throws Throwable {
                Handler<AsyncResult> handler = (Handler<AsyncResult>) invocation.getArguments()[3];
                handler.handle(asyncResult);
                return eventBus;
            }
        }).when(eventBus).request(eq(eventBusAddress), eq(requestMessage.getPayload()), any(DeliveryOptions.class), any(Handler.class));

        when(messageMock.body()).thenReturn("Hello from Vertx!");
        when(messageMock.address()).thenReturn(eventBusAddress);
        when(messageMock.replyAddress()).thenReturn("replyAddress");

        vertxEndpoint.createProducer().send(requestMessage, context);
        Message reply = vertxEndpoint.createConsumer().receive(context, 5000L);

        Assert.assertEquals(reply.getPayload(), "Hello from Vertx!");
        Assert.assertEquals(reply.getHeader(CitrusVertxMessageHeaders.VERTX_ADDRESS), eventBusAddress);
        Assert.assertEquals(reply.getHeader(CitrusVertxMessageHeaders.VERTX_REPLY_ADDRESS), "replyAddress");

    }

    @Test
    public void testVertxSyncEndpointConsumer() {
        String eventBusAddress = "news-feed";
        VertxSyncEndpointConfiguration endpointConfiguration = new VertxSyncEndpointConfiguration();
        endpointConfiguration.setAddress(eventBusAddress);

        VertxSyncEndpoint vertxEndpoint = new VertxSyncEndpoint(endpointConfiguration);
        vertxEndpoint.setVertxInstanceFactory(instanceFactory);

        Message replyMessage = new DefaultMessage("Hello from Citrus!");

        reset(vertx, eventBus, messageConsumer, messageMock);

        when(messageMock.body()).thenReturn("Hello from Vertx!");
        when(messageMock.address()).thenReturn(eventBusAddress);
        when(messageMock.replyAddress()).thenReturn("replyAddress");

        when(vertx.eventBus()).thenReturn(eventBus);
        doAnswer((Answer<MessageConsumer<?>>) invocation -> {
            Handler<io.vertx.core.eventbus.Message<?>> handler = (Handler<io.vertx.core.eventbus.Message<?>>) invocation.getArguments()[1];
            handler.handle(messageMock);

            return messageConsumer;
        }).when(eventBus).consumer(eq(eventBusAddress), any(Handler.class));

        when(eventBus.send("replyAddress", replyMessage.getPayload())).thenReturn(eventBus);

        Message receivedMessage = vertxEndpoint.createConsumer().receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(receivedMessage.getPayload(), "Hello from Vertx!");
        Assert.assertEquals(receivedMessage.getHeader(CitrusVertxMessageHeaders.VERTX_ADDRESS), eventBusAddress);
        Assert.assertEquals(receivedMessage.getHeader(CitrusVertxMessageHeaders.VERTX_REPLY_ADDRESS), "replyAddress");

        vertxEndpoint.createProducer().send(replyMessage, context);

        verify(messageConsumer).unregister();
    }

    @Test
    public void testVertxSyncEndpointWithOutboundMessageListeners() {
        String eventBusAddress = "news-feed";
        VertxSyncEndpointConfiguration endpointConfiguration = new VertxSyncEndpointConfiguration();
        endpointConfiguration.setAddress(eventBusAddress);

        VertxSyncEndpoint vertxEndpoint = new VertxSyncEndpoint(endpointConfiguration);
        vertxEndpoint.setVertxInstanceFactory(instanceFactory);

        Message requestMessage = new DefaultMessage("Hello from Citrus!");

        context.setMessageListeners(messageListeners);

        reset(vertx, eventBus, messageListeners);

        when(vertx.eventBus()).thenReturn(eventBus);
        when(eventBus.request(eq(eventBusAddress), eq(requestMessage.getPayload()), any(DeliveryOptions.class), any(Handler.class))).thenReturn(eventBus);

        when(messageListeners.isEmpty()).thenReturn(false);
        vertxEndpoint.createProducer().send(requestMessage, context);

        verify(messageListeners).onOutboundMessage(requestMessage, context);
    }
}
