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

package org.citrusframework.camel.xml;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.actions.CamelControlBusAction;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ControlBusTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadCamelActions() throws Exception {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/xml/camel-control-bus.citrus.it.xml");

        CamelContext citrusCamelContext = new DefaultCamelContext();
        citrusCamelContext.addRoutes(new RouteBuilder(citrusCamelContext) {
            @Override
            public void configure() {
                from("direct:hello")
                        .routeId("route_1")
                        .to("log:info");

                from("direct:goodbye")
                        .routeId("route_2")
                        .autoStartup(false)
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
        Assertions.assertEquals("CamelControlBusTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(4L, result.getActionCount());
        Assertions.assertEquals(CamelControlBusAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("camel:controlbus", result.getTestAction(0).getName());

        int actionIndex = 0;

        CamelControlBusAction action = (CamelControlBusAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(action.getCamelContext());
        Assertions.assertEquals(action.getCamelContext(), context.getReferenceResolver().resolve(CamelSettings.getContextName(), CamelContext.class));
        Assertions.assertEquals("route_1", action.getRouteId());
        Assertions.assertEquals("start", action.getAction());
        Assertions.assertNull(action.getResult());

        action = (CamelControlBusAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(action.getCamelContext());
        Assertions.assertEquals(context.getReferenceResolver().resolve("camelContext", CamelContext.class), action.getCamelContext());
        Assertions.assertEquals("route_2", action.getRouteId());
        Assertions.assertEquals("status", action.getAction());
        Assertions.assertEquals("Stopped", action.getResult());

        action = (CamelControlBusAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(action.getCamelContext());
        Assertions.assertEquals(context.getReferenceResolver().resolve("camelContext", CamelContext.class), action.getCamelContext());
        Assertions.assertEquals("simple", action.getLanguageType());
        Assertions.assertEquals("${camelContext.getRouteController().getRouteStatus('route_3')}", action.getLanguageExpression());
        Assertions.assertEquals("Started", action.getResult());

        action = (CamelControlBusAction) result.getTestAction(actionIndex);
        Assertions.assertNotNull(action.getCamelContext());
        Assertions.assertEquals(action.getCamelContext(), context.getReferenceResolver().resolve(CamelSettings.getContextName(), CamelContext.class));
        Assertions.assertEquals("simple", action.getLanguageType());
        Assertions.assertEquals("${camelContext.stop()}", action.getLanguageExpression());
        Assertions.assertNull(action.getResult());

    }
}
