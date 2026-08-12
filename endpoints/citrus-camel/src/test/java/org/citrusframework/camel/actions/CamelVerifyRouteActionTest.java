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

import org.apache.camel.Route;
import org.apache.camel.ServiceStatus;
import org.apache.camel.spi.RouteController;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.apache.camel.impl.engine.AbstractCamelContext;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class CamelVerifyRouteActionTest extends AbstractTestNGUnitTest {

    private final AbstractCamelContext camelContext = Mockito.mock(AbstractCamelContext.class);
    private final RouteController routeController = Mockito.mock(RouteController.class);
    private final Route route = Mockito.mock(Route.class);

    @Test
    public void testVerifyRouteExists() {
        reset(camelContext);

        when(camelContext.getName()).thenReturn("camel_context");
        when(camelContext.getRoute("route_1")).thenReturn(route);

        CamelVerifyRouteAction action = new CamelVerifyRouteAction.Builder()
                .context(camelContext)
                .route("route_1")
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*does not exist.*")
    public void testVerifyRouteNotExists() {
        reset(camelContext);

        when(camelContext.getName()).thenReturn("camel_context");
        when(camelContext.getRoute("unknown_route")).thenReturn(null);

        CamelVerifyRouteAction action = new CamelVerifyRouteAction.Builder()
                .context(camelContext)
                .route("unknown_route")
                .build();
        action.execute(context);
    }

    @Test
    public void testVerifyRouteStatusStarted() {
        reset(camelContext, routeController);

        when(camelContext.getName()).thenReturn("camel_context");
        when(camelContext.getRoute("route_1")).thenReturn(route);
        when(camelContext.getRouteController()).thenReturn(routeController);
        when(routeController.getRouteStatus("route_1")).thenReturn(ServiceStatus.Started);

        CamelVerifyRouteAction action = new CamelVerifyRouteAction.Builder()
                .context(camelContext)
                .route("route_1")
                .status(ServiceStatus.Started)
                .build();
        action.execute(context);
    }

    @Test
    public void testVerifyRouteStatusStopped() {
        reset(camelContext, routeController);

        when(camelContext.getName()).thenReturn("camel_context");
        when(camelContext.getRoute("route_1")).thenReturn(route);
        when(camelContext.getRouteController()).thenReturn(routeController);
        when(routeController.getRouteStatus("route_1")).thenReturn(ServiceStatus.Stopped);

        CamelVerifyRouteAction action = new CamelVerifyRouteAction.Builder()
                .context(camelContext)
                .route("route_1")
                .status(ServiceStatus.Stopped)
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testVerifyRouteStatusMismatch() {
        reset(camelContext, routeController);

        when(camelContext.getName()).thenReturn("camel_context");
        when(camelContext.getRoute("route_1")).thenReturn(route);
        when(camelContext.getRouteController()).thenReturn(routeController);
        when(routeController.getRouteStatus("route_1")).thenReturn(ServiceStatus.Stopped);

        CamelVerifyRouteAction action = new CamelVerifyRouteAction.Builder()
                .context(camelContext)
                .route("route_1")
                .status(ServiceStatus.Started)
                .build();
        action.execute(context);
    }

    @Test
    public void testVerifyRouteWithVariableSupport() {
        reset(camelContext, routeController);

        context.setVariable("routeId", "route_1");
        context.setVariable("expectedStatus", "Started");

        when(camelContext.getName()).thenReturn("camel_context");
        when(camelContext.getRoute("route_1")).thenReturn(route);
        when(camelContext.getRouteController()).thenReturn(routeController);
        when(routeController.getRouteStatus("route_1")).thenReturn(ServiceStatus.Started);

        CamelVerifyRouteAction action = new CamelVerifyRouteAction.Builder()
                .context(camelContext)
                .route("${routeId}")
                .status("${expectedStatus}")
                .build();
        action.execute(context);
    }

    @Test
    public void testVerifyRouteStatusWithStringStatus() {
        reset(camelContext, routeController);

        when(camelContext.getName()).thenReturn("camel_context");
        when(camelContext.getRoute("route_1")).thenReturn(route);
        when(camelContext.getRouteController()).thenReturn(routeController);
        when(routeController.getRouteStatus("route_1")).thenReturn(ServiceStatus.Started);

        CamelVerifyRouteAction action = new CamelVerifyRouteAction.Builder()
                .context(camelContext)
                .route("route_1")
                .status("Started")
                .build();
        action.execute(context);
    }
}
