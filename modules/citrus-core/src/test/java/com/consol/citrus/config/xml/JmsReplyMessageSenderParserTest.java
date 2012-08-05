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

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.TestActor;
import com.consol.citrus.jms.JmsReplyMessageSender;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;

/**
 * @author Christoph Deppisch
 */
public class JmsReplyMessageSenderParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testFailActionParser() {
        Map<String, JmsReplyMessageSender> messageSenders = beanDefinitionContext.getBeansOfType(JmsReplyMessageSender.class);
        
        Assert.assertEquals(messageSenders.size(), 4);
        
        // 1st message sender
        JmsReplyMessageSender messageSender = messageSenders.get("jmsReplyMessageSender1");
        Assert.assertNotNull(messageSender.getConnectionFactory());
        Assert.assertEquals(messageSender.getConnectionFactory(), beanDefinitionContext.getBean("connectionFactory"));
        Assert.assertNull(messageSender.getDestinationName());
        Assert.assertNull(messageSender.getDestination());
        Assert.assertNotNull(messageSender.getReplyDestinationHolder());
        Assert.assertNull(messageSender.getCorrelator());
        
        // 2nd message sender
        messageSender = messageSenders.get("jmsReplyMessageSender2");
        Assert.assertNotNull(messageSender.getConnectionFactory());
        Assert.assertEquals(messageSender.getConnectionFactory(), beanDefinitionContext.getBean("jmsConnectionFactory"));
        Assert.assertNull(messageSender.getDestinationName());
        Assert.assertNull(messageSender.getDestination());
        Assert.assertNotNull(messageSender.getReplyDestinationHolder());
        Assert.assertNotNull(messageSender.getCorrelator());
        
        // 3rd message sender
        messageSender = messageSenders.get("jmsReplyMessageSender3");
        Assert.assertNull(messageSender.getConnectionFactory());
        Assert.assertNull(messageSender.getDestinationName());
        Assert.assertNull(messageSender.getDestination());
        Assert.assertNotNull(messageSender.getReplyDestinationHolder());
        Assert.assertNull(messageSender.getCorrelator());
        
        // 4th message sender
        messageSender = messageSenders.get("jmsReplyMessageSender4");
        Assert.assertNotNull(messageSender.getActor());
        Assert.assertEquals(messageSender.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }
}
