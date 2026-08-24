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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.expectThrows;
import static org.testng.Assert.assertTrue;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowserConfiguration;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.citrusframework.playwright.support.StubStartedBrowser;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

class StartStopBrowserActionTest {

    private final TestContext context = new TestContext();

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldFailFastOnDoubleStartWhenNotAllowed() {
        StubStartedBrowser browser = new StubStartedBrowser();
        browser.start();

        StartBrowserAction action = new StartBrowserAction.Builder()
                .browser(browser)
                .allowAlreadyStarted(false)
                .build();

        assertFalse(action.isAllowAlreadyStarted());
        CitrusRuntimeException exception =
                expectThrows(CitrusRuntimeException.class, () -> action.execute(context));
        assertTrue(exception.getMessage().contains("allowAlreadyStarted=false"));
    }

    @Test
    void shouldRebindOnRepeatedStartOfTheSameBrowser() {
        StubStartedBrowser browser = new StubStartedBrowser();
        StartBrowserAction.Builder builder = new StartBrowserAction.Builder().browser(browser);

        // allowAlreadyStarted defaults to true so the start idiom stays idempotent
        // within a test; the second start must leave the same browser ambient.
        assertTrue(builder.build().isAllowAlreadyStarted());
        builder.build().execute(context);
        builder.build().execute(context);

        assertSame(browser, PlaywrightBrowserScope.current(context).orElseThrow());
    }

    @Test
    void shouldConfigureLazilyCreatedDefaultEndpointFromBuilder() {
        StartBrowserAction action = new StartBrowserAction.Builder()
                .browserType("firefox")
                .headless(false)
                .baseUrl("http://localhost:8080")
                .startPageUrl("http://localhost:8080/login")
                .defaultTimeout(5_000)
                .defaultNavigationTimeout(7_500)
                .consoleMessageLimit(25)
                .networkRecordLimit(50)
                .build();

        PlaywrightBrowserConfiguration configuration =
                action.getBrowser().getEndpointConfiguration();

        assertEquals("firefox", configuration.getBrowserType());
        assertEquals(Boolean.FALSE, configuration.getHeadless());
        assertEquals("http://localhost:8080", configuration.getBaseUrl());
        assertEquals("http://localhost:8080/login", configuration.getStartPageUrl());
        assertEquals(5_000L, configuration.getDefaultTimeout());
        assertEquals(7_500L, configuration.getDefaultNavigationTimeout());
        assertEquals(25, configuration.getConsoleMessageLimit());
        assertEquals(50, configuration.getNetworkRecordLimit());
    }

    @Test
    void shouldLeaveAnotherBrowsersAmbientBindingIntactOnStop() {
        StubStartedBrowser ambient = new StubStartedBrowser();
        ambient.start();
        PlaywrightBrowserScope.bind(ambient, context);

        StubStartedBrowser other = new StubStartedBrowser();
        other.start();
        new StopBrowserAction.Builder().browser(other).build().execute(context);

        assertEquals(1, other.stopCount());
        assertSame(ambient, PlaywrightBrowserScope.current(context).orElseThrow(),
                "stopping an unrelated browser must not unbind the thread's ambient one");
    }

    @Test
    void shouldRebindWhenStartingADifferentAlreadyStartedBrowser() {
        StubStartedBrowser adminBrowser = new StubStartedBrowser();
        adminBrowser.start();
        PlaywrightBrowserScope.bind(adminBrowser, context);

        StubStartedBrowser userBrowser = new StubStartedBrowser();
        userBrowser.start();

        new StartBrowserAction.Builder().browser(userBrowser).build().execute(context);

        // start() always binds, including when the browser was already running and
        // the start call itself was skipped. Documented on StartBrowserAction:
        // starting a browser is the explicit signal for which one is ambient.
        assertSame(userBrowser, PlaywrightBrowserScope.current(context).orElseThrow());
    }
}
