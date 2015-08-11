/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.channel;

import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSelector;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * @author Christoph Deppisch
 */
public class ChannelEndpointAdapterTest extends AbstractTestNGUnitTest {

    private QueueChannel channel = new QueueChannel();
    private ChannelEndpointAdapter endpointAdapter;
    private ChannelSyncEndpointConfiguration endpointConfiguration;

    @Autowired
    private TestContextFactory testContextFactory;

    @BeforeClass
    public void setup() {
        endpointConfiguration = new ChannelSyncEndpointConfiguration();
        endpointConfiguration.setChannel(channel);
        endpointConfiguration.setTimeout(250L);

        endpointAdapter = new ChannelEndpointAdapter(endpointConfiguration);
        endpointAdapter.setTestContextFactory(testContextFactory);
    }

    @BeforeMethod
    public void purgeChannel() {
        channel.purge(new MessageSelector() {
            @Override
            public boolean accept(org.springframework.messaging.Message message) {
                return false; //purge all messages
            }
        });
    }

    @Test
    public void testEndpointAdapter() {
        final Message request = new DefaultMessage("<TestMessage><text>Hi!</text></TestMessage>");

        new SimpleAsyncTaskExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Message receivedMessage = endpointAdapter.getEndpoint().createConsumer().receive(context, endpointConfiguration.getTimeout());
                Assert.assertNotNull(receivedMessage);
                Assert.assertEquals(receivedMessage.getPayload(), request.getPayload());

                endpointAdapter.getEndpoint().createProducer().send(new DefaultMessage("OK"), context);
            }
        });

        Message response = endpointAdapter.handleMessage(request);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getPayload(String.class), "OK");
    }

    @Test
    public void testNoResponse() {
        Assert.assertNull(endpointAdapter.handleMessage(new DefaultMessage("<TestMessage><text>Hi!</text></TestMessage>")));
    }
}
