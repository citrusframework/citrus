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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MessageStoreEndpointComponentTest {

    private TestContext context;

    @BeforeMethod
    public void setupMocks() {
        context = TestContextFactory.newInstance().getObject();
    }

    @Test
    public void shouldHandleNoParameters() {
        MessageStoreEndpointComponent component = new MessageStoreEndpointComponent();

        Endpoint endpoint = component.createEndpoint("message-store", context);

        Assert.assertEquals(endpoint.getClass(), MessageStoreEndpoint.class);

        Assert.assertEquals(((MessageStoreEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint.createProducer().send(new DefaultMessage("Message#0"), context);

        Message received = endpoint.createConsumer().receive(context);
        Assert.assertNotNull(received);
        Assert.assertEquals(received.getPayload(String.class), "Message#0");

        Assert.assertEquals(context.getMessageStore().getMessage("message-store.message"), received);
    }

    @Test
    public void shouldCreateMessageStoreEndpoint() {
        MessageStoreEndpointComponent component = new MessageStoreEndpointComponent();

        Endpoint endpoint = component.createEndpoint("message-store:myMessage", context);

        Assert.assertEquals(endpoint.getClass(), MessageStoreEndpoint.class);

        Assert.assertEquals(((MessageStoreEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint.createProducer().send(new DefaultMessage("Message#1"), context);

        Message received = endpoint.createConsumer().receive(context);
        Assert.assertNotNull(received);
        Assert.assertEquals(received.getPayload(String.class), "Message#1");

        Assert.assertEquals(context.getMessageStore().getMessage("myMessage"), received);
    }

    @Test
    public void shouldCreateEndpointWithMessageNameParameter() {
        MessageStoreEndpointComponent component = new MessageStoreEndpointComponent();

        Endpoint endpoint = component.createEndpoint("message-store?messageName=myMessage", context);

        Assert.assertEquals(endpoint.getClass(), MessageStoreEndpoint.class);

        Assert.assertEquals(((MessageStoreEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint.createProducer().send(new DefaultMessage("Message#2"), context);

        Message received = endpoint.createConsumer().receive(context);
        Assert.assertNotNull(received);
        Assert.assertEquals(received.getPayload(String.class), "Message#2");

        Assert.assertEquals(context.getMessageStore().getMessage("myMessage"), received);
    }

    @Test
    public void shouldCreateEndpointWithMessageNameParameterOverwrite() {
        MessageStoreEndpointComponent component = new MessageStoreEndpointComponent();

        Endpoint endpoint = component.createEndpoint("message-store:someName?messageName=fooMessage", context);

        Assert.assertEquals(endpoint.getClass(), MessageStoreEndpoint.class);

        Assert.assertEquals(((MessageStoreEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint.createProducer().send(new DefaultMessage("Message#2b"), context);

        Message received = endpoint.createConsumer().receive(context);
        Assert.assertNotNull(received);
        Assert.assertEquals(received.getPayload(String.class), "Message#2b");

        Assert.assertEquals(context.getMessageStore().getMessage("fooMessage"), received);
    }

    @Test
    public void shouldCreateEndpointWithParameters() {
        MessageStoreEndpointComponent component = new MessageStoreEndpointComponent();

        Endpoint endpoint = component.createEndpoint("message-store://myMessage?timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), MessageStoreEndpoint.class);

        Assert.assertEquals(((MessageStoreEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

        context.getMessageStore().storeMessage("myMessage", new DefaultMessage("Message#3"));

        Message received = endpoint.createConsumer().receive(context);
        Assert.assertNotNull(received);
        Assert.assertEquals(received.getPayload(String.class), "Message#3");
    }

    @Test(expectedExceptions = MessageTimeoutException.class)
    public void shouldTimeoutWhenNoMessage() {
        MessageStoreEndpointComponent component = new MessageStoreEndpointComponent();

        Endpoint endpoint = component.createEndpoint("message-store:myMessage", context);

        Assert.assertEquals(endpoint.getClass(), MessageStoreEndpoint.class);

        Assert.assertEquals(((MessageStoreEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint.createConsumer().receive(context);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("message-store").isPresent());
    }
}
