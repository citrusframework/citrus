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

package org.citrusframework.dsl.design;

import org.citrusframework.TestCase;
import org.citrusframework.camel.actions.CamelControlBusAction;
import org.citrusframework.camel.actions.CreateCamelRouteAction;
import org.citrusframework.camel.actions.RemoveCamelRouteAction;
import org.citrusframework.camel.actions.StartCamelRouteAction;
import org.citrusframework.camel.actions.StopCamelRouteAction;
import org.citrusframework.dsl.UnitTestSupport;
import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class CamelRouteTestDesignerTest extends UnitTestSupport {

    private DefaultCamelContext camelContext;

    @BeforeMethod
    public void setupCamelContext() throws Exception {
        camelContext = new DefaultCamelContext();
        camelContext.start();
    }

    @AfterMethod(alwaysRun = true)
    public void clearCamelContext() throws Exception {
        if (camelContext != null) {
            camelContext.shutdown();
        }
    }

    @Test
    public void testCreateCamelRouteBuilder() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                camel().context(camelContext).create(new RouteBuilder(camelContext) {
                        @Override
                        public void configure() throws Exception {
                            from("direct:news")
                                    .routeId("route_1")
                                    .setHeader("headline", simple("This is BIG news!"))
                                    .to("mock:news");

                            from("direct:rumors")
                                    .routeId("route_2")
                                    .setHeader("headline", simple("This is just a rumor!"))
                                    .to("mock:rumors");
                        }
                    });
                }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), CreateCamelRouteAction.class);

        CreateCamelRouteAction action = (CreateCamelRouteAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "create-routes");
        Assert.assertEquals(action.getRoutes().size(), 2);
    }

    @Test
    public void testStartCamelRouteBuilder() throws Exception {
        camelContext.addRoutes(new RouteBuilder(camelContext) {
            @Override
            public void configure() throws Exception {
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

        Assert.assertEquals(camelContext.getRouteStatus("route_1"), ServiceStatus.Stopped);
        Assert.assertEquals(camelContext.getRouteStatus("route_2"), ServiceStatus.Stopped);

        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                camel().context(camelContext).start("route_1", "route_2");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), StartCamelRouteAction.class);

        StartCamelRouteAction action = (StartCamelRouteAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "start-routes");
        Assert.assertEquals(action.getRouteIds().size(), 2);
    }

    @Test
    public void testStopCamelRouteBuilder() throws Exception {
        camelContext.addRoutes(new RouteBuilder(camelContext) {
            @Override
            public void configure() throws Exception {
                from("direct:news")
                        .routeId("route_1")
                        .setHeader("headline", simple("This is BIG news!"))
                        .to("mock:news");

                from("direct:rumors")
                        .routeId("route_2")
                        .setHeader("headline", simple("This is just a rumor!"))
                        .to("mock:rumors");
            }
        });

        Assert.assertEquals(camelContext.getRouteStatus("route_1"), ServiceStatus.Started);
        Assert.assertEquals(camelContext.getRouteStatus("route_2"), ServiceStatus.Started);

        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                camel().context(camelContext).stop("route_1", "route_2");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), StopCamelRouteAction.class);

        StopCamelRouteAction action = (StopCamelRouteAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "stop-routes");
        Assert.assertEquals(action.getRouteIds().size(), 2);
    }

    @Test
    public void testRemoveCamelRouteBuilder() throws Exception {
        camelContext.addRoutes(new RouteBuilder(camelContext) {
            @Override
            public void configure() throws Exception {
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

        Assert.assertEquals(camelContext.getRouteDefinitions().size(), 2);
        Assert.assertEquals(camelContext.getRouteStatus("route_1"), ServiceStatus.Stopped);
        Assert.assertEquals(camelContext.getRouteStatus("route_2"), ServiceStatus.Stopped);

        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                camel().context(camelContext).remove("route_1", "route_2");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), RemoveCamelRouteAction.class);

        RemoveCamelRouteAction action = (RemoveCamelRouteAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "remove-routes");
        Assert.assertEquals(action.getRouteIds().size(), 2);
    }

    @Test
    public void testDefaultCamelContextBuilder() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                camel().create(new RouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        from("direct:news")
                                .routeId("route_1")
                                .setHeader("headline", simple("This is BIG news!"))
                                .to("mock:news");

                        from("direct:rumors")
                                .routeId("route_2")
                                .setHeader("headline", simple("This is just a rumor!"))
                                .to("mock:rumors");
                    }
                });
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), CreateCamelRouteAction.class);

        CreateCamelRouteAction action = (CreateCamelRouteAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "create-routes");
        Assert.assertEquals(action.getRoutes().size(), 2);
    }

    @Test
    public void testCamelControlBusBuilder() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                camel().controlBus()
                        .route("default_route", "status")
                        .result(ServiceStatus.Started);

                camel().controlBus()
                        .language("${camelContext.getRouteStatus('default_route')}");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), CamelControlBusAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), CamelControlBusAction.class);

        CamelControlBusAction action = (CamelControlBusAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "controlbus");
        Assert.assertEquals(action.getRouteId(), "default_route");
        Assert.assertEquals(action.getAction(), "status");
        Assert.assertEquals(action.getResult(), "Started");

        action = (CamelControlBusAction) test.getActions().get(1);
        Assert.assertEquals(action.getName(), "controlbus");
        Assert.assertEquals(action.getLanguageType(), "simple");
        Assert.assertEquals(action.getLanguageExpression(), "${camelContext.getRouteStatus('default_route')}");
        Assert.assertNull(action.getResult());
    }
}
