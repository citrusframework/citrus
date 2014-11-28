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
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelEndpointTest extends AbstractTestNGUnitTest {

    private CamelContext camelContext = EasyMock.createMock(CamelContext.class);
    private ProducerTemplate producerTemplate = EasyMock.createMock(ProducerTemplate.class);
    private ConsumerTemplate consumerTemplate = EasyMock.createMock(ConsumerTemplate.class);
    private Exchange exchange = EasyMock.createMock(Exchange.class);

    private MessageListeners messageListeners = EasyMock.createMock(MessageListeners.class);

    @Test
    public void testCamelEndpointProducer() {
        String endpointUri = "direct:news-feed";
        CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();
        endpointConfiguration.setCamelContext(camelContext);
        endpointConfiguration.setEndpointUri(endpointUri);

        CamelEndpoint camelEndpoint = new CamelEndpoint(endpointConfiguration);

        Message requestMessage = new com.consol.citrus.message.DefaultMessage("Hello from Citrus!");

        reset(camelContext, producerTemplate, exchange);

        expect(camelContext.createProducerTemplate()).andReturn(producerTemplate).once();
        expect(producerTemplate.send(eq(endpointUri), anyObject(Processor.class))).andReturn(exchange).once();
        expect(exchange.getException()).andReturn(null).once();

        replay(camelContext, producerTemplate, exchange);

        camelEndpoint.createProducer().send(requestMessage, context);

        verify(camelContext, producerTemplate, exchange);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testCamelEndpointProducerWithInternalException() {
        String endpointUri = "direct:news-feed";
        CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();
        endpointConfiguration.setCamelContext(camelContext);
        endpointConfiguration.setEndpointUri(endpointUri);

        CamelEndpoint camelEndpoint = new CamelEndpoint(endpointConfiguration);

        Message requestMessage = new com.consol.citrus.message.DefaultMessage("Hello from Citrus!");

        reset(camelContext, producerTemplate, exchange);

        expect(camelContext.createProducerTemplate()).andReturn(producerTemplate).once();
        expect(producerTemplate.send(eq(endpointUri), anyObject(Processor.class))).andReturn(exchange).once();
        expect(exchange.getException()).andReturn(new CamelExchangeException("Failed", exchange)).times(2);

        replay(camelContext, producerTemplate, exchange);

        camelEndpoint.createProducer().send(requestMessage, context);

        verify(camelContext, producerTemplate, exchange);
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

        expect(camelContext.createConsumerTemplate()).andReturn(consumerTemplate).once();
        expect(camelContext.getUuidGenerator()).andReturn(new JavaUuidGenerator()).once();
        expect(consumerTemplate.receive(endpointUri, endpointConfiguration.getTimeout())).andReturn(exchange).once();

        replay(camelContext, consumerTemplate);

        Message receivedMessage = camelEndpoint.createConsumer().receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(receivedMessage.getPayload(), "Hello from Camel!");
        Assert.assertEquals(receivedMessage.getHeader("operation"), "newsFeed");
        Assert.assertNotNull(receivedMessage.getHeader(CitrusCamelMessageHeaders.EXCHANGE_ID));
        Assert.assertNotNull(receivedMessage.getHeader(CitrusCamelMessageHeaders.EXCHANGE_PATTERN));
        Assert.assertNotNull(receivedMessage.getHeader(CitrusCamelMessageHeaders.EXCHANGE_FAILED));

        verify(camelContext, consumerTemplate);
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

        expect(camelContext.createProducerTemplate()).andReturn(producerTemplate).once();
        expect(producerTemplate.send(eq(endpointUri), anyObject(Processor.class))).andReturn(exchange).once();

        expect(camelContext.createConsumerTemplate()).andReturn(consumerTemplate).once();
        expect(camelContext.getUuidGenerator()).andReturn(new JavaUuidGenerator()).once();
        expect(consumerTemplate.receive(endpointUri, endpointConfiguration.getTimeout())).andReturn(exchange).once();

        expect(messageListeners.isEmpty()).andReturn(false).times(2);
        messageListeners.onOutboundMessage(requestMessage, context);
        expectLastCall().once();
        messageListeners.onInboundMessage(anyObject(Message.class), eq(context));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message inboundMessage = (Message) getCurrentArguments()[0];
                Assert.assertTrue(inboundMessage.getPayload(String.class).contains("Hello from Camel!"));
                return null;
            }
        }).once();

        replay(camelContext, producerTemplate, consumerTemplate, messageListeners);

        camelEndpoint.createProducer().send(requestMessage, context);
        camelEndpoint.createConsumer().receive(context, 5000L);

        verify(camelContext, producerTemplate, consumerTemplate, messageListeners);
    }
}
