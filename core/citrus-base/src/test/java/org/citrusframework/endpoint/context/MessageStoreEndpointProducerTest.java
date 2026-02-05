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
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MessageStoreEndpointProducerTest {

    private final ReferenceResolver resolver = Mockito.mock(ReferenceResolver.class);

    private TestContext context;

    @BeforeMethod
    public void setupMocks() {
        context = new TestContext();
    }

    @Test
    public void testSendMessage() {
        MessageStoreEndpoint endpoint = new MessageStoreEndpointBuilder()
                .messageName("myMessage")
                .build();

        final Message message = new DefaultMessage("Hello World!");

        endpoint.createProducer().send(message, context);

        Message stored = context.getMessageStore().getMessage("myMessage");
        Assert.assertNotNull(stored);
        Assert.assertEquals(stored, message);
    }

    @Test
    public void testSendMessageWithDynamicName() {
        MessageStoreEndpoint endpoint = new MessageStoreEndpointBuilder()
                .build();

        context.setReferenceResolver(resolver);

        final Message message = new DefaultMessage("Hello World!").setName("fooMessage");

        endpoint.createProducer().send(message, context);

        Message stored = context.getMessageStore().getMessage("fooMessage");
        Assert.assertNotNull(stored);
        Assert.assertEquals(stored, message);
    }

    @Test
    public void testSendMessageOverwrite() {
        MessageStoreEndpoint endpoint = new MessageStoreEndpointBuilder()
                .messageName("myMessage")
                .build();

        context.getMessageStore().storeMessage("myMessage", new DefaultMessage("Hello World!"));
        final Message message = new DefaultMessage("Citrus rocks!");

        endpoint.createProducer().send(message, context);

        Message stored = context.getMessageStore().getMessage("myMessage");
        Assert.assertNotNull(stored);
        Assert.assertEquals(stored, message);
    }
}
