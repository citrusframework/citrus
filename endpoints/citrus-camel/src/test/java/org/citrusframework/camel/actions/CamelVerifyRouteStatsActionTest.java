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
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class CamelVerifyRouteStatsActionTest extends AbstractTestNGUnitTest {

    private final AbstractCamelContext camelContext = Mockito.mock(AbstractCamelContext.class);
    private final ExtendedCamelContext camelContextExtension = Mockito.mock(ExtendedCamelContext.class);
    private final ManagedCamelContext managedCamelContext = Mockito.mock(ManagedCamelContext.class);
    private final ManagedRouteMBean routeMBean = Mockito.mock(ManagedRouteMBean.class);

    @Test
    public void testVerifyCompletedExchanges() {
        reset(camelContext, camelContextExtension, managedCamelContext, routeMBean);

        when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
        when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(managedCamelContext);
        when(managedCamelContext.getManagedRoute("route_1")).thenReturn(routeMBean);
        when(routeMBean.getExchangesCompleted()).thenReturn(5L);

        CamelVerifyRouteStatsAction action = new CamelVerifyRouteStatsAction.Builder()
                .context(camelContext)
                .route("route_1")
                .completed(5)
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = ValidationException.class,
            expectedExceptionsMessageRegExp = ".*completed exchanges.*did not match.*")
    public void testVerifyCompletedExchangesMismatch() {
        reset(camelContext, camelContextExtension, managedCamelContext, routeMBean);

        when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
        when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(managedCamelContext);
        when(managedCamelContext.getManagedRoute("route_1")).thenReturn(routeMBean);
        when(routeMBean.getExchangesCompleted()).thenReturn(3L);

        CamelVerifyRouteStatsAction action = new CamelVerifyRouteStatsAction.Builder()
                .context(camelContext)
                .route("route_1")
                .completed(5)
                .build();
        action.execute(context);
    }

    @Test
    public void testVerifyFailedExchanges() {
        reset(camelContext, camelContextExtension, managedCamelContext, routeMBean);

        when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
        when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(managedCamelContext);
        when(managedCamelContext.getManagedRoute("route_1")).thenReturn(routeMBean);
        when(routeMBean.getExchangesFailed()).thenReturn(0L);

        CamelVerifyRouteStatsAction action = new CamelVerifyRouteStatsAction.Builder()
                .context(camelContext)
                .route("route_1")
                .failed(0)
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = ValidationException.class,
            expectedExceptionsMessageRegExp = ".*failed exchanges.*did not match.*")
    public void testVerifyFailedExchangesMismatch() {
        reset(camelContext, camelContextExtension, managedCamelContext, routeMBean);

        when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
        when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(managedCamelContext);
        when(managedCamelContext.getManagedRoute("route_1")).thenReturn(routeMBean);
        when(routeMBean.getExchangesFailed()).thenReturn(2L);

        CamelVerifyRouteStatsAction action = new CamelVerifyRouteStatsAction.Builder()
                .context(camelContext)
                .route("route_1")
                .failed(0)
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Failed to get managed route statistics.*")
    public void testVerifyStatsRouteNotManaged() {
        reset(camelContext, camelContextExtension, managedCamelContext);

        when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
        when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(managedCamelContext);
        when(managedCamelContext.getManagedRoute("unknown_route")).thenReturn(null);

        CamelVerifyRouteStatsAction action = new CamelVerifyRouteStatsAction.Builder()
                .context(camelContext)
                .route("unknown_route")
                .completed(0)
                .build();
        action.execute(context);
    }

    @Test
    public void testVerifyWithVariableSupport() {
        reset(camelContext, camelContextExtension, managedCamelContext, routeMBean);

        context.setVariable("routeId", "route_1");

        when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
        when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(managedCamelContext);
        when(managedCamelContext.getManagedRoute("route_1")).thenReturn(routeMBean);
        when(routeMBean.getExchangesCompleted()).thenReturn(10L);

        CamelVerifyRouteStatsAction action = new CamelVerifyRouteStatsAction.Builder()
                .context(camelContext)
                .route("${routeId}")
                .completed(10)
                .build();
        action.execute(context);
    }

    @Test
    public void testVerifyCompletedAndFailed() {
        reset(camelContext, camelContextExtension, managedCamelContext, routeMBean);

        when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
        when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(managedCamelContext);
        when(managedCamelContext.getManagedRoute("route_1")).thenReturn(routeMBean);
        when(routeMBean.getExchangesCompleted()).thenReturn(5L);
        when(routeMBean.getExchangesFailed()).thenReturn(0L);

        CamelVerifyRouteStatsAction action = new CamelVerifyRouteStatsAction.Builder()
                .context(camelContext)
                .route("route_1")
                .completed(5)
                .failed(0)
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*managed Camel context extension.*")
    public void testVerifyStatsManagedContextMissing() {
        reset(camelContext, camelContextExtension);

        when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
        when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(null);

        CamelVerifyRouteStatsAction action = new CamelVerifyRouteStatsAction.Builder()
                .context(camelContext)
                .route("route_1")
                .completed(0)
                .build();
        action.execute(context);
    }

    @Test
    public void testVerifyStatsJson() {
        reset(camelContext, camelContextExtension, managedCamelContext, routeMBean);

        String actualJson = """
                {"exchangesCompleted": 5, "exchangesFailed": 0, "meanProcessingTime": 42}""";

        when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
        when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(managedCamelContext);
        when(managedCamelContext.getManagedRoute("route_1")).thenReturn(routeMBean);
        when(routeMBean.dumpStatsAsJSon(false)).thenReturn(actualJson);

        CamelVerifyRouteStatsAction action = new CamelVerifyRouteStatsAction.Builder()
                .context(camelContext)
                .route("route_1")
                .stats("""
                        {"exchangesCompleted": 5, "exchangesFailed": 0, "meanProcessingTime": 42}""")
                .build();
        action.execute(context);
    }

    @Test
    public void testVerifyStatsJsonPartialMatch() {
        reset(camelContext, camelContextExtension, managedCamelContext, routeMBean);

        String actualJson = """
                {"exchangesCompleted": 5, "exchangesFailed": 0, "meanProcessingTime": 42}""";

        when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
        when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(managedCamelContext);
        when(managedCamelContext.getManagedRoute("route_1")).thenReturn(routeMBean);
        when(routeMBean.dumpStatsAsJSon(false)).thenReturn(actualJson);

        CamelVerifyRouteStatsAction action = new CamelVerifyRouteStatsAction.Builder()
                .context(camelContext)
                .route("route_1")
                .stats("""
                        {"exchangesCompleted": 5}""")
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testVerifyStatsJsonMismatch() {
        reset(camelContext, camelContextExtension, managedCamelContext, routeMBean);

        String actualJson = """
                {"exchangesCompleted": 5, "exchangesFailed": 0}""";

        when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
        when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(managedCamelContext);
        when(managedCamelContext.getManagedRoute("route_1")).thenReturn(routeMBean);
        when(routeMBean.dumpStatsAsJSon(false)).thenReturn(actualJson);

        CamelVerifyRouteStatsAction action = new CamelVerifyRouteStatsAction.Builder()
                .context(camelContext)
                .route("route_1")
                .stats("""
                        {"exchangesCompleted": 10}""")
                .build();
        action.execute(context);
    }

    @Test
    public void testVerifyStatsJsonWithCompletedCheck() {
        reset(camelContext, camelContextExtension, managedCamelContext, routeMBean);

        String actualJson = """
                {"exchangesCompleted": 5, "exchangesFailed": 0}""";

        when(camelContext.getCamelContextExtension()).thenReturn(camelContextExtension);
        when(camelContextExtension.getContextPlugin(ManagedCamelContext.class)).thenReturn(managedCamelContext);
        when(managedCamelContext.getManagedRoute("route_1")).thenReturn(routeMBean);
        when(routeMBean.getExchangesCompleted()).thenReturn(5L);
        when(routeMBean.dumpStatsAsJSon(false)).thenReturn(actualJson);

        CamelVerifyRouteStatsAction action = new CamelVerifyRouteStatsAction.Builder()
                .context(camelContext)
                .route("route_1")
                .completed(5)
                .stats("""
                        {"exchangesFailed": 0}""")
                .build();
        action.execute(context);
    }
}
