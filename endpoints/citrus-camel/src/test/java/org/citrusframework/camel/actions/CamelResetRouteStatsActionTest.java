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

import org.apache.camel.api.management.ManagedCamelContext;
import org.apache.camel.api.management.mbean.ManagedRouteMBean;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.impl.engine.AbstractCamelContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CamelResetRouteStatsActionTest extends org.citrusframework.junit.jupiter.AbstractUnitTest {

    private final AbstractCamelContext camelContext = Mockito.mock(AbstractCamelContext.class);
    private final ExtendedCamelContext camelContextExtension = Mockito.mock(ExtendedCamelContext.class);
    private final ManagedCamelContext managedCamelContext = Mockito.mock(ManagedCamelContext.class);
    private final ManagedRouteMBean routeMBean = Mockito.mock(ManagedRouteMBean.class);

    @Test
    public void testResetRouteStats() throws Exception {
        reset(camelContext, camelContextExtension, managedCamelContext, routeMBean);

        when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
        when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(managedCamelContext);
        when(managedCamelContext.getManagedRoute("route_1")).thenReturn(routeMBean);

        CamelResetRouteStatsAction action = new CamelResetRouteStatsAction.Builder()
                .context(camelContext)
                .route("route_1")
                .build();
        action.execute(context);

        verify(routeMBean).reset(true);
    }

    @Test
    public void testResetStatsRouteNotManaged() {

        CitrusRuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(CitrusRuntimeException.class, () -> {

            reset(camelContext, camelContextExtension, managedCamelContext);

            when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
            when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(managedCamelContext);
            when(managedCamelContext.getManagedRoute("unknown_route")).thenReturn(null);

            CamelResetRouteStatsAction action = new CamelResetRouteStatsAction.Builder()
                    .context(camelContext)
                    .route("unknown_route")
                    .build();
            action.execute(context);
    
        });

        org.junit.jupiter.api.Assertions.assertTrue(thrown.getMessage().matches(".*Failed to get managed route statistics.*"), "Unexpected exception message: " + thrown.getMessage());
    }

    @Test
    public void testResetStatsManagedContextMissing() {

        CitrusRuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(CitrusRuntimeException.class, () -> {

            reset(camelContext, camelContextExtension);

            when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
            when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(null);

            CamelResetRouteStatsAction action = new CamelResetRouteStatsAction.Builder()
                    .context(camelContext)
                    .route("route_1")
                    .build();
            action.execute(context);
    
        });

        org.junit.jupiter.api.Assertions.assertTrue(thrown.getMessage().matches(".*managed Camel context extension.*"), "Unexpected exception message: " + thrown.getMessage());
    }

    @Test
    public void testResetWithVariableSupport() throws Exception {
        reset(camelContext, camelContextExtension, managedCamelContext, routeMBean);

        context.setVariable("routeId", "route_1");

        when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
        when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(managedCamelContext);
        when(managedCamelContext.getManagedRoute("route_1")).thenReturn(routeMBean);

        CamelResetRouteStatsAction action = new CamelResetRouteStatsAction.Builder()
                .context(camelContext)
                .route("${routeId}")
                .build();
        action.execute(context);

        verify(routeMBean).reset(true);
    }
}
