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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.PurgeMessageChannelAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.PurgeChannelsBuilder;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.channel.DefaultHeaderChannelRegistry;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.integration.support.channel.HeaderChannelRegistry;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class PurgeMessageChannelTestRunnerTest extends AbstractTestNGUnitTest {

    private MessageSelector messageSelector = EasyMock.createMock(MessageSelector.class);
    private DestinationResolver channelResolver = EasyMock.createMock(DestinationResolver.class);
    
    private QueueChannel channel1 = EasyMock.createMock(QueueChannel.class);
    private QueueChannel channel2 = EasyMock.createMock(QueueChannel.class);
    private QueueChannel channel3 = EasyMock.createMock(QueueChannel.class);
    private MessageChannel channel4 = EasyMock.createMock(MessageChannel.class);

    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);

    @Test
    public void testPurgeChannelsBuilderWithChannels() {
        reset(channel1, channel2, channel3, channel4);

        expect(channel1.purge(anyObject(MessageSelector.class))).andReturn(new ArrayList<Message<?>>()).once();
        expect(channel2.purge(anyObject(MessageSelector.class))).andReturn(new ArrayList<Message<?>>()).once();
        expect(channel3.purge(anyObject(MessageSelector.class))).andReturn(new ArrayList<Message<?>>()).once();

        replay(channel1, channel2, channel3, channel4);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                purgeChannels(new BuilderSupport<PurgeChannelsBuilder>() {
                    @Override
                    public void configure(PurgeChannelsBuilder builder) {
                        builder.channels(channel1, channel2)
                                .channel(channel3);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeMessageChannelAction.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "purge-channel");

        PurgeMessageChannelAction action = (PurgeMessageChannelAction) test.getActions().get(0);
        Assert.assertEquals(action.getChannels().size(), 3);
        Assert.assertEquals(action.getChannels().toString(), "[" + channel1.toString() + ", " + channel2.toString() + ", " + channel3.toString() + "]");
        Assert.assertNull(action.getMessageSelector());

        verify(channel1, channel2, channel3, channel4);

    }
    
    @Test
    public void testPurgeChannelBuilderWithNames() {
        reset(applicationContextMock, channel1, channel2, channel3, channel4);

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();
        expect(applicationContextMock.getBean(IntegrationContextUtils.INTEGRATION_HEADER_CHANNEL_REGISTRY_BEAN_NAME, HeaderChannelRegistry.class))
                .andReturn(new DefaultHeaderChannelRegistry()).once();

        expect(applicationContextMock.getBean("ch1", MessageChannel.class)).andReturn(channel1).once();
        expect(applicationContextMock.getBean("ch2", MessageChannel.class)).andReturn(channel2).once();
        expect(applicationContextMock.getBean("ch3", MessageChannel.class)).andReturn(channel3).once();
        expect(applicationContextMock.getBean("ch4", MessageChannel.class)).andReturn(channel4).once();

        expect(channel1.purge(anyObject(MessageSelector.class))).andReturn(new ArrayList<Message<?>>()).once();
        expect(channel2.purge(anyObject(MessageSelector.class))).andReturn(new ArrayList<Message<?>>()).once();
        expect(channel3.purge(anyObject(MessageSelector.class))).andReturn(new ArrayList<Message<?>>()).once();

        replay(applicationContextMock, channel1, channel2, channel3, channel4);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                purgeChannels(new BuilderSupport<PurgeChannelsBuilder>() {
                    @Override
                    public void configure(PurgeChannelsBuilder builder) {
                        builder.channelNames("ch1", "ch2", "ch3")
                                .channel("ch4")
                                .selector(messageSelector);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeMessageChannelAction.class);

        PurgeMessageChannelAction action = (PurgeMessageChannelAction) test.getActions().get(0);
        Assert.assertEquals(action.getChannelNames().size(), 4);
        Assert.assertEquals(action.getChannelNames().toString(), "[ch1, ch2, ch3, ch4]");
        Assert.assertTrue(action.getChannelResolver() instanceof BeanFactoryChannelResolver);
        Assert.assertEquals(action.getMessageSelector(), messageSelector);

        verify(applicationContextMock, channel1, channel2, channel3, channel4);
    }

    @Test
    public void testCustomChannelResolver() {
        reset(applicationContextMock, channelResolver, channel1);

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();
        expect(applicationContextMock.getBean(IntegrationContextUtils.INTEGRATION_HEADER_CHANNEL_REGISTRY_BEAN_NAME, HeaderChannelRegistry.class))
                .andReturn(new DefaultHeaderChannelRegistry()).once();

        expect(channelResolver.resolveDestination("ch1")).andReturn(channel1).once();
        expect(channel1.purge(anyObject(MessageSelector.class))).andReturn(new ArrayList<Message<?>>()).once();
        replay(applicationContextMock, channelResolver, channel1);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                purgeChannels(new BuilderSupport<PurgeChannelsBuilder>() {
                    @Override
                    public void configure(PurgeChannelsBuilder builder) {
                        builder.channel("ch1")
                                .channelResolver(channelResolver);
                    }
                });
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

        verify(applicationContextMock, channelResolver, channel1);
    }
}
