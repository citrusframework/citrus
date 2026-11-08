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
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.actions.StopCamelRouteAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StopRouteTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadCamelActions() throws Exception {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/yaml/camel-stop-route.citrus.it.yaml");

        CamelContext citrusCamelContext = new DefaultCamelContext();
        citrusCamelContext.addRoutes(new RouteBuilder(citrusCamelContext) {
            @Override
            public void configure() {
                from("direct:hello")
                        .routeId("route_1")
                        .to("log:info");

                from("direct:goodbye")
                        .routeId("route_2")
                        .to("log:info");

                from("direct:goodnight")
                        .routeId("route_3")
                        .to("log:info");
            }
        });

        citrusCamelContext.start();

        context.getReferenceResolver().bind(CamelSettings.getContextName(), citrusCamelContext);
        context.getReferenceResolver().bind("camelContext", citrusCamelContext);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CamelStopRouteTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(StopCamelRouteAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("camel:stop-routes", result.getTestAction(0).getName());

        int actionIndex = 0;

        StopCamelRouteAction action = (StopCamelRouteAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(action.getCamelContext());
        Assertions.assertEquals(action.getCamelContext(), context.getReferenceResolver().resolve(CamelSettings.getContextName(), CamelContext.class));
        Assertions.assertEquals(1, action.getRouteIds().size());

        action = (StopCamelRouteAction) result.getTestAction(actionIndex);
        Assertions.assertNotNull(action.getCamelContext());
        Assertions.assertEquals(context.getReferenceResolver().resolve("camelContext", CamelContext.class), action.getCamelContext());
        Assertions.assertEquals(2, action.getRouteIds().size());

    }
}
