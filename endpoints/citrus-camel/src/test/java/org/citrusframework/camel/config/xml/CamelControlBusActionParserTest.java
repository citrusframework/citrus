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

package org.citrusframework.camel.config.xml;

import org.citrusframework.camel.actions.CamelControlBusAction;
import org.citrusframework.testng.AbstractActionParserTest;
import org.apache.camel.CamelContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CamelControlBusActionParserTest extends AbstractActionParserTest<CamelControlBusAction> {

    @Test
    public void testCreateRouteActionParser() {
        assertActionCount(4);
        assertActionClassAndName(CamelControlBusAction.class, "controlbus");

        CamelControlBusAction action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCamelContext());
        Assert.assertEquals(action.getCamelContext(), beanDefinitionContext.getBean("citrusCamelContext", CamelContext.class));
        Assert.assertEquals(action.getRouteId(), "route_1");
        Assert.assertEquals(action.getAction(), "start");
        Assert.assertNull(action.getResult());

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCamelContext());
        Assert.assertEquals(action.getCamelContext(), beanDefinitionContext.getBean("camelContext", CamelContext.class));
        Assert.assertEquals(action.getRouteId(), "route_2");
        Assert.assertEquals(action.getAction(), "status");
        Assert.assertEquals(action.getResult(), "Stopped");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCamelContext());
        Assert.assertEquals(action.getCamelContext(), beanDefinitionContext.getBean("citrusCamelContext", CamelContext.class));
        Assert.assertEquals(action.getLanguageType(), "simple");
        Assert.assertEquals(action.getLanguageExpression(), "${camelContext.stop()}");
        Assert.assertNull(action.getResult());

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCamelContext());
        Assert.assertEquals(action.getCamelContext(), beanDefinitionContext.getBean("camelContext", CamelContext.class));
        Assert.assertEquals(action.getLanguageType(), "simple");
        Assert.assertEquals(action.getLanguageExpression(), "${camelContext.getRouteController().getRouteStatus('route_3')}");
        Assert.assertEquals(action.getResult(), "Started");
    }
}
