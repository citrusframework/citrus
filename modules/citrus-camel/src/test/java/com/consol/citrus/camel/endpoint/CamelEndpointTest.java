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
import com.consol.citrus.exceptions.CitrusRuntimeException;
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
public class CamelEndpointTest extends AbstractTestNGUnitTest {

    private CamelContext camelContext = Mockito.mock(CamelContext.class);
    private ProducerTemplate producerTemplate = Mockito.mock(ProducerTemplate.class);
    private ConsumerTemplate consumerTemplate = Mockito.mock(ConsumerTemplate.class);
    private Exchange exchange = Mockito.mock(Exchange.class);

    private MessageListeners messageListeners = Mockito.mock(MessageListeners.class);

    @Test
    public void testCamelEndpointProducer() {
        String endpointUri = "direct:news-feed";
        CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();
        endpointConfiguration.setCamelContext(camelContext);
        endpointConfiguration.setEndpointUri(endpointUri);

        CamelEndpoint camelEndpoint = new CamelEndpoint(endpointConfiguration);

        Message requestMessage = new com.consol.citrus.message.DefaultMessage("Hello from Citrus!");

        reset(camelContext, producerTemplate, exchange);

        when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
        when(producerTemplate.send(eq(endpointUri), any(Processor.class))).thenReturn(exchange);
        when(exchange.getException()).thenReturn(null);

        camelEndpoint.createProducer().send(requestMessage, context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testCamelEndpointProducerWithInternalException() {
        String endpointUri = "direct:news-feed";
        CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();
        endpointConfiguration.setCamelContext(camelContext);
        endpointConfiguration.setEndpointUri(endpointUri);

        CamelEndpoint camelEndpoint = new CamelEndpoint(endpointConfiguration);

        Message requestMessage = new com.consol.citrus.message.DefaultMessage("Hello from Citrus!");

        CamelExchangeException exchangeException = new CamelExchangeException("Failed", exchange);

        reset(camelContext, producerTemplate, exchange);

        when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
        when(producerTemplate.send(eq(endpointUri), any(Processor.class))).thenReturn(exchange);
        when(exchange.getException()).thenReturn(exchangeException);

        camelEndpoint.createProducer().send(requestMessage, context);
    }

    @Test
    public void testCamelEndpointConsumer() {
        String endpointUri = "direct:news-feed";
        CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();
        endpointConfiguration.setCamelContext(camelContext);
        endpointConfiguration.setEndpointUri(endpointUri);

        CamelEndpoint camelEndpoint = new CamelEndpoint(endpointConfiguration);
        DefaultMessage message = new DefaultMessage();
        message.setBody("Hello from Camel!");
        message.setHeader("operation", "newsFeed");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        reset(camelContext, consumerTemplate);

        when(camelContext.createConsumerTemplate()).thenReturn(consumerTemplate);
        when(camelContext.getUuidGenerator()).thenReturn(new JavaUuidGenerator());
        when(consumerTemplate.receive(endpointUri, endpointConfiguration.getTimeout())).thenReturn(exchange);

        Message receivedMessage = camelEndpoint.createConsumer().receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(receivedMessage.getPayload(), "Hello from Camel!");
        Assert.assertEquals(receivedMessage.getHeader("operation"), "newsFeed");
        Assert.assertNotNull(receivedMessage.getHeader(CitrusCamelMessageHeaders.EXCHANGE_ID));
        Assert.assertNotNull(receivedMessage.getHeader(CitrusCamelMessageHeaders.EXCHANGE_PATTERN));
        Assert.assertNotNull(receivedMessage.getHeader(CitrusCamelMessageHeaders.EXCHANGE_FAILED));
    }

    @Test
    public void testCamelEndpointWithMessageListeners() {
        String endpointUri = "direct:news-feed";
        CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();
        endpointConfiguration.setCamelContext(camelContext);
        endpointConfiguration.setEndpointUri(endpointUri);

        CamelEndpoint camelEndpoint = new CamelEndpoint(endpointConfiguration);

        Message requestMessage = new com.consol.citrus.message.DefaultMessage("Hello from Citrus!");

        DefaultMessage message = new DefaultMessage();
        message.setBody("Hello from Camel!");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        context.setMessageListeners(messageListeners);

        reset(camelContext, producerTemplate, consumerTemplate, messageListeners);

        when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
        when(producerTemplate.send(eq(endpointUri), any(Processor.class))).thenReturn(exchange);

        when(camelContext.createConsumerTemplate()).thenReturn(consumerTemplate);
        when(camelContext.getUuidGenerator()).thenReturn(new JavaUuidGenerator());
        when(consumerTemplate.receive(endpointUri, endpointConfiguration.getTimeout())).thenReturn(exchange);

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
