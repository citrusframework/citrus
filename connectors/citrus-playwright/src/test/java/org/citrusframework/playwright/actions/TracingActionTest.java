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

package org.citrusframework.playwright.actions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.Tracing;

class TracingActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;
    private Tracing tracing;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        tracing = mock(Tracing.class);
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        browser.createContext("admin");
        when(browser.context().tracing()).thenReturn(tracing);
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldStartTracing() {
        new TracingAction.Builder().start().screenshots(true).snapshots(true).sources(true).build().execute(context);

        verify(tracing).start(any(Tracing.StartOptions.class));
    }

    @Test
    void shouldStartTracingWithoutFlagsWhenUnset() {
        new TracingAction.Builder().start().build().execute(context);

        verify(tracing).start(any(Tracing.StartOptions.class));
    }

    @Test
    void shouldStopTracingToConfiguredPathAndStoreVariable() {
        new TracingAction.Builder().stop().path("target/traces/run.zip").variable("tracePath").build().execute(context);

        verify(tracing).stop(any(Tracing.StopOptions.class));
        assertEquals(java.nio.file.Path.of("target/traces/run.zip").toString(), context.getVariable("tracePath"));
    }

    @Test
    void shouldFailFastWhenCommandMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new TracingAction.Builder().build());
    }
}
