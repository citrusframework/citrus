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

import static org.testng.Assert.expectThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.MouseAction.Command;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.Locator;

class MouseActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        when(browser.page().locator("#save")).thenReturn(locator());
    }

    private Locator locator() {
        return mock(Locator.class);
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldClickElement() {
        Locator element = browser.page().locator("#save");

        new MouseAction.Builder(Command.CLICK).locator("#save").build().execute(context);

        verify(element).click();
    }

    @Test
    void shouldDoubleClickElement() {
        Locator element = browser.page().locator("#save");

        new MouseAction.Builder(Command.DOUBLE_CLICK).locator("#save").build().execute(context);

        verify(element).dblclick();
    }

    @Test
    void shouldRightClickElement() {
        Locator element = browser.page().locator("#save");

        new MouseAction.Builder(Command.RIGHT_CLICK).locator("#save").build().execute(context);

        verify(element).click(any(Locator.ClickOptions.class));
    }

    @Test
    void shouldHoverElement() {
        Locator element = browser.page().locator("#save");

        new MouseAction.Builder(Command.HOVER).locator("#save").build().execute(context);

        verify(element).hover();
    }

    @Test
    void shouldFocusElement() {
        Locator element = browser.page().locator("#save");

        new MouseAction.Builder(Command.FOCUS).locator("#save").build().execute(context);

        verify(element).focus();
    }

    @Test
    void shouldTapElement() {
        Locator element = browser.page().locator("#save");

        new MouseAction.Builder(Command.TAP).locator("#save").build().execute(context);

        verify(element).tap();
    }

    @Test
    void shouldFailFastWhenLocatorMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new MouseAction.Builder(Command.CLICK).build());
    }
}
