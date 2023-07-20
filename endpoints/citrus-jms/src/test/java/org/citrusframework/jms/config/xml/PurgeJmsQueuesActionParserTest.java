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

package org.citrusframework.jms.config.xml;

import org.citrusframework.config.CitrusNamespaceParserRegistry;
import org.citrusframework.jms.actions.PurgeJmsQueuesAction;
import org.citrusframework.testng.AbstractActionParserTest;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class PurgeJmsQueuesActionParserTest extends AbstractActionParserTest<PurgeJmsQueuesAction> {

    @Test
    public void testPurgeJmsQueuesActionParser() {
        assertActionCount(3);
        assertActionClassAndName(PurgeJmsQueuesAction.class, "purge-queue");

        PurgeJmsQueuesAction action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getConnectionFactory());
        Assert.assertEquals(action.getReceiveTimeout(), 100);
        Assert.assertEquals(action.getSleepTime(), 350L);
        Assert.assertEquals(action.getQueues().size(), 0);
        Assert.assertEquals(action.getQueueNames().size(), 3);
        Assert.assertEquals(action.getQueueNames().get(0), "JMS.Queue.1");
        Assert.assertEquals(action.getQueueNames().get(1), "JMS.Queue.2");
        Assert.assertEquals(action.getQueueNames().get(2), "JMS.Queue.3");

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getReceiveTimeout(), 125);
        Assert.assertEquals(action.getSleepTime(), 250L);
        Assert.assertNotNull(action.getConnectionFactory());
        Assert.assertEquals(action.getQueues().size(), 0);
        Assert.assertEquals(action.getQueueNames().size(), 3);
        Assert.assertEquals(action.getQueueNames().get(0), "JMS.Queue.1");
        Assert.assertEquals(action.getQueueNames().get(1), "JMS.Queue.2");
        Assert.assertEquals(action.getQueueNames().get(2), "JMS.Queue.3");

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getQueues().size(), 1);
        Assert.assertEquals(action.getQueueNames().size(), 1);
        Assert.assertEquals(action.getQueueNames().get(0), "JMS.Queue.1");
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

    @Test
    public void shouldLookupTestActionParser() {
        Assert.assertTrue(CitrusNamespaceParserRegistry.lookupBeanParser().containsKey("purge-jms-queues"));
        Assert.assertEquals(CitrusNamespaceParserRegistry.lookupBeanParser().get("purge-jms-queues").getClass(), PurgeJmsQueuesActionParser.class);

        Assert.assertEquals(CitrusNamespaceParserRegistry.getBeanParser("purge-jms-queues").getClass(), PurgeJmsQueuesActionParser.class);
    }
}
