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

package org.citrusframework.config.xml;

import org.citrusframework.actions.PurgeMessageChannelAction;
import org.citrusframework.config.CitrusNamespaceParserRegistry;
import org.citrusframework.testng.AbstractActionParserTest;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class PurgeMessageChannelActionParserTest extends AbstractActionParserTest<PurgeMessageChannelAction> {

    @Test
    public void testPurgeMessageChannelActionParser() {
        assertActionCount(3);
        assertActionClassAndName(PurgeMessageChannelAction.class, "purge-channel");

        PurgeMessageChannelAction action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getMessageSelector());
        Assert.assertEquals(action.getChannels().size(), 0);
        Assert.assertEquals(action.getChannelNames().size(), 3);
        Assert.assertEquals(action.getChannelNames().get(0), "testChannel1");
        Assert.assertEquals(action.getChannelNames().get(1), "testChannel2");
        Assert.assertEquals(action.getChannelNames().get(2), "testChannel3");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getMessageSelector());
        Assert.assertEquals(action.getChannels().size(), 1);
        Assert.assertEquals(action.getChannelNames().size(), 3);
        Assert.assertEquals(action.getChannelNames().get(0), "testChannel1");
        Assert.assertEquals(action.getChannelNames().get(1), "testChannel2");
        Assert.assertEquals(action.getChannelNames().get(2), "testChannel3");

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getMessageSelector(), beanDefinitionContext.getBean("testMessageSelector"));
        Assert.assertEquals(action.getChannels().size(), 1);
        Assert.assertEquals(action.getChannelNames().size(), 1);
        Assert.assertEquals(action.getChannelNames().get(0), "testChannel1");
    }

    @Test(expectedExceptions = BeanDefinitionStoreException.class)
    public void testPurgeMessageChannelActionParserFailed() {
        createApplicationContext("failed");
    }

    @Test
    public void shouldLookupTestActionParser() {
        Assert.assertTrue(CitrusNamespaceParserRegistry.lookupBeanParser().containsKey("purge-channel"));
        Assert.assertEquals(CitrusNamespaceParserRegistry.lookupBeanParser().get("purge-channel").getClass(), PurgeMessageChannelActionParser.class);

        Assert.assertEquals(CitrusNamespaceParserRegistry.getBeanParser("purge-channel").getClass(), PurgeMessageChannelActionParser.class);
    }
}
