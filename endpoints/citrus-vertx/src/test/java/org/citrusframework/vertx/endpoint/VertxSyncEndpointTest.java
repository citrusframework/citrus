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

package org.citrusframework.vertx.endpoint;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.vertx.factory.SingleVertxInstanceFactory;
import org.citrusframework.vertx.message.CitrusVertxMessageHeaders;
import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

import static org.mockito.Mockito.*;

/**
 * @since 1.4.1
 */
@SuppressWarnings("unchecked")
public class VertxSyncEndpointTest extends AbstractUnitTest {

    private Vertx vertx = Mockito.mock(Vertx.class);
    private EventBus eventBus = Mockito.mock(EventBus.class);
    private MessageConsumer messageConsumer = Mockito.mock(MessageConsumer.class);
    private MessageListeners messageListeners = Mockito.mock(MessageListeners.class);
    private AsyncResult asyncResult = Mockito.mock(AsyncResult.class);
    private io.vertx.core.eventbus.Message messageMock = Mockito.mock(io.vertx.core.eventbus.Message.class);

    private final SingleVertxInstanceFactory instanceFactory = new SingleVertxInstanceFactory();

    @BeforeEach
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
        Future future = Mockito.mock(Future.class);
        when(eventBus.request(eq(eventBusAddress), eq(requestMessage.getPayload()), any(DeliveryOptions.class))).thenReturn(future);
        doAnswer((Answer<Future>) invocation -> {
            Handler<AsyncResult> handler = (Handler<AsyncResult>) invocation.getArguments()[0];
            handler.handle(asyncResult);
            return future;
        }).when(future).onComplete(any(Handler.class));

        when(messageMock.body()).thenReturn("Hello from Vertx!");
        when(messageMock.address()).thenReturn(eventBusAddress);
        when(messageMock.replyAddress()).thenReturn("replyAddress");

        vertxEndpoint.createProducer().send(requestMessage, context);
        Message reply = vertxEndpoint.createConsumer().receive(context, 5000L);

        Assertions.assertEquals("Hello from Vertx!", reply.getPayload());
        Assertions.assertEquals(eventBusAddress, reply.getHeader(CitrusVertxMessageHeaders.VERTX_ADDRESS));
        Assertions.assertEquals("replyAddress", reply.getHeader(CitrusVertxMessageHeaders.VERTX_REPLY_ADDRESS));

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
        when(eventBus.consumer(eventBusAddress)).thenReturn(messageConsumer);
        doAnswer((Answer<MessageConsumer<?>>) invocation -> {
            Handler<io.vertx.core.eventbus.Message<?>> handler = (Handler<io.vertx.core.eventbus.Message<?>>) invocation.getArguments()[0];
            handler.handle(messageMock);
            return messageConsumer;
        }).when(messageConsumer).handler(any(Handler.class));

        when(eventBus.send("replyAddress", replyMessage.getPayload())).thenReturn(eventBus);

        Message receivedMessage = vertxEndpoint.createConsumer().receive(context, endpointConfiguration.getTimeout());
        Assertions.assertEquals("Hello from Vertx!", receivedMessage.getPayload());
        Assertions.assertEquals(eventBusAddress, receivedMessage.getHeader(CitrusVertxMessageHeaders.VERTX_ADDRESS));
        Assertions.assertEquals("replyAddress", receivedMessage.getHeader(CitrusVertxMessageHeaders.VERTX_REPLY_ADDRESS));

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
        Future future = Mockito.mock(Future.class);
        when(eventBus.request(eq(eventBusAddress), eq(requestMessage.getPayload()), any(DeliveryOptions.class))).thenReturn(future);

        when(messageListeners.isEmpty()).thenReturn(false);
        vertxEndpoint.createProducer().send(requestMessage, context);

        verify(messageListeners).onOutboundMessage(requestMessage, context);
    }
}
