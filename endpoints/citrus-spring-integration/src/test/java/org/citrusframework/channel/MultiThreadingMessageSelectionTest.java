/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.channel;

import org.citrusframework.channel.selector.DispatchingMessageSelector;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.doThrow;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class MultiThreadingMessageSelectionTest extends AbstractTestNGUnitTest {

    private BeanFactory beanFactory = Mockito.mock(BeanFactory.class);

    private MessageSelectingQueueChannel channel = new MessageSelectingQueueChannel();

    private AtomicInteger index = new AtomicInteger();

    private Message<DefaultMessage> declinedMessage = MessageBuilder.withPayload(new DefaultMessage("<FooTest><operation>foobar0</operation></FooTest>")
            .setHeader("operation", "foobar0"))
            .build();

    @BeforeClass
    public void setupMock() {
        doThrow(new NoSuchBeanDefinitionException(NamespaceContextBuilder.class)).when(beanFactory).getBean(NamespaceContextBuilder.class);

        channel.send(declinedMessage);

        channel.send(MessageBuilder.withPayload(new DefaultMessage("<FooTest><operation>foo0</operation></FooTest>")
                .setHeader("operation", "foo0"))
                .setHeader("index", 0L)
                .build());
    }

    @Test(invocationCount = 100, threadPoolSize = 100)
    public void testHeaderMatchingSelectorConcurrent() {
        int i = index.incrementAndGet();

        channel.send(MessageBuilder.withPayload(new DefaultMessage("<FooTest><operation>foo" + i + "</operation></FooTest>")
                    .setHeader("operation", "foo" + i))
                    .setHeader("index", i)
                .build());

        DispatchingMessageSelector messageSelector = new DispatchingMessageSelector("xpath:/FooTest/operation = 'foo" + (i-1) +"'", beanFactory, context);

        Message<DefaultMessage> received = (Message<DefaultMessage>) channel.receive(messageSelector, 5000L);
        received.getPayload().setHeader("received", true);
        received.getPayload().setHeader("time", System.currentTimeMillis());

        declinedMessage.getPayload().setHeader("time" + i, System.currentTimeMillis());

        Assert.assertEquals(received.getPayload().getHeader("operation"), "foo" + (i-1));
    }
}
