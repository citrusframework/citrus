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

package org.citrusframework.endpoint.direct;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageSelector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DirectEndpointAdapterTest {

    private MessageQueue queue = new DefaultMessageQueue("testQueue");
    private DirectEndpointAdapter endpointAdapter;
    private DirectSyncEndpointConfiguration endpointConfiguration;

    private TestContextFactory testContextFactory = TestContextFactory.newInstance();
    private TestContext context;

    @BeforeAll
    public void setup() {
        endpointConfiguration = new DirectSyncEndpointConfiguration();
        endpointConfiguration.setQueue(queue);
        endpointConfiguration.setTimeout(10000L);

        endpointAdapter = new DirectEndpointAdapter(endpointConfiguration);
        endpointAdapter.setTestContextFactory(testContextFactory);
    }

    @BeforeEach
    public void purgeQueue() {
        context = new TestContext();
        queue.purge(new MessageSelector.AllAcceptingMessageSelector());
    }

    @Test
    public void testEndpointAdapter() {
        final Message request = new DefaultMessage("<TestMessage><text>Hi!</text></TestMessage>");

        var executorService = newSingleThreadExecutor();
        try {
            executorService.execute(() -> {
                Message receivedMessage = endpointAdapter.getEndpoint().createConsumer().receive(context, endpointConfiguration.getTimeout());
                Assertions.assertNotNull(receivedMessage);
                Assertions.assertEquals(request.getPayload(), receivedMessage.getPayload());

                endpointAdapter.getEndpoint().createProducer().send(new DefaultMessage("OK"), context);
            });
        } finally {
            executorService.shutdownNow();
        }

        Message response = endpointAdapter.handleMessage(request);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("OK", response.getPayload(String.class));
    }

    @Test
    public void testNoResponse() {
        Assertions.assertNull(endpointAdapter.handleMessage(new DefaultMessage("<TestMessage><text>Hi!</text></TestMessage>")));
    }
}
