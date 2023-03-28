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
import org.apache.camel.FailedToStartRouteException;
import org.apache.camel.impl.engine.AbstractCamelContext;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class StartCamelRouteActionTest extends AbstractTestNGUnitTest {

    private AbstractCamelContext camelContext = Mockito.mock(AbstractCamelContext.class);

    @Test
    public void testStartRoute() throws Exception {
        reset(camelContext);

        when(camelContext.getName()).thenReturn("camel_context");

        StartCamelRouteAction action = new StartCamelRouteAction.Builder()
                .context(camelContext)
                .routeIds(Collections.singletonList("route_1"))
                .build();
        action.execute(context);

        verify(camelContext).startRoute("route_1");
    }

    @Test
    public void testStartRouteVariableSupport() throws Exception {
        reset(camelContext);

        context.setVariable("routeId", "route_1");

        when(camelContext.getName()).thenReturn("camel_context");

        StartCamelRouteAction action = new StartCamelRouteAction.Builder()
                .context(camelContext)
                .routeIds(Collections.singletonList("${routeId}"))
                .build();
        action.execute(context);

        verify(camelContext).startRoute("route_1");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testStartRouteWithException() throws Exception {
        reset(camelContext);

        when(camelContext.getName()).thenReturn("camel_context");

        doThrow(new FailedToStartRouteException("route_2", "Failed to start route")).when(camelContext).startRoute("route_2");

        StartCamelRouteAction action = new StartCamelRouteAction.Builder()
                .context(camelContext)
                .routes("route_1", "route_2", "route_3")
                .build();
        action.execute(context);

        verify(camelContext).startRoute("route_1");
    }
}
