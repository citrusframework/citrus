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

package com.consol.citrus.http.config.xml;

import com.consol.citrus.TestActor;
import com.consol.citrus.http.message.HttpReplyMessageReceiver;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class HttpReplyMessageReceiverParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testFailActionParser() {
        Map<String, HttpReplyMessageReceiver> messageReceivers = beanDefinitionContext.getBeansOfType(HttpReplyMessageReceiver.class);
        
        Assert.assertEquals(messageReceivers.size(), 3);
        Assert.assertTrue(messageReceivers.containsKey("httpReplyMessageReceiver1"));
        
        Assert.assertTrue(messageReceivers.containsKey("httpReplyMessageReceiver2"));
        
        Assert.assertTrue(messageReceivers.containsKey("httpReplyMessageReceiver3"));
        
        // 2nd message receiver
        HttpReplyMessageReceiver messageReceiver = messageReceivers.get("httpReplyMessageReceiver2");
        Assert.assertNotNull(messageReceiver.getActor());
        Assert.assertEquals(messageReceiver.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
        
        // 3rd message receiver
        messageReceiver = messageReceivers.get("httpReplyMessageReceiver3");
        Assert.assertNotNull(messageReceiver.getEndpointConfiguration().getPollingInterval());
        Assert.assertEquals(messageReceiver.getEndpointConfiguration().getPollingInterval(), 550L);
    }
}
