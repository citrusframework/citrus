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
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxEndpointTest extends AbstractTestNGUnitTest {

    private Vertx vertx = Mockito.mock(Vertx.class);
    private EventBus eventBus = Mockito.mock(EventBus.class);
    private MessageConsumer messageConsumer = Mockito.mock(MessageConsumer.class);
    private MessageListeners messageListeners = Mockito.mock(MessageListeners.class);
    private io.vertx.core.eventbus.Message messageMock = Mockito.mock(io.vertx.core.eventbus.Message.class);

    private SingleVertxInstanceFactory instanceFactory = new SingleVertxInstanceFactory();

    @BeforeClass
    public void setup() {
        instanceFactory.setVertx(vertx);
    }

    @Test
    public void testVertxEndpointProducer() {
        String eventBusAddress = "news-feed";
        VertxEndpointConfiguration endpointConfiguration = new VertxEndpointConfiguration();
        endpointConfiguration.setAddress(eventBusAddress);

        VertxEndpoint vertxEndpoint = new VertxEndpoint(endpointConfiguration);
        vertxEndpoint.setVertxInstanceFactory(instanceFactory);

        Message requestMessage = new DefaultMessage("Hello from Citrus!");

        reset(vertx, eventBus);

        when(vertx.eventBus()).thenReturn(eventBus);
        when(eventBus.send(eventBusAddress, requestMessage.getPayload())).thenReturn(eventBus);

        vertxEndpoint.createProducer().send(requestMessage, context);

    }

    @Test
    public void testVertxEndpointProducerPubSubDomain() {
        String eventBusAddress = "news-feed";
        VertxEndpointConfiguration endpointConfiguration = new VertxEndpointConfiguration();
        endpointConfiguration.setAddress(eventBusAddress);
        endpointConfiguration.setPubSubDomain(true);

        VertxEndpoint vertxEndpoint = new VertxEndpoint(endpointConfiguration);
        vertxEndpoint.setVertxInstanceFactory(instanceFactory);

        Message requestMessage = new DefaultMessage("Hello from Citrus!");

        reset(vertx, eventBus);

        when(vertx.eventBus()).thenReturn(eventBus);
        when(eventBus.publish(eventBusAddress, requestMessage.getPayload())).thenReturn(eventBus);

        vertxEndpoint.createProducer().send(requestMessage, context);

    }

    @Test
    public void testVertxEndpointConsumer() {
        String eventBusAddress = "news-feed";
        VertxEndpointConfiguration endpointConfiguration = new VertxEndpointConfiguration();
        endpointConfiguration.setAddress(eventBusAddress);

        VertxEndpoint vertxEndpoint = new VertxEndpoint(endpointConfiguration);
        vertxEndpoint.setVertxInstanceFactory(instanceFactory);

        reset(vertx, eventBus, messageConsumer, messageMock);

        when(messageMock.body()).thenReturn("Hello from Vertx!");
        when(messageMock.address()).thenReturn(eventBusAddress);
        when(messageMock.replyAddress()).thenReturn("replyAddress");

        when(vertx.eventBus()).thenReturn(eventBus);
        doAnswer(new Answer<MessageConsumer>() {
            @Override
            public MessageConsumer answer(InvocationOnMock invocation) throws Throwable {
                Handler handler = (Handler) invocation.getArguments()[1];
                handler.handle(messageMock);

                return messageConsumer;
            }
        }).when(eventBus).consumer(eq(eventBusAddress), any(Handler.class));

        Message receivedMessage = vertxEndpoint.createConsumer().receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(receivedMessage.getPayload(), "Hello from Vertx!");
        Assert.assertEquals(receivedMessage.getHeader(CitrusVertxMessageHeaders.VERTX_ADDRESS), eventBusAddress);
        Assert.assertEquals(receivedMessage.getHeader(CitrusVertxMessageHeaders.VERTX_REPLY_ADDRESS), "replyAddress");

        verify(messageConsumer).unregister();
    }

    @Test
    public void testVertxEndpointWithOutboundMessageListeners() {
        String eventBusAddress = "news-feed";
        VertxEndpointConfiguration endpointConfiguration = new VertxEndpointConfiguration();
        endpointConfiguration.setAddress(eventBusAddress);

        VertxEndpoint vertxEndpoint = new VertxEndpoint(endpointConfiguration);
        vertxEndpoint.setVertxInstanceFactory(instanceFactory);

        Message requestMessage = new DefaultMessage("Hello from Citrus!");

        context.setMessageListeners(messageListeners);

        reset(vertx, eventBus, messageListeners);

        when(vertx.eventBus()).thenReturn(eventBus);
        when(eventBus.send(eventBusAddress, requestMessage.getPayload())).thenReturn(eventBus);

        when(messageListeners.isEmpty()).thenReturn(false);

        vertxEndpoint.createProducer().send(requestMessage, context);

        verify(messageListeners).onOutboundMessage(requestMessage, context);
    }
}
