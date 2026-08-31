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
import static org.mockito.Mockito.verify;

import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.citrusframework.playwright.actions.NavigateAction.Command;

class NavigateActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldDefaultToReloadCommand() {
        assertEquals(Command.RELOAD, new NavigateAction.Builder().build().getCommand());
    }

    @Test
    void shouldGoBack() {
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);

        NavigateAction action = new NavigateAction.Builder().back().build();
        assertEquals(Command.BACK, action.getCommand());

        action.execute(context);
        verify(browser.page()).goBack();
    }

    @Test
    void shouldGoForward() {
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);

        NavigateAction action = new NavigateAction.Builder().forward().build();

        action.execute(context);
        verify(browser.page()).goForward();
    }

    @Test
    void shouldReload() {
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);

        NavigateAction action = new NavigateAction.Builder().reload().build();

        action.execute(context);
        verify(browser.page()).reload();
    }
}
