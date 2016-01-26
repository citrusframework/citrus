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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.camel.actions.*;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.CamelRouteActionBuilder;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.apache.camel.CamelContext;
import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.SimpleBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class CamelRouteTestRunnerTest extends AbstractTestNGUnitTest {

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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                camel(new BuilderSupport<CamelRouteActionBuilder>() {
                    @Override
                    public void configure(CamelRouteActionBuilder builder) {
                        builder.context(camelContext).create(new RouteBuilder(camelContext) {
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
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), CreateCamelRouteAction.class);
        Assert.assertEquals(test.getLastExecutedAction().getClass(), DelegatingTestAction.class);

        CreateCamelRouteAction action = (CreateCamelRouteAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "create-routes");
        Assert.assertEquals(action.getRoutes().size(), 2);

        Assert.assertEquals(camelContext.getRouteDefinitions().size(), 2);
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

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                camel(new BuilderSupport<CamelRouteActionBuilder>() {
                    @Override
                    public void configure(CamelRouteActionBuilder builder) {
                        builder.context(camelContext).start("route_1", "route_2");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), StartCamelRouteAction.class);
        Assert.assertEquals(test.getLastExecutedAction().getClass(), DelegatingTestAction.class);

        StartCamelRouteAction action = (StartCamelRouteAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "start-routes");
        Assert.assertEquals(action.getRouteIds().size(), 2);

        Assert.assertEquals(camelContext.getRouteDefinitions().size(), 2);
        Assert.assertEquals(camelContext.getRouteStatus("route_1"), ServiceStatus.Started);
        Assert.assertEquals(camelContext.getRouteStatus("route_2"), ServiceStatus.Started);
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

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                camel(new BuilderSupport<CamelRouteActionBuilder>() {
                    @Override
                    public void configure(CamelRouteActionBuilder builder) {
                        builder.context(camelContext).stop("route_1", "route_2");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), StopCamelRouteAction.class);
        Assert.assertEquals(test.getLastExecutedAction().getClass(), DelegatingTestAction.class);

        StopCamelRouteAction action = (StopCamelRouteAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "stop-routes");
        Assert.assertEquals(action.getRouteIds().size(), 2);

        Assert.assertEquals(camelContext.getRouteDefinitions().size(), 2);
        Assert.assertEquals(camelContext.getRouteStatus("route_1"), ServiceStatus.Stopped);
        Assert.assertEquals(camelContext.getRouteStatus("route_2"), ServiceStatus.Stopped);
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

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                camel(new BuilderSupport<CamelRouteActionBuilder>() {
                    @Override
                    public void configure(CamelRouteActionBuilder builder) {
                        builder.context(camelContext).remove("route_1", "route_2");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), RemoveCamelRouteAction.class);
        Assert.assertEquals(test.getLastExecutedAction().getClass(), DelegatingTestAction.class);

        RemoveCamelRouteAction action = (RemoveCamelRouteAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "remove-routes");
        Assert.assertEquals(action.getRouteIds().size(), 2);

        Assert.assertEquals(camelContext.getRouteDefinitions().size(), 0);
    }

    @Test
    public void testDefaultCamelContextBuilder() {
        CamelContext defaultContext = applicationContext.getBean(CamelContext.class);
        Assert.assertEquals(defaultContext.getRouteDefinitions().size(), 1);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                camel(new BuilderSupport<CamelRouteActionBuilder>() {
                    @Override
                    public void configure(CamelRouteActionBuilder builder) {
                        builder.create(new RouteBuilder() {
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
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), CreateCamelRouteAction.class);
        Assert.assertEquals(test.getLastExecutedAction().getClass(), DelegatingTestAction.class);

        CreateCamelRouteAction action = (CreateCamelRouteAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "create-routes");
        Assert.assertEquals(action.getRoutes().size(), 2);

        Assert.assertEquals(defaultContext.getRouteDefinitions().size(), 3);
    }

    @Test
    public void testCamelControlBusBuilder() throws Exception {
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

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                camel(new BuilderSupport<CamelRouteActionBuilder>() {
                    @Override
                    public void configure(CamelRouteActionBuilder builder) {
                        builder.context(camelContext)
                                .controlBus()
                                    .route("route_1", "status")
                                    .result(ServiceStatus.Stopped);
                    }
                });

                camel(new BuilderSupport<CamelRouteActionBuilder>() {
                    @Override
                    public void configure(CamelRouteActionBuilder builder) {
                        builder.context(camelContext)
                                .controlBus()
                                .route("route_1", "start");
                    }
                });

                camel(new BuilderSupport<CamelRouteActionBuilder>() {
                    @Override
                    public void configure(CamelRouteActionBuilder builder) {
                        builder.context(camelContext)
                                .controlBus()
                                .language(SimpleBuilder.simple("${camelContext.getRouteStatus('route_1')}"))
                                .result(ServiceStatus.Started);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 3);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), CamelControlBusAction.class);
        Assert.assertEquals(test.getLastExecutedAction().getClass(), DelegatingTestAction.class);

        CamelControlBusAction action = (CamelControlBusAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "controlbus");
        Assert.assertEquals(action.getRouteId(), "route_1");
        Assert.assertEquals(action.getAction(), "status");
        Assert.assertEquals(action.getResult(), "Stopped");

        action = (CamelControlBusAction) ((DelegatingTestAction)test.getActions().get(1)).getDelegate();
        Assert.assertEquals(action.getName(), "controlbus");
        Assert.assertEquals(action.getRouteId(), "route_1");
        Assert.assertEquals(action.getAction(), "start");
        Assert.assertNull(action.getResult());

        action = (CamelControlBusAction) ((DelegatingTestAction)test.getActions().get(2)).getDelegate();
        Assert.assertEquals(action.getName(), "controlbus");
        Assert.assertEquals(action.getLanguageType(), "simple");
        Assert.assertEquals(action.getLanguageExpression(), "${camelContext.getRouteStatus('route_1')}");
        Assert.assertEquals(action.getResult(), "Started");

        Assert.assertEquals(camelContext.getRouteDefinitions().size(), 2);
        Assert.assertEquals(camelContext.getRouteStatus("route_1"), ServiceStatus.Started);
        Assert.assertEquals(camelContext.getRouteStatus("route_2"), ServiceStatus.Stopped);
    }
}
