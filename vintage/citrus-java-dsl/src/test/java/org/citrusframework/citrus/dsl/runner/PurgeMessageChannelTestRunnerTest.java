/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.citrus.dsl.runner;

import java.util.ArrayList;
import java.util.HashMap;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.PurgeMessageChannelAction;
import org.citrusframework.citrus.container.SequenceAfterTest;
import org.citrusframework.citrus.container.SequenceBeforeTest;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.report.TestActionListeners;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.mockito.Mockito;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class PurgeMessageChannelTestRunnerTest extends UnitTestSupport {

    private MessageSelector messageSelector = Mockito.mock(MessageSelector.class);
    private DestinationResolver channelResolver = Mockito.mock(DestinationResolver.class);

    private QueueChannel channel1 = Mockito.mock(QueueChannel.class);
    private QueueChannel channel2 = Mockito.mock(QueueChannel.class);
    private QueueChannel channel3 = Mockito.mock(QueueChannel.class);
    private MessageChannel channel4 = Mockito.mock(MessageChannel.class);

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void testPurgeChannelsBuilderWithChannels() {
        reset(channel1, channel2, channel3, channel4);

        when(channel1.purge(any(MessageSelector.class))).thenReturn(new ArrayList<>());
        when(channel2.purge(any(MessageSelector.class))).thenReturn(new ArrayList<>());
        when(channel3.purge(any(MessageSelector.class))).thenReturn(new ArrayList<>());

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                purgeChannels(builder -> builder.channels(channel1, channel2)
                        .channel(channel3));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeMessageChannelAction.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "purge-channel");

        PurgeMessageChannelAction action = (PurgeMessageChannelAction) test.getActions().get(0);
        Assert.assertEquals(action.getChannels().size(), 3);
        Assert.assertEquals(action.getChannels().toString(), "[" + channel1.toString() + ", " + channel2.toString() + ", " + channel3.toString() + "]");
        Assert.assertNotNull(action.getMessageSelector());
        Assert.assertEquals(action.getMessageSelector().getClass(), PurgeMessageChannelAction.AllAcceptingMessageSelector.class);
    }

    @Test
    public void testPurgeChannelBuilderWithNames() {
        reset(referenceResolver, channel1, channel2, channel3, channel4);

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolve("ch1", MessageChannel.class)).thenReturn(channel1);
        when(referenceResolver.resolve("ch2", MessageChannel.class)).thenReturn(channel2);
        when(referenceResolver.resolve("ch3", MessageChannel.class)).thenReturn(channel3);
        when(referenceResolver.resolve("ch4", MessageChannel.class)).thenReturn(channel4);

        when(channel1.purge(any(MessageSelector.class))).thenReturn(new ArrayList<>());
        when(channel2.purge(any(MessageSelector.class))).thenReturn(new ArrayList<>());
        when(channel3.purge(any(MessageSelector.class))).thenReturn(new ArrayList<>());

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                purgeChannels(builder -> builder.channelNames("ch1", "ch2", "ch3")
                        .channel("ch4")
                        .selector(messageSelector));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeMessageChannelAction.class);

        PurgeMessageChannelAction action = (PurgeMessageChannelAction) test.getActions().get(0);
        Assert.assertEquals(action.getChannelNames().size(), 4);
        Assert.assertEquals(action.getChannelNames().toString(), "[ch1, ch2, ch3, ch4]");
        Assert.assertNotNull(action.getChannelResolver());
        Assert.assertEquals(action.getMessageSelector(), messageSelector);
    }

    @Test
    public void testCustomChannelResolver() {
        reset(referenceResolver, channelResolver, channel1);

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        when(channelResolver.resolveDestination("ch1")).thenReturn(channel1);
        when(channel1.purge(any(MessageSelector.class))).thenReturn(new ArrayList<>());

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                purgeChannels(builder -> builder.channel("ch1")
                        .channelResolver(channelResolver));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeMessageChannelAction.class);

        PurgeMessageChannelAction action = (PurgeMessageChannelAction) test.getActions().get(0);
        Assert.assertEquals(action.getChannelNames().size(), 1);
        Assert.assertEquals(action.getChannelNames().toString(), "[ch1]");
        Assert.assertNotNull(action.getChannelResolver());
        Assert.assertEquals(action.getChannelResolver(), channelResolver);
    }
}
