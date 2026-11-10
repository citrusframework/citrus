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

package org.citrusframework.springintegration.channel;

import org.citrusframework.context.TestContextFactory;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.springframework.integration.channel.QueueChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class ChannelEndpointAdapterTest extends AbstractUnitTest {

    private QueueChannel channel = new QueueChannel();
    private ChannelEndpointAdapter endpointAdapter;
    private ChannelSyncEndpointConfiguration endpointConfiguration;

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory testContextFactory = super.createTestContextFactory();

        endpointConfiguration = new ChannelSyncEndpointConfiguration();
        endpointConfiguration.setChannel(channel);
        endpointConfiguration.setTimeout(250L);

        endpointAdapter = new ChannelEndpointAdapter(endpointConfiguration);
        endpointAdapter.setTestContextFactory(testContextFactory);

        return testContextFactory;
    }

    @BeforeEach
    public void purgeChannel() {
        channel.purge(message -> {
            return false; //purge all messages
        });
    }

    @Test
    public void testEndpointAdapter() {
        final Message request = new DefaultMessage("<TestMessage><text>Hi!</text></TestMessage>");

        var executor = newSingleThreadExecutor();
        try {
            executor.execute(() -> {
                Message receivedMessage = endpointAdapter.getEndpoint().createConsumer().receive(context, endpointConfiguration.getTimeout());
                Assertions.assertNotNull(receivedMessage);
                Assertions.assertEquals(request.getPayload(), receivedMessage.getPayload());

                endpointAdapter.getEndpoint().createProducer().send(new DefaultMessage("OK"), context);
            });
        } finally {
            executor.shutdown();
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
