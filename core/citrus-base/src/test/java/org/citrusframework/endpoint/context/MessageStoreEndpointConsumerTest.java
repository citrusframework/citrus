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

package org.citrusframework.endpoint.context;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MessageStoreEndpointConsumerTest {

    private TestContext context;

    @BeforeMethod
    public void setupMocks() {
        context = new TestContext();
    }

    @Test
    public void testReceiveMessage() {
        MessageStoreEndpoint endpoint = new MessageStoreEndpointBuilder()
                .messageName("fooMessage")
                .build();

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("Hello World!", headers);
        context.getMessageStore().storeMessage("fooMessage", message);

        Message receivedMessage = endpoint.createConsumer().receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(MessageHeaders.ID), message.getId());
    }

    @Test
    public void testReceiveMessageDefaultMessageName() {
        MessageStoreEndpoint endpoint = new MessageStoreEndpointBuilder()
                .name("fooEndpoint")
                .build();

        Map<String, Object> headers = new HashMap<>();
        final Message message = new DefaultMessage("Hello World!", headers);
        context.getMessageStore().storeMessage("fooEndpoint.message", message);

        Message receivedMessage = endpoint.createConsumer().receive(context);

        Assert.assertEquals(receivedMessage.getPayload(), message.getPayload());
        Assert.assertEquals(receivedMessage.getHeader(MessageHeaders.ID), message.getId());
    }

    @Test
    public void testReceiveTimeout() {
        MessageStoreEndpoint endpoint = new MessageStoreEndpointBuilder()
                .messageName("fooMessage")
                .build();

        try {
            endpoint.createConsumer().receive(context);
            Assert.fail("Missing " + MessageTimeoutException.class + " because no message was received");
        } catch(MessageTimeoutException e) {
            Assert.assertTrue(e.getLocalizedMessage().startsWith("Action timeout after 5000 milliseconds. Failed to receive message on endpoint"));
        }
    }

}
