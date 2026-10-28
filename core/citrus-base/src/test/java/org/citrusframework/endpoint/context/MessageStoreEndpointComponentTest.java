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

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MessageStoreEndpointComponentTest {

    private TestContext context;

    @BeforeEach
    public void setupMocks() {
        context = TestContextFactory.newInstance().getObject();
    }

    @Test
    public void shouldHandleNoParameters() {
        MessageStoreEndpointComponent component = new MessageStoreEndpointComponent();

        Endpoint endpoint = component.createEndpoint("message-store", context);

        Assertions.assertEquals(MessageStoreEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(5000L, ((MessageStoreEndpoint) endpoint).getEndpointConfiguration().getTimeout());

        endpoint.createProducer().send(new DefaultMessage("Message#0"), context);

        Message received = endpoint.createConsumer().receive(context);
        Assertions.assertNotNull(received);
        Assertions.assertEquals("Message#0", received.getPayload(String.class));

        Assertions.assertEquals(received, context.getMessageStore().getMessage("message-store.message"));
    }

    @Test
    public void shouldCreateMessageStoreEndpoint() {
        MessageStoreEndpointComponent component = new MessageStoreEndpointComponent();

        Endpoint endpoint = component.createEndpoint("message-store:myMessage", context);

        Assertions.assertEquals(MessageStoreEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(5000L, ((MessageStoreEndpoint) endpoint).getEndpointConfiguration().getTimeout());

        endpoint.createProducer().send(new DefaultMessage("Message#1"), context);

        Message received = endpoint.createConsumer().receive(context);
        Assertions.assertNotNull(received);
        Assertions.assertEquals("Message#1", received.getPayload(String.class));

        Assertions.assertEquals(received, context.getMessageStore().getMessage("myMessage"));
    }

    @Test
    public void shouldCreateEndpointWithMessageNameParameter() {
        MessageStoreEndpointComponent component = new MessageStoreEndpointComponent();

        Endpoint endpoint = component.createEndpoint("message-store?messageName=myMessage", context);

        Assertions.assertEquals(MessageStoreEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(5000L, ((MessageStoreEndpoint) endpoint).getEndpointConfiguration().getTimeout());

        endpoint.createProducer().send(new DefaultMessage("Message#2"), context);

        Message received = endpoint.createConsumer().receive(context);
        Assertions.assertNotNull(received);
        Assertions.assertEquals("Message#2", received.getPayload(String.class));

        Assertions.assertEquals(received, context.getMessageStore().getMessage("myMessage"));
    }

    @Test
    public void shouldCreateEndpointWithMessageNameParameterOverwrite() {
        MessageStoreEndpointComponent component = new MessageStoreEndpointComponent();

        Endpoint endpoint = component.createEndpoint("message-store:someName?messageName=fooMessage", context);

        Assertions.assertEquals(MessageStoreEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(5000L, ((MessageStoreEndpoint) endpoint).getEndpointConfiguration().getTimeout());

        endpoint.createProducer().send(new DefaultMessage("Message#2b"), context);

        Message received = endpoint.createConsumer().receive(context);
        Assertions.assertNotNull(received);
        Assertions.assertEquals("Message#2b", received.getPayload(String.class));

        Assertions.assertEquals(received, context.getMessageStore().getMessage("fooMessage"));
    }

    @Test
    public void shouldCreateEndpointWithParameters() {
        MessageStoreEndpointComponent component = new MessageStoreEndpointComponent();

        Endpoint endpoint = component.createEndpoint("message-store://myMessage?timeout=10000", context);

        Assertions.assertEquals(MessageStoreEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(10000L, ((MessageStoreEndpoint) endpoint).getEndpointConfiguration().getTimeout());

        context.getMessageStore().storeMessage("myMessage", new DefaultMessage("Message#3"));

        Message received = endpoint.createConsumer().receive(context);
        Assertions.assertNotNull(received);
        Assertions.assertEquals("Message#3", received.getPayload(String.class));
    }

    @Test
    public void shouldTimeoutWhenNoMessage() {
        MessageStoreEndpointComponent component = new MessageStoreEndpointComponent();

        Endpoint endpoint = component.createEndpoint("message-store:myMessage", context);

        Assertions.assertEquals(MessageStoreEndpoint.class, endpoint.getClass());

        Assertions.assertEquals(5000L, ((MessageStoreEndpoint) endpoint).getEndpointConfiguration().getTimeout());

        assertThrows(MessageTimeoutException.class, () -> endpoint.createConsumer().receive(context));
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("message-store").isPresent());
    }
}
