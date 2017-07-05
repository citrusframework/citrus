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
import org.apache.camel.CamelException;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class StopCamelRouteActionTest extends AbstractTestNGUnitTest {

    private CamelContext camelContext = Mockito.mock(CamelContext.class);

    @Test
    public void testStopRoute() throws Exception {
        reset(camelContext);

        when(camelContext.getName()).thenReturn("camel_context");

        StopCamelRouteAction action = new StopCamelRouteAction();
        action.setCamelContext(camelContext);
        action.setRouteIds(Collections.singletonList("route_1"));

        action.execute(context);

        verify(camelContext).stopRoute("route_1");
    }
    
    @Test
    public void testStopRouteVariableSupport() throws Exception {
        reset(camelContext);

        context.setVariable("routeId", "route_1");

        when(camelContext.getName()).thenReturn("camel_context");

        StopCamelRouteAction action = new StopCamelRouteAction();
        action.setCamelContext(camelContext);
        action.setRouteIds(Collections.singletonList("${routeId}"));

        action.execute(context);

        verify(camelContext).stopRoute("route_1");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testStopRouteWithException() throws Exception {
        reset(camelContext);

        when(camelContext.getName()).thenReturn("camel_context");

        doThrow(new CamelException("Failed to stop route")).when(camelContext).stopRoute("route_2");

        StopCamelRouteAction action = new StopCamelRouteAction();
        action.setCamelContext(camelContext);
        action.setRouteIds(Arrays.asList("route_1", "route_2", "route_3"));

        action.execute(context);

        verify(camelContext).stopRoute("route_1");
    }
}