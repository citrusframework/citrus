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
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.easymock.EasyMock.*;

public class CreateCamelRouteActionTest extends AbstractTestNGUnitTest {

    private CamelContext camelContext = EasyMock.createMock(CamelContext.class);
    private RouteDefinition route = EasyMock.createMock(RouteDefinition.class);

    @Test
     public void testCreateRoute() throws Exception {
        reset(camelContext, route);

        expect(camelContext.getName()).andReturn("camel_context").atLeastOnce();
        expect(route.getId()).andReturn("route_1").atLeastOnce();

        camelContext.addRouteDefinition(route);
        expectLastCall().once();

        replay(camelContext, route);

        CreateCamelRouteAction action = new CreateCamelRouteAction();
        action.setCamelContext(camelContext);
        action.setRoutes(Collections.singletonList(route));

        action.execute(context);

        verify(camelContext, route);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testCreateRouteWithException() throws Exception {
        reset(camelContext, route);

        expect(camelContext.getName()).andReturn("camel_context").atLeastOnce();
        expect(route.getId()).andReturn("route_1").atLeastOnce();

        camelContext.addRouteDefinition(route);
        expectLastCall().andThrow(new FailedToStartRouteException("routeId", "Failed to start route")).once();

        replay(camelContext, route);

        CreateCamelRouteAction action = new CreateCamelRouteAction();
        action.setCamelContext(camelContext);
        action.setRoutes(Collections.singletonList(route));

        action.execute(context);

        verify(camelContext, route);
    }
}