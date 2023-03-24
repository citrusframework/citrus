/*
 * Copyright 2006-2015 the original author or authors.
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

import org.citrusframework.actions.PurgeEndpointAction;
import org.citrusframework.testng.AbstractActionParserTest;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class PurgeEndpointActionParserTest extends AbstractActionParserTest<PurgeEndpointAction> {

    @Test
    public void testPurgeEndpointActionParser() {
        assertActionCount(4);
        assertActionClassAndName(PurgeEndpointAction.class, "purge-endpoint");

        PurgeEndpointAction action = getNextTestActionFromTest();
        Assert.assertNull(action.getMessageSelector());
        Assert.assertNotNull(action.getMessageSelectorMap());
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0);
        Assert.assertEquals(action.getEndpoints().size(), 0);
        Assert.assertEquals(action.getEndpointNames().size(), 3);
        Assert.assertEquals(action.getEndpointNames().get(0), "testEndpoint1");
        Assert.assertEquals(action.getEndpointNames().get(1), "testEndpoint2");
        Assert.assertEquals(action.getEndpointNames().get(2), "testEndpoint3");

        action = getNextTestActionFromTest();
        Assert.assertNull(action.getMessageSelector());
        Assert.assertNotNull(action.getMessageSelectorMap());
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0);
        Assert.assertEquals(action.getEndpoints().size(), 1);
        Assert.assertEquals(action.getEndpointNames().size(), 3);
        Assert.assertEquals(action.getEndpointNames().get(0), "testEndpoint1");
        Assert.assertEquals(action.getEndpointNames().get(1), "testEndpoint2");
        Assert.assertEquals(action.getEndpointNames().get(2), "testEndpoint3");

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getMessageSelector(), "operation = 'sayHello'");
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0);
        Assert.assertEquals(action.getEndpoints().size(), 1);
        Assert.assertEquals(action.getEndpointNames().size(), 1);
        Assert.assertEquals(action.getEndpointNames().get(0), "testEndpoint1");

        action = getNextTestActionFromTest();
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getMessageSelectorMap().size(), 2);
        Assert.assertEquals(action.getMessageSelectorMap().get("operation"), "sayHello");
        Assert.assertEquals(action.getMessageSelectorMap().get("id"), "12345");
        Assert.assertEquals(action.getEndpoints().size(), 0);
        Assert.assertEquals(action.getEndpointNames().size(), 1);
        Assert.assertEquals(action.getEndpointNames().get(0), "testEndpoint1");
    }

    @Test(expectedExceptions = BeanDefinitionStoreException.class)
    public void testPurgeEndpointActionParserFailed() {
        try {
            createApplicationContext("failed");
            Assert.fail("Missing bean creation exception due to empty endpoint attributes");
        } catch (BeanDefinitionStoreException e) {
            Assert.assertNotNull(e.getRootCause());
            Assert.assertTrue(e.getRootCause().getMessage().endsWith("Element 'endpoint' must set one of the attributes 'name' or 'ref'"));
            throw e;
        }
    }
}
