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
import org.apache.camel.CamelContext;
import org.apache.camel.FailedToStartRouteException;
import org.apache.camel.model.RouteDefinition;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.*;


public class CreateCamelRouteActionTest extends AbstractTestNGUnitTest {

    private CamelContext camelContext = Mockito.mock(CamelContext.class);
    private RouteDefinition route = Mockito.mock(RouteDefinition.class);

    @Test
     public void testCreateRoute() throws Exception {
        reset(camelContext, route);

        when(camelContext.getName()).thenReturn("camel_context");
        when(route.getId()).thenReturn("route_1");

        CreateCamelRouteAction action = new CreateCamelRouteAction();
        action.setCamelContext(camelContext);
        action.setRoutes(Collections.singletonList(route));

        action.execute(context);

        verify(camelContext).addRouteDefinition(route);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testCreateRouteWithException() throws Exception {
        reset(camelContext, route);

        when(camelContext.getName()).thenReturn("camel_context");
        when(route.getId()).thenReturn("route_1");

        doThrow(new FailedToStartRouteException("routeId", "Failed to start route")).when(camelContext).addRouteDefinition(route);

        CreateCamelRouteAction action = new CreateCamelRouteAction();
        action.setCamelContext(camelContext);
        action.setRoutes(Collections.singletonList(route));

        action.execute(context);

    }
}