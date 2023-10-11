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

import org.apache.camel.CamelContext;
import org.citrusframework.camel.actions.CreateCamelRouteAction;
import org.citrusframework.testng.AbstractActionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateCamelRouteActionParserTest extends AbstractActionParserTest<CreateCamelRouteAction> {

    @Test
    public void testCreateRouteActionParser() {
        assertActionCount(2);
        assertActionClassAndName(CreateCamelRouteAction.class, "create-routes");

        CreateCamelRouteAction action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCamelContext());
        Assert.assertEquals(action.getCamelContext(), beanDefinitionContext.getBean("citrusCamelContext", CamelContext.class));
        Assert.assertEquals(action.getRouteContext().replaceAll("\\s", ""), ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<routeContext xmlns=\"http://camel.apache.org/schema/spring\">" +
                    "<route id=\"route_1\">" +
                        "<from uri=\"direct:test1\"/>" +
                        "<to uri=\"mock:test1\"/>" +
                    "</route>" +
                    "<route id=\"route_2\">" +
                        "<from uri=\"direct:test2\"/>" +
                        "<to uri=\"mock:test2\"/>" +
                    "</route>" +
                "</routeContext>").replaceAll("\\s", ""));
        Assert.assertEquals(action.getRoutes().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCamelContext());
        Assert.assertEquals(action.getCamelContext(), beanDefinitionContext.getBean("camelContext", CamelContext.class));
        Assert.assertEquals(action.getRouteContext().replaceAll("\\s", ""), ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<routeContext xmlns=\"http://camel.apache.org/schema/spring\">" +
                    "<route>" +
                        "<from uri=\"direct:test3\"/>" +
                        "<to uri=\"mock:test3\"/>" +
                    "</route>" +
                "</routeContext>").replaceAll("\\s", ""));
        Assert.assertEquals(action.getRoutes().size(), 0);
    }
}
