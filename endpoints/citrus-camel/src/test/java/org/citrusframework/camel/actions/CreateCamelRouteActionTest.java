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

package org.citrusframework.camel.actions;

import java.util.Collections;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.apache.camel.FailedToStartRouteException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.engine.AbstractCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateCamelRouteActionTest extends AbstractTestNGUnitTest {

    private final AbstractCamelContext camelContext = Mockito.mock(AbstractCamelContext.class);
    private final RouteDefinition route = Mockito.mock(RouteDefinition.class);

    @Test
    public void testCreateRouteContext() throws Exception {
        reset(camelContext);

        when(camelContext.getName()).thenReturn("camel_context");

        doAnswer(invocation -> {
            RouteBuilder routesBuilder = invocation.getArgument(0);
            routesBuilder.configure();
            Assert.assertEquals(routesBuilder.getRouteCollection().getRoutes().size(), 2L);
            Assert.assertEquals(routesBuilder.getRouteCollection().getRoutes().get(0).getId(), "route_1");
            Assert.assertEquals(routesBuilder.getRouteCollection().getRoutes().get(1).getId(), "route_2");
            return null;
        }).when(camelContext).addRoutes(any(RouteBuilder.class));

        CreateCamelRouteAction action = new CreateCamelRouteAction.Builder()
                .context(camelContext)
                .route("""
                        <routeContext xmlns="http://camel.apache.org/schema/spring">
                            <route id="route_1">
                                <from uri="direct:test1"/>
                                <to uri="mock:test1"/>
                            </route>
                            <route id="route_2">
                                <from uri="direct:test2"/>
                                <to uri="mock:test2"/>
                            </route>
                        </routeContext>""")
                .build();
        action.execute(context);

        verify(camelContext).addRoutes(any(RouteBuilder.class));
    }

    @Test
    public void testCreateRouteContextVariableSupport() throws Exception {
        reset(camelContext);

        context.setVariable("routeId", "route_1");
        context.setVariable("endpointUri", "test1");

        when(camelContext.getName()).thenReturn("camel_context");

        doAnswer(invocation -> {
            RouteBuilder routesBuilder = invocation.getArgument(0);
            routesBuilder.configure();
            Assert.assertEquals(routesBuilder.getRouteCollection().getRoutes().size(), 1L);
            Assert.assertEquals(routesBuilder.getRouteCollection().getRoutes().get(0).getId(), "route_1");
            return null;
        }).when(camelContext).addRoutes(any(RouteBuilder.class));

        CreateCamelRouteAction action = new CreateCamelRouteAction.Builder()
                .context(camelContext)
                .route("""
                        <routeContext xmlns="http://camel.apache.org/schema/spring">
                            <route id="${routeId}">
                                <from uri="direct:${endpointUri}"/>
                                <to uri="mock:${endpointUri}"/>
                            </route>
                        </routeContext>""")
                .build();

        action.execute(context);

        verify(camelContext).addRoutes(any(RouteBuilder.class));
    }

    @Test
    public void testCreateRoute() throws Exception {
        reset(camelContext, route);

        when(camelContext.getName()).thenReturn("camel_context");
        when(route.getId()).thenReturn("route_1");

        doAnswer(invocation -> {
            RouteBuilder routesBuilder = invocation.getArgument(0);
            routesBuilder.configure();
            Assert.assertEquals(routesBuilder.getRouteCollection().getRoutes().size(), 1L);
            Assert.assertEquals(routesBuilder.getRouteCollection().getRoutes().get(0), route);
            return null;
        }).when(camelContext).addRoutes(any(RouteBuilder.class));

        CreateCamelRouteAction action = new CreateCamelRouteAction.Builder()
                .context(camelContext)
                .routes(Collections.singletonList(route))
                .build();
        action.execute(context);

        verify(camelContext).addRoutes(any(RouteBuilder.class));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testCreateRouteWithException() throws Exception {
        reset(camelContext, route);

        when(camelContext.getName()).thenReturn("camel_context");
        when(route.getId()).thenReturn("route_1");

        doThrow(new FailedToStartRouteException("routeId", "Failed to start route")).when(camelContext).addRoutes(any(RouteBuilder.class));

        CreateCamelRouteAction action = new CreateCamelRouteAction.Builder()
                .context(camelContext)
                .routes(Collections.singletonList(route))
                .build();
        action.execute(context);

    }
}
