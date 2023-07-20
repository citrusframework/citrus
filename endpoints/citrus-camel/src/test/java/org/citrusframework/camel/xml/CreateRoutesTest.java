/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.camel.xml;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.camel.actions.CreateCamelRouteAction;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class CreateRoutesTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadCamelActions() throws Exception {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/xml/camel-create-routes-test.xml");

        CamelContext citrusCamelContext = new DefaultCamelContext();
        citrusCamelContext.start();

        context.getReferenceResolver().bind("citrusCamelContext", citrusCamelContext);
        context.getReferenceResolver().bind("camelContext", citrusCamelContext);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CamelCreateRouteTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CreateCamelRouteAction.class);
        Assert.assertEquals(result.getTestAction(0).getName(), "create-routes");

        int actionIndex = 0;

        CreateCamelRouteAction action = (CreateCamelRouteAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(action.getCamelContext());
        Assert.assertEquals(action.getCamelContext(), context.getReferenceResolver().resolve("citrusCamelContext", CamelContext.class));
        Assert.assertNull(action.getRouteContext());
        Assert.assertNotNull(action.getRoutes());
        Assert.assertEquals(action.getRoutes().size(), 2L);
        Assert.assertEquals(action.getRoutes().get(0).getRouteId(), "route_1");
        Assert.assertEquals(action.getRoutes().get(1).getRouteId(), "route_2");

        action = (CreateCamelRouteAction) result.getTestAction(actionIndex);
        Assert.assertNotNull(action.getCamelContext());
        Assert.assertEquals(action.getCamelContext(), context.getReferenceResolver().resolve("camelContext", CamelContext.class));
        Assert.assertNull(action.getRouteContext());
        Assert.assertNotNull(action.getRoutes());
        Assert.assertEquals(action.getRoutes().size(), 1L);
        Assert.assertEquals(action.getRoutes().get(0).getEndpointUrl(), "direct:test3");
    }
}
