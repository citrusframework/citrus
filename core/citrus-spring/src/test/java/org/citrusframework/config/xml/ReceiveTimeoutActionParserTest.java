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

package org.citrusframework.config.xml;

import org.citrusframework.endpoint.Endpoint;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.citrusframework.actions.ReceiveTimeoutAction;
import org.citrusframework.testng.AbstractActionParserTest;

/**
 * @author Christoph Deppisch
 */
public class ReceiveTimeoutActionParserTest extends AbstractActionParserTest<ReceiveTimeoutAction> {

    @Test
    public void testReceiveTimeoutActionParser() {
        assertActionCount(4);
        assertActionClassAndName(ReceiveTimeoutAction.class, "expect-timeout:myMessageEndpoint");

        ReceiveTimeoutAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getTimeout(), 1000L);
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());
        Assert.assertNull(action.getMessageSelector());

        action = getNextTestActionFromTest();
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getTimeout(), 10000L);
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());
        Assert.assertEquals(action.getMessageSelector(), "operation='Test'");
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0L);

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getTimeout(), 10000L);
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getMessageSelectorMap().size(), 1L);
        Assert.assertEquals(action.getMessageSelectorMap().get("operation"), "Test");
    }

    @Test
    public void testEmptyEndpointReferenceTemplate() {
        try {
            createApplicationContext("failed");
            Assert.fail("Missing bean creation exception due to missing endpoint reference");
        } catch (BeanDefinitionStoreException e) {
            Assert.assertTrue(e.getCause().getMessage().startsWith("Missing proper message endpoint reference"));
        }
    }
}
