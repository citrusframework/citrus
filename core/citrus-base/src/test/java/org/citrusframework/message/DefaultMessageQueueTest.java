/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.message;

import java.util.concurrent.atomic.AtomicLong;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.selector.HeaderMatchingMessageSelector;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class DefaultMessageQueueTest {

    private TestContext context;

    @BeforeMethod
    public void setupMocks() {
        context = new TestContext();
    }

    @Test
    public void testReceiveSelected() {
        DefaultMessageQueue queue = new DefaultMessageQueue("testQueue");
        queue.setPollingInterval(100L);

        queue.send(new DefaultMessage("FooMessage").setHeader("foo", "bar"));

        MessageSelector selector = new HeaderMatchingMessageSelector("foo", "bar", context);

        Message receivedMessage = queue.receive(selector, 1000L);

        Assert.assertEquals(receivedMessage.getPayload(), "FooMessage");
        Assert.assertEquals(receivedMessage.getHeaders().get("foo"), "bar");
    }

    @Test
    public void testWithRetry() {
        DefaultMessageQueue queue = new DefaultMessageQueue("testQueue");
        queue.setPollingInterval(100L);

        queue.send(new DefaultMessage("FooMessage").setHeader("foo", "bar"));

        final AtomicLong retries = new AtomicLong();
        MessageSelector selector = new HeaderMatchingMessageSelector("foo", "bar", context) {
            @Override
            public boolean accept(Message message) {
                return retries.incrementAndGet() > 7;
            }
        };

        Message receivedMessage = queue.receive(selector, 1000L);

        Assert.assertEquals(receivedMessage.getPayload(), "FooMessage");
        Assert.assertEquals(receivedMessage.getHeaders().get("foo"), "bar");
        Assert.assertEquals(retries.get(), 8L);
    }

    @Test
    public void testRetryExceeded() {
        DefaultMessageQueue queue = new DefaultMessageQueue("testQueue");
        queue.setPollingInterval(500L);

        queue.send(new DefaultMessage("FooMessage").setHeader("foos", "bars"));

        final AtomicLong retries = new AtomicLong();
        MessageSelector selector = new HeaderMatchingMessageSelector("foo", "bar", context) {
            @Override
            public boolean accept(Message message) {
                retries.incrementAndGet();
                return super.accept(message);
            }
        };

        Message receivedMessage = queue.receive(selector, 1000L);

        Assert.assertNull(receivedMessage);
        Assert.assertEquals(retries.get(), 3L);
    }

    @Test
    public void testRetryExceededWithTimeoutRest() {
        DefaultMessageQueue queue = new DefaultMessageQueue("testQueue");
        queue.setPollingInterval(400L);

        queue.send(new DefaultMessage("FooMessage").setHeader("foos", "bars"));

        final AtomicLong retries = new AtomicLong();
        MessageSelector selector = new HeaderMatchingMessageSelector("foo", "bar", context) {
            @Override
            public boolean accept(Message message) {
                retries.incrementAndGet();
                return super.accept(message);
            }
        };

        Message receivedMessage = queue.receive(selector, 1000L);

        Assert.assertNull(receivedMessage);
        Assert.assertEquals(retries.get(), 4L);
    }
}
