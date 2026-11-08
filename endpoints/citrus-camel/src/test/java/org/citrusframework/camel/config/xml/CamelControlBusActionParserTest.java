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

package org.citrusframework.camel.config.xml;

import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.actions.CamelControlBusAction;
import org.apache.camel.CamelContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CamelControlBusActionParserTest extends org.citrusframework.junit.jupiter.AbstractActionParserTest<CamelControlBusAction> {

    @Test
    public void testCreateRouteActionParser() {
        assertActionCount(4);
        assertActionClassAndName(CamelControlBusAction.class, "camel:controlbus");

        CamelControlBusAction action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCamelContext());
        Assertions.assertEquals(action.getCamelContext(), beanDefinitionContext.getBean(CamelSettings.getContextName(), CamelContext.class));
        Assertions.assertEquals("route_1", action.getRouteId());
        Assertions.assertEquals("start", action.getAction());
        Assertions.assertNull(action.getResult());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCamelContext());
        Assertions.assertEquals(beanDefinitionContext.getBean("citrusCamelContext", CamelContext.class), action.getCamelContext());
        Assertions.assertEquals("route_2", action.getRouteId());
        Assertions.assertEquals("status", action.getAction());
        Assertions.assertEquals("Stopped", action.getResult());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCamelContext());
        Assertions.assertEquals(action.getCamelContext(), beanDefinitionContext.getBean(CamelSettings.getContextName(), CamelContext.class));
        Assertions.assertEquals("simple", action.getLanguageType());
        Assertions.assertEquals("${camelContext.stop()}", action.getLanguageExpression());
        Assertions.assertNull(action.getResult());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCamelContext());
        Assertions.assertEquals(beanDefinitionContext.getBean("citrusCamelContext", CamelContext.class), action.getCamelContext());
        Assertions.assertEquals("simple", action.getLanguageType());
        Assertions.assertEquals("${camelContext.getRouteController().getRouteStatus('route_3')}", action.getLanguageExpression());
        Assertions.assertEquals("Started", action.getResult());
    }
}
