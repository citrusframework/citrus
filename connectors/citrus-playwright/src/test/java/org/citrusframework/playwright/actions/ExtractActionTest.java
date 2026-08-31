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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.Cookie;

class ExtractActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;
    private Locator locator;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        locator = mock(Locator.class);
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        when(browser.page().locator(anyString())).thenReturn(locator);
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldExtractPageUrl() {
        when(browser.page().url()).thenReturn("https://example.com");

        new ExtractAction.Builder().url().variable("currentUrl").build().execute(context);

        assertEquals("https://example.com", context.getVariable("currentUrl"));
    }

    @Test
    void shouldExtractPageTitle() {
        when(browser.page().title()).thenReturn("ACME");

        new ExtractAction.Builder().title().variable("title").build().execute(context);

        assertEquals("ACME", context.getVariable("title"));
    }

    @Test
    void shouldExtractLocatorText() {
        when(locator.textContent()).thenReturn("Hello");

        new ExtractAction.Builder().locator(".el").text().variable("text").build().execute(context);

        assertEquals("Hello", context.getVariable("text"));
    }

    @Test
    void shouldExtractLocatorValue() {
        when(locator.inputValue()).thenReturn("42");

        new ExtractAction.Builder().locator(".el").value().variable("input").build().execute(context);

        assertEquals("42", context.getVariable("input"));
    }

    @Test
    void shouldExtractLocatorAttribute() {
        when(locator.getAttribute("data-id")).thenReturn("7");

        new ExtractAction.Builder().locator(".el").attribute("data-id").variable("id").build().execute(context);

        assertEquals("7", context.getVariable("id"));
    }

    @Test
    void shouldExtractLocatorCount() {
        when(locator.count()).thenReturn(3);

        new ExtractAction.Builder().locator(".el").count().variable("count").build().execute(context);

        assertEquals("3", String.valueOf(context.getVariable("count")));
    }

    @Test
    void shouldExtractLocalStorageValue() {
        com.microsoft.playwright.WebStorage storage = mock(com.microsoft.playwright.WebStorage.class);
        when(browser.page().localStorage()).thenReturn(storage);
        when(storage.getItem("token")).thenReturn("abc");

        new ExtractAction.Builder().storageLocal("token").variable("stored").build().execute(context);

        assertEquals("abc", context.getVariable("stored"));
    }

    @Test
    void shouldExtractCookieValue() {
        browser.createContext("admin");
        Cookie cookie = new Cookie("session", "abc");
        cookie.domain = "example.com";
        cookie.path = "/";
        when(browser.context().cookies()).thenReturn(List.of(cookie));

        new ExtractAction.Builder().cookie("session").variable("cookie").build().execute(context);

        assertEquals("abc", context.getVariable("cookie"));
    }

    @Test
    void shouldStoreEmptyStringWhenResultNull() {
        when(locator.textContent()).thenReturn(null);

        new ExtractAction.Builder().locator(".el").text().variable("text").build().execute(context);

        assertEquals("", context.getVariable("text"));
    }

    @Test
    void shouldFailFastWhenVariableMissing() {
        expectThrows(CitrusRuntimeException.class,
                () -> new ExtractAction.Builder().locator(".el").text().build());
    }

    @Test
    void shouldFailFastWhenLocatorMissing() {
        expectThrows(CitrusRuntimeException.class,
                () -> new ExtractAction.Builder().text().variable("text").build());
    }
}
