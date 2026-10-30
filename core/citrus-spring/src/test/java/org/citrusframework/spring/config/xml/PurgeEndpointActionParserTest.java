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

import org.citrusframework.actions.PurgeEndpointAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PurgeEndpointActionParserTest extends AbstractActionParserTest<PurgeEndpointAction> {

    @Test
    public void testPurgeEndpointActionParser() {
        assertActionCount(4);
        assertActionClassAndName(PurgeEndpointAction.class, "purge-endpoint");

        PurgeEndpointAction action = getNextTestActionFromTest();
        Assertions.assertNull(action.getMessageSelector());
        Assertions.assertNotNull(action.getMessageSelectorMap());
        Assertions.assertEquals(0, action.getMessageSelectorMap().size());
        Assertions.assertEquals(0, action.getEndpoints().size());
        Assertions.assertEquals(3, action.getEndpointNames().size());
        Assertions.assertEquals("testEndpoint1", action.getEndpointNames().get(0));
        Assertions.assertEquals("testEndpoint2", action.getEndpointNames().get(1));
        Assertions.assertEquals("testEndpoint3", action.getEndpointNames().get(2));

        action = getNextTestActionFromTest();
        Assertions.assertNull(action.getMessageSelector());
        Assertions.assertNotNull(action.getMessageSelectorMap());
        Assertions.assertEquals(0, action.getMessageSelectorMap().size());
        Assertions.assertEquals(1, action.getEndpoints().size());
        Assertions.assertEquals(3, action.getEndpointNames().size());
        Assertions.assertEquals("testEndpoint1", action.getEndpointNames().get(0));
        Assertions.assertEquals("testEndpoint2", action.getEndpointNames().get(1));
        Assertions.assertEquals("testEndpoint3", action.getEndpointNames().get(2));

        action = getNextTestActionFromTest();
        Assertions.assertEquals("operation = 'sayHello'", action.getMessageSelector());
        Assertions.assertEquals(0, action.getMessageSelectorMap().size());
        Assertions.assertEquals(1, action.getEndpoints().size());
        Assertions.assertEquals(1, action.getEndpointNames().size());
        Assertions.assertEquals("testEndpoint1", action.getEndpointNames().get(0));

        action = getNextTestActionFromTest();
        Assertions.assertNull(action.getMessageSelector());
        Assertions.assertEquals(2, action.getMessageSelectorMap().size());
        Assertions.assertEquals("sayHello", action.getMessageSelectorMap().get("operation"));
        Assertions.assertEquals("12345", action.getMessageSelectorMap().get("id"));
        Assertions.assertEquals(0, action.getEndpoints().size());
        Assertions.assertEquals(1, action.getEndpointNames().size());
        Assertions.assertEquals("testEndpoint1", action.getEndpointNames().get(0));
    }

    @Test
    public void testPurgeEndpointActionParserFailed() {
        assertThrows(BeanDefinitionStoreException.class, () -> {
            try {
                createApplicationContext("failed");
                Assertions.fail("Missing bean creation exception due to empty endpoint attributes");
            } catch (BeanDefinitionStoreException e) {
                Assertions.assertNotNull(e.getRootCause());
                Assertions.assertTrue(e.getRootCause().getMessage().endsWith("Element 'endpoint' must set one of the attributes 'name' or 'ref'"));
                throw e;
            }
        });
    }
}
