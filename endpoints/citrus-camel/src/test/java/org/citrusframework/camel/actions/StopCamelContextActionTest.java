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

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.Registry;
import org.apache.camel.spi.ShutdownStrategy;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StopCamelContextActionTest extends AbstractTestNGUnitTest {

    private final CamelContext camelContext = Mockito.mock(CamelContext.class);
    private final ShutdownStrategy shutdownStrategy = Mockito.mock(ShutdownStrategy.class);
    private final Registry registry = Mockito.mock(Registry.class);

    @BeforeMethod
    public void setup() {
        Mockito.reset(camelContext, shutdownStrategy, registry);
        when(camelContext.getRegistry()).thenReturn(registry);
        when(registry.findByType(any())).thenReturn(Collections.emptySet());
        when(registry.findByTypeWithName(any())).thenReturn(Collections.emptyMap());
    }

    @Test
    public void testStopContext() throws Exception {
        context.getReferenceResolver().bind(CamelSettings.getContextName(), camelContext);

        StopCamelContextAction action = new StopCamelContextAction.Builder()
                .context(camelContext)
                .build();
        action.execute(context);

        verify(camelContext).stop();
        verify(camelContext, never()).getShutdownStrategy();
        Assert.assertFalse(action.isImmediate());
    }

    @Test
    public void testStopContextWithTimeout() throws Exception {
        when(camelContext.getShutdownStrategy()).thenReturn(shutdownStrategy);
        context.getReferenceResolver().bind("myCamelContext", camelContext);

        StopCamelContextAction action = new StopCamelContextAction.Builder()
                .context(camelContext)
                .contextName("myCamelContext")
                .timeout(60)
                .build();
        action.execute(context);

        verify(shutdownStrategy).setTimeout(60);
        verify(shutdownStrategy).setTimeUnit(TimeUnit.SECONDS);
        verify(camelContext).stop();

        Assert.assertEquals(action.getTimeout(), 60L);
        Assert.assertEquals(action.getContextName(), "myCamelContext");
        Assert.assertFalse(action.isImmediate());
    }

    @Test
    public void testStopContextImmediate() throws Exception {
        when(camelContext.getShutdownStrategy()).thenReturn(shutdownStrategy);
        context.getReferenceResolver().bind("myCamelContext", camelContext);

        StopCamelContextAction action = new StopCamelContextAction.Builder()
                .context(camelContext)
                .contextName("myCamelContext")
                .immediate()
                .build();
        action.execute(context);

        verify(shutdownStrategy).setShutdownNowOnTimeout(true);
        verify(shutdownStrategy).setTimeout(1);
        verify(shutdownStrategy).setTimeUnit(TimeUnit.SECONDS);
        verify(camelContext).stop();

        Assert.assertTrue(action.isImmediate());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testStopContextWithException() throws Exception {
        context.getReferenceResolver().bind(CamelSettings.getContextName(), camelContext);
        Mockito.doThrow(new RuntimeException("Failed to stop")).when(camelContext).stop();

        StopCamelContextAction action = new StopCamelContextAction.Builder()
                .context(camelContext)
                .build();
        action.execute(context);
    }
}
