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

package org.citrusframework.spring.config.xml;

import org.citrusframework.endpoint.Endpoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;

import org.citrusframework.actions.ReceiveTimeoutAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;

public class ReceiveTimeoutActionParserTest extends AbstractActionParserTest<ReceiveTimeoutAction> {

    @Test
    public void testReceiveTimeoutActionParser() {
        assertActionCount(4);
        assertActionClassAndName(ReceiveTimeoutAction.class, "expect-timeout:myMessageEndpoint");

        ReceiveTimeoutAction action = getNextTestActionFromTest();
        Assertions.assertEquals(1000L, action.getTimeout());
        Assertions.assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());
        Assertions.assertNull(action.getMessageSelector());

        action = getNextTestActionFromTest();
        Assertions.assertNull(action.getEndpoint());
        Assertions.assertEquals("channel:myMessageEndpoint", action.getEndpointUri());

        action = getNextTestActionFromTest();
        Assertions.assertEquals(10000L, action.getTimeout());
        Assertions.assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());
        Assertions.assertEquals("operation='Test'", action.getMessageSelector());
        Assertions.assertEquals(0L, action.getMessageSelectorMap().size());

        action = getNextTestActionFromTest();
        Assertions.assertEquals(10000L, action.getTimeout());
        Assertions.assertEquals(beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getEndpointUri());
        Assertions.assertNull(action.getMessageSelector());
        Assertions.assertEquals(1L, action.getMessageSelectorMap().size());
        Assertions.assertEquals("Test", action.getMessageSelectorMap().get("operation"));
    }

    @Test
    public void testEmptyEndpointReferenceTemplate() {
        try {
            createApplicationContext("failed");
            Assertions.fail("Missing bean creation exception due to missing endpoint reference");
        } catch (BeanDefinitionStoreException e) {
            Assertions.assertTrue(e.getCause().getMessage().startsWith("Missing proper message endpoint reference"));
        }
    }
}
