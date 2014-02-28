/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.config.xml;

import com.consol.citrus.channel.SyncMessageChannelReceiver;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @deprecated
 */
public class SyncMessageChannelReceiverParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testSyncMessageChannelReceiverParser() {
        Map<String, SyncMessageChannelReceiver> messageReceivers = beanDefinitionContext.getBeansOfType(SyncMessageChannelReceiver.class);
        
        Assert.assertEquals(messageReceivers.size(), 3);
        
        // 1st message receiver
        SyncMessageChannelReceiver messageReceiver = messageReceivers.get("syncMessageChannelReceiver1");
        Assert.assertEquals(messageReceiver.getChannelName(), "channelName");
        Assert.assertNull(messageReceiver.getChannel());
        Assert.assertEquals(messageReceiver.getReceiveTimeout(), 5000L);
        Assert.assertNotNull(messageReceiver.getChannelResolver());
        Assert.assertNull(messageReceiver.getCorrelator());
        
        // 2nd message receiver
        messageReceiver = messageReceivers.get("syncMessageChannelReceiver2");
        Assert.assertNull(messageReceiver.getChannelName());
        Assert.assertNotNull(messageReceiver.getChannel());
        Assert.assertEquals(messageReceiver.getReceiveTimeout(), 10000L);
        Assert.assertNull(messageReceiver.getChannelResolver());
        Assert.assertNotNull(messageReceiver.getCorrelator());
        
        // 3rd message receiver
        messageReceiver = messageReceivers.get("syncMessageChannelReceiver3");
        Assert.assertNull(messageReceiver.getChannelName());
        Assert.assertNull(messageReceiver.getChannel());
        Assert.assertNull(messageReceiver.getChannelResolver());
        Assert.assertNull(messageReceiver.getCorrelator());
    }
}
