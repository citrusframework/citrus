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
import org.citrusframework.playwright.actions.WaitForAction.Condition;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;

class WaitForActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        when(browser.page().locator("#ready")).thenReturn(mock(Locator.class));
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldDefaultToVisible() {
        assertEquals(Condition.VISIBLE, new WaitForAction.Builder().locator("#ready").build().getCondition());
    }

    @Test
    void shouldWaitForLocatorSelectorState() {
        Locator element = browser.page().locator("#ready");

        new WaitForAction.Builder().locator("#ready").visible().build().execute(context);

        verify(element).waitFor(any(Locator.WaitForOptions.class));
    }

    @Test
    void shouldWaitForLoadState() {
        new WaitForAction.Builder().load().build().execute(context);

        verify(browser.page()).waitForLoadState(LoadState.LOAD);
    }

    @Test
    void shouldWaitForDomContentLoadedState() {
        new WaitForAction.Builder().domContentLoaded().build().execute(context);

        verify(browser.page()).waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    @Test
    void shouldWaitForNetworkIdleState() {
        new WaitForAction.Builder().networkIdle().build().execute(context);

        verify(browser.page()).waitForLoadState(LoadState.NETWORKIDLE);
    }

    @Test
    void shouldFailFastWhenSelectorConditionMissingLocator() {
        expectThrows(CitrusRuntimeException.class, () -> new WaitForAction.Builder().visible().build());
    }
}
