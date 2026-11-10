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

package org.citrusframework.springintegration.config.xml;

import org.citrusframework.springintegration.actions.PurgeMessageChannelAction;
import org.citrusframework.spring.config.CitrusNamespaceParserRegistry;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PurgeMessageChannelActionParserTest extends AbstractActionParserTest<PurgeMessageChannelAction> {

    @Test
    public void testPurgeMessageChannelActionParser() {
        assertActionCount(3);
        assertActionClassAndName(PurgeMessageChannelAction.class, "purge-channel");

        PurgeMessageChannelAction action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getMessageSelector());
        Assertions.assertEquals(0, action.getChannels().size());
        Assertions.assertEquals(3, action.getChannelNames().size());
        Assertions.assertEquals("testChannel1", action.getChannelNames().get(0));
        Assertions.assertEquals("testChannel2", action.getChannelNames().get(1));
        Assertions.assertEquals("testChannel3", action.getChannelNames().get(2));

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getMessageSelector());
        Assertions.assertEquals(1, action.getChannels().size());
        Assertions.assertEquals(3, action.getChannelNames().size());
        Assertions.assertEquals("testChannel1", action.getChannelNames().get(0));
        Assertions.assertEquals("testChannel2", action.getChannelNames().get(1));
        Assertions.assertEquals("testChannel3", action.getChannelNames().get(2));

        action = getNextTestActionFromTest();
        Assertions.assertEquals(beanDefinitionContext.getBean("testMessageSelector"), action.getMessageSelector());
        Assertions.assertEquals(1, action.getChannels().size());
        Assertions.assertEquals(1, action.getChannelNames().size());
        Assertions.assertEquals("testChannel1", action.getChannelNames().get(0));
    }

    @Test
    public void testPurgeMessageChannelActionParserFailed() {
        Assertions.assertThrows(BeanDefinitionStoreException.class, () -> createApplicationContext("failed"));
    }

    @Test
    public void shouldLookupTestActionParser() {
        Assertions.assertTrue(CitrusNamespaceParserRegistry.lookupBeanParser().containsKey("purge-channel"));
        Assertions.assertEquals(PurgeMessageChannelActionParser.class, CitrusNamespaceParserRegistry.lookupBeanParser().get("purge-channel").getClass());

        Assertions.assertEquals(PurgeMessageChannelActionParser.class, CitrusNamespaceParserRegistry.getBeanParser("purge-channel").getClass());
    }
}
