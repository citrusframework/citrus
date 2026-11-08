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

package org.citrusframework.camel.actions.dsl;

import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCase;
import org.citrusframework.camel.UnitTestSupport;
import org.citrusframework.camel.actions.CamelControlBusAction;
import org.citrusframework.camel.actions.CreateCamelRouteAction;
import org.citrusframework.camel.actions.RemoveCamelRouteAction;
import org.citrusframework.camel.actions.StopCamelRouteAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @since 2.4
 */
public class CamelRouteTestActionRunnerTest extends UnitTestSupport implements TestActionSupport {

    private DefaultCamelContext camelContext;

    @BeforeEach
    public void setupCamelContext() {
        camelContext = new DefaultCamelContext();
        camelContext.start();
    }

    @AfterEach
    public void clearCamelContext() {
        if (camelContext != null) {
            camelContext.shutdown();
        }
    }

    @Test
    public void testCreateCamelRouteBuilder() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);

        builder.$(camel().camelContext(camelContext)
                .route()
                .create(new RouteBuilder(camelContext) {
            @Override
            public void configure() {
                from("direct:news")
                        .routeId("route_1")
                        .setHeader("headline", simple("This is BIG news!"))
                        .to("mock:news");

                from("direct:rumors")
                        .routeId("route_2")
                        .setHeader("headline", simple("This is just a rumor!"))
                        .to("mock:rumors");
            }
        }));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(CreateCamelRouteAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(CreateCamelRouteAction.class, test.getActiveAction().getClass());

        CreateCamelRouteAction action = (CreateCamelRouteAction) test.getActions().get(0);
        Assertions.assertEquals("camel:create-routes", action.getName());
        Assertions.assertEquals(2, action.getRoutes().size());

        Assertions.assertEquals(2, camelContext.getRouteDefinitions().size());
    }

    @Test
    public void testStartCamelRouteBuilder() throws Exception {
        camelContext.addRoutes(new RouteBuilder(camelContext) {
            @Override
            public void configure() {
                from("direct:news")
                        .routeId("route_1")
                        .autoStartup(false)
                        .setHeader("headline", simple("This is BIG news!"))
                        .to("mock:news");

                from("direct:rumors")
                        .routeId("route_2")
                        .autoStartup(false)
                        .setHeader("headline", simple("This is just a rumor!"))
                        .to("mock:rumors");
            }
        });

        Assertions.assertEquals(ServiceStatus.Stopped, camelContext.getRouteStatus("route_1"));
        Assertions.assertEquals(ServiceStatus.Stopped, camelContext.getRouteStatus("route_2"));

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(camel().camelContext(camelContext).route().start("route_1", "route_2"));
        builder = new DefaultTestCaseRunner(context);
        builder.$(camel().camelContext(camelContext).route().stop("route_1", "route_2"));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(StopCamelRouteAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(StopCamelRouteAction.class, test.getActiveAction().getClass());

        StopCamelRouteAction action = (StopCamelRouteAction) test.getActions().get(0);
        Assertions.assertEquals("camel:stop-routes", action.getName());
        Assertions.assertEquals(2, action.getRouteIds().size());

        Assertions.assertEquals(2, camelContext.getRouteDefinitions().size());
        Assertions.assertEquals(ServiceStatus.Stopped, camelContext.getRouteStatus("route_1"));
        Assertions.assertEquals(ServiceStatus.Stopped, camelContext.getRouteStatus("route_2"));
    }

    @Test
    public void testRemoveCamelRouteBuilder() throws Exception {
        camelContext.addRoutes(new RouteBuilder(camelContext) {
            @Override
            public void configure() {
                from("direct:news")
                        .routeId("route_1")
                        .autoStartup(false)
                        .setHeader("headline", simple("This is BIG news!"))
                        .to("mock:news");

                from("direct:rumors")
                        .routeId("route_2")
                        .autoStartup(false)
                        .setHeader("headline", simple("This is just a rumor!"))
                        .to("mock:rumors");
            }
        });

        Assertions.assertEquals(2, camelContext.getRouteDefinitions().size());
        Assertions.assertEquals(ServiceStatus.Stopped, camelContext.getRouteStatus("route_1"));
        Assertions.assertEquals(ServiceStatus.Stopped, camelContext.getRouteStatus("route_2"));

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(camel().camelContext(camelContext).route().remove("route_1", "route_2"));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(RemoveCamelRouteAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(RemoveCamelRouteAction.class, test.getActiveAction().getClass());

        RemoveCamelRouteAction action = (RemoveCamelRouteAction) test.getActions().get(0);
        Assertions.assertEquals("camel:remove-routes", action.getName());
        Assertions.assertEquals(2, action.getRouteIds().size());

        Assertions.assertEquals(0, camelContext.getRouteDefinitions().size());
    }

    @Test
    public void testDefaultCamelContextBuilder() {
        context.getReferenceResolver().bind("camelContext", camelContext);

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(camel().route().create(new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:news")
                        .routeId("route_1")
                        .setHeader("headline", simple("This is BIG news!"))
                        .to("mock:news");

                from("direct:rumors")
                        .routeId("route_2")
                        .setHeader("headline", simple("This is just a rumor!"))
                        .to("mock:rumors");
            }
        }));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(CreateCamelRouteAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(CreateCamelRouteAction.class, test.getActiveAction().getClass());

        CreateCamelRouteAction action = (CreateCamelRouteAction) test.getActions().get(0);
        Assertions.assertEquals("camel:create-routes", action.getName());
        Assertions.assertEquals(2, action.getRoutes().size());

        Assertions.assertEquals(2, camelContext.getRoutes().size());
    }

    @Test
    public void testCamelControlBusBuilder() throws Exception {
        camelContext.addRoutes(new RouteBuilder(camelContext) {
            @Override
            public void configure() {
                from("direct:news")
                        .routeId("route_1")
                        .autoStartup(false)
                        .setHeader("headline", simple("This is BIG news!"))
                        .to("mock:news");

                from("direct:rumors")
                        .routeId("route_2")
                        .autoStartup(false)
                        .setHeader("headline", simple("This is just a rumor!"))
                        .to("mock:rumors");
            }
        });

        Assertions.assertEquals(ServiceStatus.Stopped, camelContext.getRouteStatus("route_1"));
        Assertions.assertEquals(ServiceStatus.Stopped, camelContext.getRouteStatus("route_2"));

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(camel().camelContext(camelContext)
                .controlBus()
                    .route("route_1", "status")
                    .result(ServiceStatus.Stopped));

        builder.$(camel().camelContext(camelContext)
                .controlBus()
                .route("route_1", "start"));

        builder.$(camel().camelContext(camelContext)
                .controlBus()
                .simple("${camelContext.getRouteStatus('route_1')}")
                .result(ServiceStatus.Started));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(3, test.getActionCount());
        Assertions.assertEquals(CamelControlBusAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(CamelControlBusAction.class, test.getActiveAction().getClass());

        CamelControlBusAction action = (CamelControlBusAction) test.getActions().get(0);
        Assertions.assertEquals("camel:controlbus", action.getName());
        Assertions.assertEquals("route_1", action.getRouteId());
        Assertions.assertEquals("status", action.getAction());
        Assertions.assertEquals("Stopped", action.getResult());

        action = (CamelControlBusAction) test.getActions().get(1);
        Assertions.assertEquals("camel:controlbus", action.getName());
        Assertions.assertEquals("route_1", action.getRouteId());
        Assertions.assertEquals("start", action.getAction());
        Assertions.assertNull(action.getResult());

        action = (CamelControlBusAction) test.getActions().get(2);
        Assertions.assertEquals("camel:controlbus", action.getName());
        Assertions.assertEquals("simple", action.getLanguageType());
        Assertions.assertEquals("${camelContext.getRouteStatus('route_1')}", action.getLanguageExpression());
        Assertions.assertEquals("Started", action.getResult());

        Assertions.assertEquals(2, camelContext.getRouteDefinitions().size());
        Assertions.assertEquals(ServiceStatus.Started, camelContext.getRouteStatus("route_1"));
        Assertions.assertEquals(ServiceStatus.Stopped, camelContext.getRouteStatus("route_2"));
    }
}
