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

package org.citrusframework.camel.yaml;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.actions.CreateCamelRouteAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateRoutesTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadCamelActions() throws Exception {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/yaml/camel-create-route-test.yaml");

        CamelContext citrusCamelContext = new DefaultCamelContext();
        citrusCamelContext.start();

        context.getReferenceResolver().bind(CamelSettings.getContextName(), citrusCamelContext);
        context.getReferenceResolver().bind("camelContext", citrusCamelContext);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CamelCreateRouteTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 3L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CreateCamelRouteAction.class);
        Assert.assertEquals(result.getTestAction(0).getName(), "create-routes");

        int actionIndex = 0;

        CreateCamelRouteAction action = (CreateCamelRouteAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(action.getCamelContext());
        Assert.assertEquals(action.getCamelContext(), context.getReferenceResolver().resolve(CamelSettings.getContextName(), CamelContext.class));
        Assert.assertEquals(action.getRouteSpec().replaceAll("\\s", ""), ("<routeContext xmlns=\"http://camel.apache.org/schema/spring\">" +
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

        action = (CreateCamelRouteAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(action.getCamelContext());
        Assert.assertEquals(action.getCamelContext(), context.getReferenceResolver().resolve("camelContext", CamelContext.class));
        Assert.assertEquals(action.getRouteSpec().replaceAll("\\s", ""), ("<route>" +
                        "<from uri=\"direct:test3\"/>" +
                        "<to uri=\"mock:test3\"/>" +
                    "</route>").replaceAll("\\s", ""));
        Assert.assertEquals(action.getRoutes().size(), 0);

        action = (CreateCamelRouteAction) result.getTestAction(actionIndex);
        Assert.assertNotNull(action.getCamelContext());
        Assert.assertEquals(action.getCamelContext(), context.getReferenceResolver().resolve("camelContext", CamelContext.class));
        Assert.assertEquals(action.getRouteSpec().replaceAll("\\s", ""), ("""
          - from:
              uri: direct:test4
              steps:
                - to:
                    uri: mock:test4
        """).replaceAll("\\s", ""));
        Assert.assertEquals(action.getRoutes().size(), 0);

    }
}
