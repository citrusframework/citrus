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

import org.citrusframework.camel.actions.RemoveCamelRouteAction;
import org.citrusframework.testng.AbstractActionParserTest;
import org.apache.camel.CamelContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RemoveCamelRouteActionParserTest extends AbstractActionParserTest<RemoveCamelRouteAction> {

    @Test
    public void testRemoveRouteActionParser() {
        assertActionCount(2);
        assertActionClassAndName(RemoveCamelRouteAction.class, "remove-routes");

        RemoveCamelRouteAction action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCamelContext());
        Assert.assertEquals(action.getCamelContext(), beanDefinitionContext.getBean("citrusCamelContext", CamelContext.class));
        Assert.assertEquals(action.getRouteIds().size(), 1);

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCamelContext());
        Assert.assertEquals(action.getCamelContext(), beanDefinitionContext.getBean("camelContext", CamelContext.class));
        Assert.assertEquals(action.getRouteIds().size(), 2);
    }
}
