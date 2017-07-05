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

package com.consol.citrus.camel.actions;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.apache.camel.*;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;


public class RemoveCamelRouteActionTest extends AbstractTestNGUnitTest {

    private CamelContext camelContext = Mockito.mock(CamelContext.class);

    @Test
    public void testRemoveRoute() throws Exception {
        reset(camelContext);

        when(camelContext.getName()).thenReturn("camel_context");

        when(camelContext.getRouteStatus("route_1")).thenReturn(ServiceStatus.Stopped);
        when(camelContext.removeRoute("route_1")).thenReturn(true);

        RemoveCamelRouteAction action = new RemoveCamelRouteAction();
        action.setCamelContext(camelContext);
        action.setRouteIds(Collections.singletonList("route_1"));

        action.execute(context);

    }
    
    @Test
    public void testRemoveRouteVariableSupport() throws Exception {
        reset(camelContext);

        context.setVariable("routeId", "route_1");

        when(camelContext.getName()).thenReturn("camel_context");

        when(camelContext.getRouteStatus("route_1")).thenReturn(ServiceStatus.Stopped);
        when(camelContext.removeRoute("route_1")).thenReturn(true);

        RemoveCamelRouteAction action = new RemoveCamelRouteAction();
        action.setCamelContext(camelContext);
        action.setRouteIds(Collections.singletonList("${routeId}"));

        action.execute(context);

    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = ".*must be stopped.*")
    public void testRemoveRouteNotStopped() throws Exception {
        reset(camelContext);

        when(camelContext.getName()).thenReturn("camel_context");

        when(camelContext.getRouteStatus("route_1")).thenReturn(ServiceStatus.Stopped);
        when(camelContext.removeRoute("route_1")).thenReturn(true);
        when(camelContext.getRouteStatus("route_2")).thenReturn(ServiceStatus.Started);

        RemoveCamelRouteAction action = new RemoveCamelRouteAction();
        action.setCamelContext(camelContext);
        action.setRouteIds(Arrays.asList("route_1", "route_2", "route_3"));

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


        RemoveCamelRouteAction action = new RemoveCamelRouteAction();
        action.setCamelContext(camelContext);
        action.setRouteIds(Arrays.asList("route_1", "route_2", "route_3"));

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


        RemoveCamelRouteAction action = new RemoveCamelRouteAction();
        action.setCamelContext(camelContext);
        action.setRouteIds(Arrays.asList("route_1", "route_2", "route_3"));

        action.execute(context);

    }
}