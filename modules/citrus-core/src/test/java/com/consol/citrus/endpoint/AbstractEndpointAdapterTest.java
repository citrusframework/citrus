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

package com.consol.citrus.endpoint;

import com.consol.citrus.message.MessageHandler;
import org.easymock.EasyMock;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class AbstractEndpointAdapterTest {

    /** Message handler mock */
    private MessageHandler messageHandler = EasyMock.createMock(MessageHandler.class);

    @Test
    public void testEndpointAdapter() {
        AbstractEndpointAdapter abstractEndpointAdapter = new AbstractEndpointAdapter() {
            @Override
            protected Message<?> handleMessageInternal(Message<?> message) {
                return null;
            }

            @Override
            public Endpoint getEndpoint() {
                return null;
            }

            @Override
            public EndpointConfiguration getEndpointConfiguration() {
                return null;
            }
        };

        Assert.assertNull(abstractEndpointAdapter.handleMessage(MessageBuilder.withPayload("<TestMessage><text>Hi!</text></TestMessage>").build()));
    }

    @Test
    public void testFallbackMessageHandler() {
        AbstractEndpointAdapter abstractEndpointAdapter = new AbstractEndpointAdapter() {
            @Override
            protected Message<?> handleMessageInternal(Message<?> message) {
                return null;
            }

            @Override
            public Endpoint getEndpoint() {
                return null;
            }

            @Override
            public EndpointConfiguration getEndpointConfiguration() {
                return null;
            }
        };

        Message<String> request = MessageBuilder.withPayload("<TestMessage><text>Hi!</text></TestMessage>").build();

        reset(messageHandler);
        expect(messageHandler.handleMessage(request)).andReturn((Message) MessageBuilder.withPayload("OK").build()).once();
        replay(messageHandler);

        abstractEndpointAdapter.setFallbackMessageHandler(messageHandler);
        Message<?> response = abstractEndpointAdapter.handleMessage(request);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getPayload().toString(), "OK");

        verify(messageHandler);
    }
}
