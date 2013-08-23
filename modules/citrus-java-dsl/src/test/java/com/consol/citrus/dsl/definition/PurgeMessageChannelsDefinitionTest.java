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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.actions.PurgeMessageChannelAction;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.integration.support.channel.ChannelResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.easymock.EasyMock.*;

public class PurgeMessageChannelsDefinitionTest extends AbstractTestNGUnitTest {
    private MessageSelector messageSelector = EasyMock.createMock(MessageSelector.class);
    
    private ChannelResolver channelResolver = EasyMock.createMock(ChannelResolver.class);
    
    private MessageChannel channel1 = EasyMock.createMock(MessageChannel.class);
    private MessageChannel channel2 = EasyMock.createMock(MessageChannel.class);
    private MessageChannel channel3 = EasyMock.createMock(MessageChannel.class);
    
    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);

    @Test
    public void testPurgeChannelsBuilderWithChannels() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                purgeChannels()
                        .channels(channel1, channel2)
                        .channel(channel3);
            }
        };

        builder.run(null, null);

        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), PurgeMessageChannelAction.class);

        PurgeMessageChannelAction action = (PurgeMessageChannelAction) builder.testCase().getActions().get(0);
        Assert.assertEquals(action.getChannels().size(), 3);
        Assert.assertEquals(action.getChannels().toString(), "[" + channel1.toString() + ", " + channel2.toString() + ", " + channel3.toString() + "]");
        Assert.assertNull(action.getMessageSelector());
    }
    
    @Test
    public void testPurgeChannelBuilderWithNames() {
        MockBuilder builder = new MockBuilder(applicationContextMock) {
            @Override
            public void configure() {
                purgeChannels()
                        .channelResolver(channelResolver)
                        .channelNames("ch1", "ch2", "ch3")
                        .channel("ch4")
                        .selector(messageSelector);
            }
        };

        reset(applicationContextMock);

        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();

        replay(applicationContextMock);
        
        builder.run(null, null);

        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), PurgeMessageChannelAction.class);

        PurgeMessageChannelAction action = (PurgeMessageChannelAction) builder.testCase().getActions().get(0);
        Assert.assertEquals(action.getChannelNames().size(), 4);
        Assert.assertEquals(action.getChannelNames().toString(), "[ch1, ch2, ch3, ch4]");
        Assert.assertEquals(action.getChannelResolver(), channelResolver);
        Assert.assertEquals(action.getMessageSelector(), messageSelector);
        
        verify(applicationContextMock);
    }
    
    @Test
    public void testMissingChannelResolver() {
        MockBuilder builder = new MockBuilder(applicationContextMock) {
            @Override
            public void configure() {
                purgeChannels()
                        .channel("ch1");
            }
        };
        
        reset(applicationContextMock);

        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        
        replay(applicationContextMock);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), PurgeMessageChannelAction.class);

        PurgeMessageChannelAction action = (PurgeMessageChannelAction) builder.testCase().getActions().get(0);
        Assert.assertEquals(action.getChannelNames().size(), 1);
        Assert.assertEquals(action.getChannelNames().toString(), "[ch1]");
        Assert.assertNotNull(action.getChannelResolver());
        Assert.assertTrue(action.getChannelResolver() instanceof BeanFactoryChannelResolver);
        
        verify(applicationContextMock);
    }
}
