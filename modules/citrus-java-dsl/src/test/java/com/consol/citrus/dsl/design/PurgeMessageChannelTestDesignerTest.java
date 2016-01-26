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

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.PurgeMessageChannelAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 * @since 1.3
 */
public class PurgeMessageChannelTestDesignerTest extends AbstractTestNGUnitTest {
    private MessageSelector messageSelector = Mockito.mock(MessageSelector.class);
    
    private DestinationResolver channelResolver = Mockito.mock(DestinationResolver.class);
    
    private MessageChannel channel1 = Mockito.mock(MessageChannel.class);
    private MessageChannel channel2 = Mockito.mock(MessageChannel.class);
    private MessageChannel channel3 = Mockito.mock(MessageChannel.class);
    
    private ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    @Test
    public void testPurgeChannelsBuilderWithChannels() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                purgeChannels()
                        .channels(channel1, channel2)
                        .channel(channel3);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeMessageChannelAction.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "purge-channel");

        PurgeMessageChannelAction action = (PurgeMessageChannelAction) test.getActions().get(0);
        Assert.assertEquals(action.getChannels().size(), 3);
        Assert.assertEquals(action.getChannels().toString(), "[" + channel1.toString() + ", " + channel2.toString() + ", " + channel3.toString() + "]");
        Assert.assertNull(action.getMessageSelector());
    }
    
    @Test
    public void testPurgeChannelBuilderWithNames() {
        reset(applicationContextMock);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());
        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                purgeChannels()
                        .channelResolver(channelResolver)
                        .channelNames("ch1", "ch2", "ch3")
                        .channel("ch4")
                        .selector(messageSelector);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeMessageChannelAction.class);

        PurgeMessageChannelAction action = (PurgeMessageChannelAction) test.getActions().get(0);
        Assert.assertEquals(action.getChannelNames().size(), 4);
        Assert.assertEquals(action.getChannelNames().toString(), "[ch1, ch2, ch3, ch4]");
        Assert.assertEquals(action.getChannelResolver(), channelResolver);
        Assert.assertEquals(action.getMessageSelector(), messageSelector);

    }
    
    @Test
    public void testMissingChannelResolver() {
        reset(applicationContextMock);

        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());
        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                purgeChannels()
                        .channel("ch1");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeMessageChannelAction.class);

        PurgeMessageChannelAction action = (PurgeMessageChannelAction) test.getActions().get(0);
        Assert.assertEquals(action.getChannelNames().size(), 1);
        Assert.assertEquals(action.getChannelNames().toString(), "[ch1]");
        Assert.assertNotNull(action.getChannelResolver());
        Assert.assertTrue(action.getChannelResolver() instanceof BeanFactoryChannelResolver);

    }
}
