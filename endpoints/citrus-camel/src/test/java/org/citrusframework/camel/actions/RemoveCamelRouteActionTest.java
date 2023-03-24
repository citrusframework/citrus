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

package org.citrusframework.camel.actions;

import java.util.Collections;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.apache.camel.CamelException;
import org.apache.camel.ServiceStatus;
import org.apache.camel.impl.engine.AbstractCamelContext;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


public class RemoveCamelRouteActionTest extends AbstractTestNGUnitTest {

    private AbstractCamelContext camelContext = Mockito.mock(AbstractCamelContext.class);

    @Test
    public void testRemoveRoute() throws Exception {
        reset(camelContext);

        when(camelContext.getName()).thenReturn("camel_context");

        when(camelContext.getRouteStatus("route_1")).thenReturn(ServiceStatus.Stopped);
        when(camelContext.removeRoute("route_1")).thenReturn(true);

        RemoveCamelRouteAction action = new RemoveCamelRouteAction.Builder()
                .context(camelContext)
                .routeIds(Collections.singletonList("route_1"))
                .build();
        action.execute(context);

    }

    @Test
    public void testRemoveRouteVariableSupport() throws Exception {
        reset(camelContext);

        context.setVariable("routeId", "route_1");

        when(camelContext.getName()).thenReturn("camel_context");

        when(camelContext.getRouteStatus("route_1")).thenReturn(ServiceStatus.Stopped);
        when(camelContext.removeRoute("route_1")).thenReturn(true);

        RemoveCamelRouteAction action = new RemoveCamelRouteAction.Builder()
                .context(camelContext)
                .routeIds(Collections.singletonList("${routeId}"))
                .build();
        action.execute(context);

    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = ".*must be stopped.*")
    public void testRemoveRouteNotStopped() throws Exception {
        reset(camelContext);

        when(camelContext.getName()).thenReturn("camel_context");

        when(camelContext.getRouteStatus("route_1")).thenReturn(ServiceStatus.Stopped);
        when(camelContext.removeRoute("route_1")).thenReturn(true);
        when(camelContext.getRouteStatus("route_2")).thenReturn(ServiceStatus.Started);

        RemoveCamelRouteAction action = new RemoveCamelRouteAction.Builder()
                .context(camelContext)
                .routes("route_1", "route_2", "route_3")
                .build();
        action.execute(context);

    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testRemoveRouteWithFalseResult() throws Exception {
        reset(camelContext);

        when(camelContext.getName()).thenReturn("camel_context");

        when(camelContext.getRouteStatus("route_1")).thenReturn(ServiceStatus.Stopped);
        when(camelContext.removeRoute("route_1")).thenReturn(true);
        when(camelContext.getRouteStatus("route_2")).thenReturn(ServiceStatus.Stopped);
        when(camelContext.removeRoute("route_2")).thenReturn(false);

        RemoveCamelRouteAction action = new RemoveCamelRouteAction.Builder()
                .context(camelContext)
                .routes("route_1", "route_2", "route_3")
                .build();
        action.execute(context);

    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testRemoveRouteWithException() throws Exception {
        reset(camelContext);

        when(camelContext.getName()).thenReturn("camel_context");

        when(camelContext.getRouteStatus("route_1")).thenReturn(ServiceStatus.Stopped);
        when(camelContext.removeRoute("route_1")).thenReturn(true);
        when(camelContext.getRouteStatus("route_2")).thenReturn(ServiceStatus.Stopped);
        doThrow(new CamelException("Failed to stop route")).when(camelContext).removeRoute("route_2");

        RemoveCamelRouteAction action = new RemoveCamelRouteAction.Builder()
                .context(camelContext)
                .routes("route_1", "route_2", "route_3")
                .build();
        action.execute(context);

    }
}
