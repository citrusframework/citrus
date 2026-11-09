/*
 * Copyright the original author or authors.
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

import org.citrusframework.spring.config.CitrusNamespaceParserRegistry;
import org.citrusframework.jms.actions.PurgeJmsQueuesAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PurgeJmsQueuesActionParserTest extends AbstractActionParserTest<PurgeJmsQueuesAction> {

    @Test
    public void testPurgeJmsQueuesActionParser() {
        assertActionCount(3);
        assertActionClassAndName(PurgeJmsQueuesAction.class, "purge-queue");

        PurgeJmsQueuesAction action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getConnectionFactory());
        Assertions.assertEquals(100, action.getReceiveTimeout());
        Assertions.assertEquals(350L, action.getSleepTime());
        Assertions.assertEquals(0, action.getQueues().size());
        Assertions.assertEquals(3, action.getQueueNames().size());
        Assertions.assertEquals("JMS.Queue.1", action.getQueueNames().get(0));
        Assertions.assertEquals("JMS.Queue.2", action.getQueueNames().get(1));
        Assertions.assertEquals("JMS.Queue.3", action.getQueueNames().get(2));

        action = getNextTestActionFromTest();
        Assertions.assertEquals(125, action.getReceiveTimeout());
        Assertions.assertEquals(250L, action.getSleepTime());
        Assertions.assertNotNull(action.getConnectionFactory());
        Assertions.assertEquals(0, action.getQueues().size());
        Assertions.assertEquals(3, action.getQueueNames().size());
        Assertions.assertEquals("JMS.Queue.1", action.getQueueNames().get(0));
        Assertions.assertEquals("JMS.Queue.2", action.getQueueNames().get(1));
        Assertions.assertEquals("JMS.Queue.3", action.getQueueNames().get(2));

        action = getNextTestActionFromTest();
        Assertions.assertEquals(1, action.getQueues().size());
        Assertions.assertEquals(1, action.getQueueNames().size());
        Assertions.assertEquals("JMS.Queue.1", action.getQueueNames().get(0));
    }

    @Test
    public void testPurgeJmsQueuesActionParserFailed() {
        try {
            createApplicationContext("failed");
            Assertions.fail("Missing bean creation exception due to empty connection factory name");
        } catch (BeanDefinitionStoreException e) {
            Assertions.assertTrue(e.getMessage().startsWith("Configuration problem: Attribute 'connection-factory' must not be empty"));
        }
    }

    @Test
    public void shouldLookupTestActionParser() {
        Assertions.assertTrue(CitrusNamespaceParserRegistry.lookupBeanParser().containsKey("purge-jms-queues"));
        Assertions.assertEquals(PurgeJmsQueuesActionParser.class, CitrusNamespaceParserRegistry.lookupBeanParser().get("purge-jms-queues").getClass());

        Assertions.assertEquals(PurgeJmsQueuesActionParser.class, CitrusNamespaceParserRegistry.getBeanParser("purge-jms-queues").getClass());
    }
}
