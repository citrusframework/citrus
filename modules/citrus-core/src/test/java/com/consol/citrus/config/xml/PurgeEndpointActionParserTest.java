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

package com.consol.citrus.config.xml;

import com.consol.citrus.actions.PurgeEndpointAction;
import com.consol.citrus.testng.AbstractActionParserTest;
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
        Assert.assertNotNull(action.getMessageSelector());
        Assert.assertEquals(action.getEndpoints().size(), 0);
        Assert.assertEquals(action.getEndpointNames().size(), 3);
        Assert.assertEquals(action.getEndpointNames().get(0), "testEndpoint1");
        Assert.assertEquals(action.getEndpointNames().get(1), "testEndpoint2");
        Assert.assertEquals(action.getEndpointNames().get(2), "testEndpoint3");
        
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getMessageSelector());
        Assert.assertEquals(action.getEndpoints().size(), 1);
        Assert.assertEquals(action.getEndpointNames().size(), 3);
        Assert.assertEquals(action.getEndpointNames().get(0), "testEndpoint1");
        Assert.assertEquals(action.getEndpointNames().get(1), "testEndpoint2");
        Assert.assertEquals(action.getEndpointNames().get(2), "testEndpoint3");
        
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getMessageSelectorString(), "operation = 'sayHello'");
        Assert.assertEquals(action.getMessageSelector().size(), 0);
        Assert.assertEquals(action.getEndpoints().size(), 1);
        Assert.assertEquals(action.getEndpointNames().size(), 1);
        Assert.assertEquals(action.getEndpointNames().get(0), "testEndpoint1");

        action = getNextTestActionFromTest();
        Assert.assertNull(action.getMessageSelectorString());
        Assert.assertEquals(action.getMessageSelector().size(), 2);
        Assert.assertEquals(action.getMessageSelector().get("operation"), "sayHello");
        Assert.assertEquals(action.getMessageSelector().get("id"), "12345");
        Assert.assertEquals(action.getEndpoints().size(), 0);
        Assert.assertEquals(action.getEndpointNames().size(), 1);
        Assert.assertEquals(action.getEndpointNames().get(0), "testEndpoint1");
    }
    
    @Test
    public void testPurgeEndpointActionParserFailed() {
        try {
            createApplicationContext("failed");
            Assert.fail("Missing bean creation exception due to empty endpoint attributes");
        } catch (BeanDefinitionStoreException e) {
            Assert.assertTrue(e.getMessage().endsWith("Element 'endpoint' must set one of the attributes 'name' or 'ref'"));
        }
    }
}
