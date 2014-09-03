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
import com.consol.citrus.report.MessageListeners;
import org.apache.camel.*;
import org.apache.camel.impl.*;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.messaging.Message;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelEndpointTest {

    private CamelContext camelContext = EasyMock.createMock(CamelContext.class);
    private ProducerTemplate producerTemplate = EasyMock.createMock(ProducerTemplate.class);
    private ConsumerTemplate consumerTemplate = EasyMock.createMock(ConsumerTemplate.class);

    private MessageListeners messageListeners = EasyMock.createMock(MessageListeners.class);

    @Test
    public void testCamelEndpointProducer() {
        String endpointUri = "direct:news-feed";
        CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();
        endpointConfiguration.setCamelContext(camelContext);
        endpointConfiguration.setEndpointUri(endpointUri);

        CamelEndpoint camelEndpoint = new CamelEndpoint(endpointConfiguration);

        Message<?> requestMessage = MessageBuilder.withPayload("Hello from Citrus!").build();

        reset(camelContext, producerTemplate);

        expect(camelContext.createProducerTemplate()).andReturn(producerTemplate).once();
        producerTemplate.sendBodyAndHeaders(eq(endpointUri), eq(requestMessage.getPayload()), anyObject(Map.class));
        expectLastCall().once();

        replay(camelContext, producerTemplate);

        camelEndpoint.createProducer().send(requestMessage);

        verify(camelContext, producerTemplate);
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

        Message receivedMessage = camelEndpoint.createConsumer().receive(endpointConfiguration.getTimeout());
        Assert.assertEquals(receivedMessage.getPayload(), "Hello from Camel!");
        Assert.assertEquals(receivedMessage.getHeaders().get("operation"), "newsFeed");
        Assert.assertTrue(receivedMessage.getHeaders().containsKey(CitrusCamelMessageHeaders.EXCHANGE_ID));
        Assert.assertTrue(receivedMessage.getHeaders().containsKey(CitrusCamelMessageHeaders.EXCHANGE_PATTERN));
        Assert.assertTrue(receivedMessage.getHeaders().containsKey(CitrusCamelMessageHeaders.EXCHANGE_FAILED));

        verify(camelContext, consumerTemplate);
    }

    @Test
    public void testCamelEndpointWithMessageListeners() {
        String endpointUri = "direct:news-feed";
        CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();
        endpointConfiguration.setCamelContext(camelContext);
        endpointConfiguration.setEndpointUri(endpointUri);

        CamelEndpoint camelEndpoint = new CamelEndpoint(endpointConfiguration);
        camelEndpoint.setMessageListener(messageListeners);

        Message<?> requestMessage = MessageBuilder.withPayload("Hello from Citrus!").build();

        DefaultMessage message = new DefaultMessage();
        message.setBody("Hello from Camel!");
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setIn(message);

        reset(camelContext, producerTemplate, consumerTemplate, messageListeners);

        expect(camelContext.createProducerTemplate()).andReturn(producerTemplate).once();
        producerTemplate.sendBodyAndHeaders(eq(endpointUri), eq(requestMessage.getPayload()), anyObject(Map.class));
        expectLastCall().once();

        expect(camelContext.createConsumerTemplate()).andReturn(consumerTemplate).once();
        expect(camelContext.getUuidGenerator()).andReturn(new JavaUuidGenerator()).once();
        expect(consumerTemplate.receive(endpointUri, endpointConfiguration.getTimeout())).andReturn(exchange).once();

        messageListeners.onOutboundMessage(requestMessage.toString());
        messageListeners.onInboundMessage(anyObject(String.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                String inboundMessage = (String) getCurrentArguments()[0];
                Assert.assertTrue(inboundMessage.contains("Hello from Camel!"));
                return null;
            }
        }).once();

        replay(camelContext, producerTemplate, consumerTemplate, messageListeners);

        camelEndpoint.createProducer().send(requestMessage);
        camelEndpoint.createConsumer().receive(5000L);

        verify(camelContext, producerTemplate, consumerTemplate, messageListeners);
    }
}
