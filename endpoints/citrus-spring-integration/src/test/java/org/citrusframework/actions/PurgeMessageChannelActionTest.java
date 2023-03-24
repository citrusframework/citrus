/*
 * Copyright 2006-2012 the original author or authors.
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

package org.citrusframework.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class PurgeMessageChannelActionTest extends AbstractTestNGUnitTest {

    @Autowired
    @Qualifier(value="mockChannel")
    private QueueChannel mockChannel;

    private final QueueChannel emptyChannel = Mockito.mock(QueueChannel.class);

    @Test
    public void testPurgeWithChannelNames() throws Exception {
        List<Message<?>> purgedMessages = new ArrayList<>();
        purgedMessages.add(MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());

        reset(mockChannel);

        when(mockChannel.purge((MessageSelector)any())).thenReturn(purgedMessages);

        PurgeMessageChannelAction purgeChannelAction = new PurgeMessageChannelAction.Builder()
                .beanFactory(applicationContext)
                .channelNames("mockChannel")
                .build();
        purgeChannelAction.execute(context);

    }

	@SuppressWarnings("unchecked")
    @Test
    public void testPurgeWithChannelObjects() throws Exception {
        List<Message<?>> purgedMessages = new ArrayList<>();
        purgedMessages.add(MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());

        reset(mockChannel, emptyChannel);

        when(mockChannel.purge(any())).thenReturn(purgedMessages);
        when(emptyChannel.purge(any())).thenReturn(Collections.EMPTY_LIST);

        PurgeMessageChannelAction purgeChannelAction = new PurgeMessageChannelAction.Builder()
                .beanFactory(applicationContext)
                .channels(mockChannel, emptyChannel)
                .build();
        purgeChannelAction.execute(context);

    }

	@Test
    public void testPurgeWithMessageSelector() throws Exception {
        MessageSelector messageSelector = message -> false;

        List<Message<?>> purgedMessages = new ArrayList<>();
        purgedMessages.add(MessageBuilder.withPayload("<TestRequest>Hello World!</TestRequest>").build());

        reset(mockChannel);

        when(mockChannel.purge(messageSelector)).thenReturn(purgedMessages);

        PurgeMessageChannelAction purgeChannelAction = new PurgeMessageChannelAction.Builder()
                .beanFactory(applicationContext)
                .channel(mockChannel)
                .selector(messageSelector)
                .build();
        purgeChannelAction.execute(context);

    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Map<String, TestActionBuilder<?>> endpointBuilders = TestActionBuilder.lookup();
        Assert.assertTrue(endpointBuilders.containsKey("purgeChannels"));

        Assert.assertTrue(TestActionBuilder.lookup("purgeChannels").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("purgeChannels").get().getClass(), PurgeMessageChannelAction.Builder.class);
    }

}
