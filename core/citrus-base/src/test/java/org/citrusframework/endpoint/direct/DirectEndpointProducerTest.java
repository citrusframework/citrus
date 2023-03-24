/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.endpoint.direct;

import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class DirectEndpointProducerTest {

    private MessageQueue queue = Mockito.mock(MessageQueue.class);
    private ReferenceResolver resolver = Mockito.mock(ReferenceResolver.class);

    private TestContext context;

    @BeforeMethod
    public void setupMocks() {
        context = new TestContext();
    }

    @Test
    public void testSendMessage() {
        DirectEndpoint endpoint = new DirectEndpoint();

        endpoint.getEndpointConfiguration().setQueue(queue);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(queue);

        endpoint.createProducer().send(message, context);

        verify(queue).send(any(Message.class));
    }

    @Test
    public void testSendMessageQueueNameResolver() {
        DirectEndpoint endpoint = new DirectEndpoint();

        endpoint.getEndpointConfiguration().setQueueName("testQueue");

        context.setReferenceResolver(resolver);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(queue, resolver);

        when(resolver.resolve("testQueue", MessageQueue.class)).thenReturn(queue);

        endpoint.createProducer().send(message, context);

        verify(queue).send(any(Message.class));
    }

    @Test
    public void testSendMessageFailed() {
        DirectEndpoint endpoint = new DirectEndpoint();

        endpoint.getEndpointConfiguration().setQueue(queue);

        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(queue);

        doThrow(new RuntimeException("Internal error!")).when(queue).send(any(Message.class));

        try {
            endpoint.createProducer().send(message, context);
        } catch(CitrusRuntimeException e) {
            Assert.assertTrue(e.getLocalizedMessage().startsWith("Failed to send message to queue: "));
            Assert.assertNotNull(e.getCause());
            Assert.assertEquals(e.getCause().getClass(), RuntimeException.class);
            Assert.assertEquals(e.getCause().getLocalizedMessage(), "Internal error!");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " because no message was received");
    }

}
