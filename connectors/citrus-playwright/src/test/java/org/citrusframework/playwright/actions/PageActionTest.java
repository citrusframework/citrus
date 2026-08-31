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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.expectThrows;
import static org.testng.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.Page;

class PageActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldCreatePageByAlias() {
        new PageAction.Builder().newPage("dashboard").build().execute(context);

        assertTrue(browser.getPageAliases().contains("dashboard"));
    }

    @Test
    void shouldCreatePageInNamedContext() {
        browser.createContext("tenant");

        new PageAction.Builder().newPage("dashboard").inContext("tenant").build().execute(context);

        assertTrue(browser.getPageAliases().contains("dashboard"));
    }

    @Test
    void shouldSwitchPageByAlias() {
        browser.registerNewPage("first");
        browser.registerNewPage("second");

        new PageAction.Builder().switchTo("first").build().execute(context);

        assertTrue(browser.getCurrentPageAlias().filter("first"::equals).isPresent());
    }

    @Test
    void shouldSwitchPageByIndex() {
        browser.registerNewPage("first");
        browser.registerNewPage("second");

        new PageAction.Builder().switchToIndex(1).build().execute(context);

        assertTrue(browser.getCurrentPageAlias().filter("first"::equals).isPresent());
    }

    @Test
    void shouldSwitchPageByTitle() {
        browser.registerNewPage("first");
        Page second = browser.registerNewPage("second");
        when(second.title()).thenReturn("Orders");

        new PageAction.Builder().switchToTitle("Orders").build().execute(context);

        assertTrue(browser.getCurrentPageAlias().filter("second"::equals).isPresent());
    }

    @Test
    void shouldSwitchPageByUrlContaining() {
        browser.registerNewPage("first");
        Page second = browser.registerNewPage("second");
        when(second.url()).thenReturn("http://localhost/orders");

        new PageAction.Builder().switchToUrlContaining("orders").build().execute(context);

        assertTrue(browser.getCurrentPageAlias().filter("second"::equals).isPresent());
    }

    @Test
    void shouldClosePageByAlias() {
        browser.registerNewPage("embeddable");
        when(browser.findPage("embeddable").orElseThrow().isClosed()).thenReturn(false);

        new PageAction.Builder().close("embeddable").build().execute(context);

        assertFalse(browser.getPageAliases().contains("embeddable"));
    }

    @Test
    void shouldFailFastWhenCommandOrSelectorMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new PageAction.Builder().build());
    }

    @Test
    void shouldFailFastWhenIndexCommandHasNoIndex() {
        PageAction.Builder builder = new PageAction.Builder();
        builder.switchToIndex(2);
        PageAction action = builder.build();

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class, () -> action.execute(context));
        assertTrue(exception.getMessage().contains("index: 2"));
    }
}
