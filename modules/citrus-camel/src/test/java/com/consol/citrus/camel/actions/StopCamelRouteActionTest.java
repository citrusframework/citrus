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
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.easymock.EasyMock.*;

public class StopCamelRouteActionTest extends AbstractTestNGUnitTest {

    private CamelContext camelContext = EasyMock.createMock(CamelContext.class);

    @Test
     public void testStopRoute() throws Exception {
        reset(camelContext);

        expect(camelContext.getName()).andReturn("camel_context").atLeastOnce();

        camelContext.stopRoute("route_1");
        expectLastCall().once();

        replay(camelContext);

        StopCamelRouteAction action = new StopCamelRouteAction();
        action.setCamelContext(camelContext);
        action.setRouteIds(Collections.singletonList("route_1"));

        action.execute(context);

        verify(camelContext);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testStopRouteWithException() throws Exception {
        reset(camelContext);

        expect(camelContext.getName()).andReturn("camel_context").atLeastOnce();

        camelContext.stopRoute("route_1");
        expectLastCall().once();

        camelContext.stopRoute("route_2");
        expectLastCall().andThrow(new CamelException("Failed to stop route")).once();

        replay(camelContext);

        StopCamelRouteAction action = new StopCamelRouteAction();
        action.setCamelContext(camelContext);
        action.setRouteIds(Arrays.asList("route_1", "route_2", "route_3"));

        action.execute(context);

        verify(camelContext);
    }
}