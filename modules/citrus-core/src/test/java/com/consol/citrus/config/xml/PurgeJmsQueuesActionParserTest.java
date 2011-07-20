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

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.PurgeJmsQueuesAction;
import com.consol.citrus.testng.AbstractBeanDefinitionParserBaseTest;

/**
 * @author Christoph Deppisch
 */
public class PurgeJmsQueuesActionParserTest extends AbstractBeanDefinitionParserBaseTest {

    @Test
    public void testPurgeJmsQueuesActionParser() {
        Assert.assertEquals(getTestCase().getActions().size(), 3);

        Assert.assertEquals(getTestCase().getActions().get(0).getClass(), PurgeJmsQueuesAction.class);
        Assert.assertEquals(getTestCase().getActions().get(0).getName(), "purge-jms-queues");
        
        Assert.assertNotNull(((PurgeJmsQueuesAction)getTestCase().getActions().get(0)).getReceiveTimeout());
        Assert.assertNotNull(((PurgeJmsQueuesAction)getTestCase().getActions().get(0)).getConnectionFactory());
        Assert.assertEquals(((PurgeJmsQueuesAction)getTestCase().getActions().get(0)).getQueues().size(), 0);
        Assert.assertEquals(((PurgeJmsQueuesAction)getTestCase().getActions().get(0)).getQueueNames().size(), 3);
        Assert.assertEquals(((PurgeJmsQueuesAction)getTestCase().getActions().get(0)).getQueueNames().get(0), "JMS.Queue.1");
        Assert.assertEquals(((PurgeJmsQueuesAction)getTestCase().getActions().get(0)).getQueueNames().get(1), "JMS.Queue.2");
        Assert.assertEquals(((PurgeJmsQueuesAction)getTestCase().getActions().get(0)).getQueueNames().get(2), "JMS.Queue.3");
        
        Assert.assertNotNull(((PurgeJmsQueuesAction)getTestCase().getActions().get(1)).getReceiveTimeout());
        Assert.assertEquals(((PurgeJmsQueuesAction)getTestCase().getActions().get(1)).getReceiveTimeout(), 125);
        Assert.assertNotNull(((PurgeJmsQueuesAction)getTestCase().getActions().get(1)).getConnectionFactory());
        Assert.assertEquals(((PurgeJmsQueuesAction)getTestCase().getActions().get(1)).getQueues().size(), 0);
        Assert.assertEquals(((PurgeJmsQueuesAction)getTestCase().getActions().get(1)).getQueueNames().size(), 3);
        Assert.assertEquals(((PurgeJmsQueuesAction)getTestCase().getActions().get(1)).getQueueNames().get(0), "JMS.Queue.1");
        Assert.assertEquals(((PurgeJmsQueuesAction)getTestCase().getActions().get(1)).getQueueNames().get(1), "JMS.Queue.2");
        Assert.assertEquals(((PurgeJmsQueuesAction)getTestCase().getActions().get(1)).getQueueNames().get(2), "JMS.Queue.3");
        
        Assert.assertEquals(((PurgeJmsQueuesAction)getTestCase().getActions().get(2)).getQueues().size(), 1);
        Assert.assertEquals(((PurgeJmsQueuesAction)getTestCase().getActions().get(2)).getQueueNames().size(), 1);
        Assert.assertEquals(((PurgeJmsQueuesAction)getTestCase().getActions().get(2)).getQueueNames().get(0), "JMS.Queue.1");
    }
    
    @Test
    public void testPurgeJmsQueuesActionParserFailed() {
        try {
            createApplicationContext("failed");
            Assert.fail("Missing bean creation exception due to empty connection factory name");
        } catch (BeanDefinitionStoreException e) {
            Assert.assertTrue(e.getMessage().startsWith("Configuration problem: Attribute 'connection-factory' must not be empty"));
        }
    }
}
