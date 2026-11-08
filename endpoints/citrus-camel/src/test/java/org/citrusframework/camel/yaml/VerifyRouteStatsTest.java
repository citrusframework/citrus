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
import org.citrusframework.camel.actions.CamelVerifyRouteStatsAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VerifyRouteStatsTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadCamelActions() throws Exception {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/yaml/camel-verify-route-stats.citrus.it.yaml");

        CamelContext citrusCamelContext = new DefaultCamelContext();
        citrusCamelContext.addRoutes(new RouteBuilder(citrusCamelContext) {
            @Override
            public void configure() {
                from("direct:hello")
                        .routeId("route_1")
                        .to("log:info");
            }
        });

        citrusCamelContext.start();

        context.getReferenceResolver().bind(CamelSettings.getContextName(), citrusCamelContext);
        context.getReferenceResolver().bind("camelContext", citrusCamelContext);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CamelVerifyRouteStatsTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(CamelVerifyRouteStatsAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("camel:verify-route-stats", result.getTestAction(0).getName());

        int actionIndex = 0;

        CamelVerifyRouteStatsAction action = (CamelVerifyRouteStatsAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(action.getCamelContext());
        Assertions.assertEquals(action.getCamelContext(), context.getReferenceResolver().resolve(CamelSettings.getContextName(), CamelContext.class));
        Assertions.assertEquals("route_1", action.getRouteId());
        Assertions.assertEquals(Long.valueOf(0), action.getCompleted());
        Assertions.assertNull(action.getFailed());

        action = (CamelVerifyRouteStatsAction) result.getTestAction(actionIndex);
        Assertions.assertNotNull(action.getCamelContext());
        Assertions.assertEquals(context.getReferenceResolver().resolve("camelContext", CamelContext.class), action.getCamelContext());
        Assertions.assertEquals("route_1", action.getRouteId());
        Assertions.assertEquals(Long.valueOf(0), action.getCompleted());
        Assertions.assertEquals(Long.valueOf(0), action.getFailed());
    }
}
