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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

class JavaScriptActionTest {

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
    void shouldStoreConfiguredScript() {
        assertEquals("() => document.title", new JavaScriptAction.Builder().script("() => document.title").build().getScript());
    }

    @Test
    void shouldEvaluateScriptOnCurrentPage() {
        new JavaScriptAction.Builder().script("() => document.title").build().execute(context);

        verify(browser.page()).evaluate("() => document.title");
    }

    @Test
    void shouldStoreNonNullResultInVariable() {
        when(browser.page().evaluate(anyString())).thenReturn("Citrus Test Hub");

        new JavaScriptAction.Builder().script("() => 1").variable("title").build().execute(context);

        assertEquals("Citrus Test Hub", context.getVariable("title"));
    }

    @Test
    void shouldStoreEmptyStringWhenResultIsNull() {
        when(browser.page().evaluate(anyString())).thenReturn(null);

        new JavaScriptAction.Builder().script("() => null").variable("title").build().execute(context);

        assertEquals("", context.getVariable("title"));
    }

    @Test
    void shouldFailFastWhenScriptMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new JavaScriptAction.Builder().build());
    }
}
