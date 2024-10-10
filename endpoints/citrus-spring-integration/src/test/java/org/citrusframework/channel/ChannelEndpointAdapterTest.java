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

package org.citrusframework.channel;

import org.citrusframework.context.TestContextFactory;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.springframework.integration.channel.QueueChannel;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class ChannelEndpointAdapterTest extends AbstractTestNGUnitTest {

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

    @BeforeMethod
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
                Assert.assertNotNull(receivedMessage);
                Assert.assertEquals(receivedMessage.getPayload(), request.getPayload());

                endpointAdapter.getEndpoint().createProducer().send(new DefaultMessage("OK"), context);
            });
        } finally {
            executor.shutdown();
        }

        Message response = endpointAdapter.handleMessage(request);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getPayload(String.class), "OK");
    }

    @Test
    public void testNoResponse() {
        Assert.assertNull(endpointAdapter.handleMessage(new DefaultMessage("<TestMessage><text>Hi!</text></TestMessage>")));
    }
}
