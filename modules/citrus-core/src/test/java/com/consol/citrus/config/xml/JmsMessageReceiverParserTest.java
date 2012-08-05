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
import com.consol.citrus.jms.JmsMessageReceiver;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;

/**
 * @author Christoph Deppisch
 */
public class JmsMessageReceiverParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testFailActionParser() {
        Map<String, JmsMessageReceiver> messageReceivers = beanDefinitionContext.getBeansOfType(JmsMessageReceiver.class);
        
        Assert.assertEquals(messageReceivers.size(), 4);
        
        // 1st message receiver
        JmsMessageReceiver messageReceiver = messageReceivers.get("jmsMessageReceiver1");
        Assert.assertNotNull(messageReceiver.getConnectionFactory());
        Assert.assertEquals(messageReceiver.getConnectionFactory(), beanDefinitionContext.getBean("connectionFactory"));
        Assert.assertEquals(messageReceiver.getDestinationName(), "JMS.Queue.Test");
        Assert.assertNull(messageReceiver.getDestination());
        Assert.assertEquals(messageReceiver.getReceiveTimeout(), 5000L);
        
        // 2nd message receiver
        messageReceiver = messageReceivers.get("jmsMessageReceiver2");
        Assert.assertNotNull(messageReceiver.getConnectionFactory());
        Assert.assertEquals(messageReceiver.getConnectionFactory(), beanDefinitionContext.getBean("jmsConnectionFactory"));
        Assert.assertNull(messageReceiver.getDestinationName());
        Assert.assertNotNull(messageReceiver.getDestination());
        Assert.assertEquals(messageReceiver.getReceiveTimeout(), 10000L);
        
        // 3rd message receiver
        messageReceiver = messageReceivers.get("jmsMessageReceiver3");
        Assert.assertNull(messageReceiver.getConnectionFactory());
        Assert.assertNull(messageReceiver.getDestinationName());
        Assert.assertNull(messageReceiver.getDestination());
        Assert.assertEquals(messageReceiver.isPubSubDomain(), true);
        
        // 4th message receiver
        messageReceiver = messageReceivers.get("jmsMessageReceiver4");
        Assert.assertNotNull(messageReceiver.getActor());
        Assert.assertEquals(messageReceiver.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }
}
