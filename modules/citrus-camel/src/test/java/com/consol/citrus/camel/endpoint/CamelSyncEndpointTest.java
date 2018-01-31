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

package com.consol.citrus.camel.endpoint;

import com.consol.citrus.camel.message.CitrusCamelMessageHeaders;
import com.consol.citrus.message.Message;
import com.consol.citrus.report.MessageListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.apache.camel.*;
import org.apache.camel.impl.*;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelSyncEndpointTest extends AbstractTestNGUnitTest {

    private CamelContext camelContext = Mockito.mock(CamelContext.class);
    private ProducerTemplate producerTemplate = Mockito.mock(ProducerTemplate.class);
    private ConsumerTemplate consumerTemplate = Mockito.mock(ConsumerTemplate.class);
    private MessageListeners messageListeners = Mockito.mock(MessageListeners.class);

    @Test
    public void testCamelSyncEndpointProducer() {
        String endpointUri = "direct:news-feed";
        CamelSyncEndpointConfiguration endpointConfiguration = new CamelSyncEndpointConfiguration();
        endpointConfiguration.setCamelContext(camelContext);
        endpointConfiguration.setEndpointUri(endpointUri);

        CamelSyncEndpoint camelEndpoint = new CamelSyncEndpoint(endpointConfiguration);

        Message requestMessage = new com.consol.citrus.message.DefaultMessage("Hello from Citrus!");

        reset(camelContext, producerTemplate);

        when(camelContext.getHeadersMapFactory()).thenReturn(new DefaultHeadersMapFactory());
        when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
        when(camelContext.getUuidGenerator()).thenReturn(new JavaUuidGenerator());

        DefaultMessage message = new DefaultMessage(camelContext);
        message.setBody("Hello from Camel!");
        message.setHeader("operation", "newsFeed");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        when(producerTemplate.request(eq(endpointUri), any(Processor.class))).thenReturn(exchange);

        camelEndpoint.createProducer().send(requestMessage, context);
        Message reply = camelEndpoint.createConsumer().receive(context, 5000L);

        Assert.assertEquals(reply.getPayload(), "Hello from Camel!");
        Assert.assertEquals(reply.getHeader(CitrusCamelMessageHeaders.EXCHANGE_ID), exchange.getExchangeId());
        Assert.assertNotNull(reply.getHeader(CitrusCamelMessageHeaders.EXCHANGE_PATTERN));
        Assert.assertNotNull(reply.getHeader(CitrusCamelMessageHeaders.EXCHANGE_FAILED));
        Assert.assertEquals(reply.getHeader("operation"), "newsFeed");

    }

    @Test
    public void testCamelSyncEndpointConsumer() {
        String endpointUri = "direct:news-feed";
        CamelSyncEndpointConfiguration endpointConfiguration = new CamelSyncEndpointConfiguration();
        endpointConfiguration.setCamelContext(camelContext);
        endpointConfiguration.setEndpointUri(endpointUri);

        CamelSyncEndpoint camelEndpoint = new CamelSyncEndpoint(endpointConfiguration);


        Message replyMessage = new com.consol.citrus.message.DefaultMessage("Hello from Citrus!")
                                                .setHeader("operation", "hello");

        reset(camelContext, consumerTemplate);

        when(camelContext.createConsumerTemplate()).thenReturn(consumerTemplate);
        when(camelContext.getHeadersMapFactory()).thenReturn(new DefaultHeadersMapFactory());
        when(camelContext.getUuidGenerator()).thenReturn(new JavaUuidGenerator());

        DefaultMessage message = new DefaultMessage(camelContext);
        message.setBody("Hello from Camel!");
        message.setHeader("operation", "newsFeed");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        when(consumerTemplate.receive(endpointUri, endpointConfiguration.getTimeout())).thenReturn(exchange);

        Message receivedMessage = camelEndpoint.createConsumer().receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(receivedMessage.getPayload(), "Hello from Camel!");
        Assert.assertEquals(receivedMessage.getHeader("operation"), "newsFeed");
        Assert.assertNotNull(receivedMessage.getHeader(CitrusCamelMessageHeaders.EXCHANGE_ID));
        Assert.assertNotNull(receivedMessage.getHeader(CitrusCamelMessageHeaders.EXCHANGE_PATTERN));
        Assert.assertNotNull(receivedMessage.getHeader(CitrusCamelMessageHeaders.EXCHANGE_FAILED));

        camelEndpoint.createProducer().send(replyMessage, context);

        Assert.assertEquals(exchange.getOut().getBody().toString(), replyMessage.getPayload());
        Assert.assertEquals(exchange.getOut().getHeaders().get("operation"), "hello");

        verify(consumerTemplate).doneUoW(exchange);
    }

    @Test
    public void testCamelSyncEndpointWithMessageListeners() {
        String endpointUri = "direct:news-feed";
        CamelSyncEndpointConfiguration endpointConfiguration = new CamelSyncEndpointConfiguration();
        endpointConfiguration.setCamelContext(camelContext);
        endpointConfiguration.setEndpointUri(endpointUri);

        CamelSyncEndpoint camelEndpoint = new CamelSyncEndpoint(endpointConfiguration);

        Message requestMessage = new com.consol.citrus.message.DefaultMessage("Hello from Citrus!");

        DefaultMessage message = new DefaultMessage(camelContext);
        message.setBody("Hello from Camel!");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        context.setMessageListeners(messageListeners);

        reset(camelContext, producerTemplate, messageListeners);

        when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
        when(camelContext.getHeadersMapFactory()).thenReturn(new DefaultHeadersMapFactory());
        when(camelContext.getUuidGenerator()).thenReturn(new JavaUuidGenerator());
        when(producerTemplate.request(eq(endpointUri), any(Processor.class))).thenReturn(exchange);

        when(messageListeners.isEmpty()).thenReturn(false);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Message inboundMessage = (Message) invocation.getArguments()[0];
                Assert.assertTrue(inboundMessage.getPayload(String.class).contains("Hello from Camel!"));
                return null;
            }
        }).when(messageListeners).onInboundMessage(any(Message.class), eq(context));

        camelEndpoint.createProducer().send(requestMessage, context);
        camelEndpoint.createConsumer().receive(context, 5000L);

        verify(messageListeners).onOutboundMessage(requestMessage, context);
    }
}
