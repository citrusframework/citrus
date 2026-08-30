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
import static org.mockito.Mockito.verify;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.Page;

class OpenActionTest {

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
    void shouldStoreConfiguredUrl() {
        OpenAction action = new OpenAction.Builder().url("http://localhost:8080/index.html").build();

        assertEquals("http://localhost:8080/index.html", action.getUrl());
    }

    @Test
    void shouldNavigateCurrentPageToTheUrl() {
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        Page page = browser.page();

        new OpenAction.Builder().url("http://localhost:8080/index.html").build().execute(context);

        verify(page).navigate("http://localhost:8080/index.html");
    }

    @Test
    void shouldResolveUrlThroughContextVariables() {
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        context.setVariable("baseUrl", "/index.html");

        new OpenAction.Builder().url("${baseUrl}").build().execute(context);

        verify(browser.page()).navigate("/index.html");
    }

    @Test
    void shouldFailFastWhenUrlIsMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new OpenAction.Builder().build());
    }
}
