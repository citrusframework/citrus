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
import static org.testng.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.Cookie;

class VerifyActionTest {

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
    void shouldVerifyVisibleLocator() {
        when(locator.isVisible()).thenReturn(true);

        new VerifyAction.Builder().locator(".el").visible().build().execute(context);
    }

    @Test
    void shouldFailValidationWhenLocatorNotVisible() {
        when(locator.isVisible()).thenReturn(false);

        VerifyAction action = new VerifyAction.Builder().locator(".el").visible().build();

        ValidationException exception = expectThrows(ValidationException.class, () -> action.execute(context));
        assertTrue(exception.getMessage().contains("visible"));
    }

    @Test
    void shouldVerifyPresentWhenCountPositive() {
        when(locator.count()).thenReturn(2);

        new VerifyAction.Builder().locator(".el").present().build().execute(context);
    }

    @Test
    void shouldVerifyAbsentWhenCountZero() {
        when(locator.count()).thenReturn(0);

        new VerifyAction.Builder().locator(".el").absent().build().execute(context);
    }

    @Test
    void shouldVerifyLocatorText() {
        when(locator.textContent()).thenReturn("Hello");

        new VerifyAction.Builder().locator(".el").text("Hello").build().execute(context);
    }

    @Test
    void shouldResolveExpectedTextThroughContext() {
        context.setVariable("greeting", "Hello");
        when(locator.textContent()).thenReturn("Hello");

        new VerifyAction.Builder().locator(".el").text("${greeting}").build().execute(context);
    }

    @Test
    void shouldVerifyLocatorCount() {
        when(locator.count()).thenReturn(3);

        new VerifyAction.Builder().locator(".el").count(3).build().execute(context);
    }

    @Test
    void shouldVerifyLocatorAttribute() {
        when(locator.getAttribute("data-id")).thenReturn("42");

        new VerifyAction.Builder().locator(".el").attribute("data-id", "42").build().execute(context);
    }

    @Test
    void shouldVerifyPageUrl() {
        when(browser.page().url()).thenReturn("https://example.com/home");

        new VerifyAction.Builder().url("https://example.com/home").build().execute(context);
    }

    @Test
    void shouldVerifyPageTitle() {
        when(browser.page().title()).thenReturn("ACME");

        new VerifyAction.Builder().title("ACME").build().execute(context);
    }

    @Test
    void shouldVerifyLocalStorageValue() {
        com.microsoft.playwright.WebStorage storage = mock(com.microsoft.playwright.WebStorage.class);
        when(browser.page().localStorage()).thenReturn(storage);
        when(storage.getItem("token")).thenReturn("abc");

        new VerifyAction.Builder().storageLocal("token", "abc").build().execute(context);
    }

    @Test
    void shouldVerifySessionStorageValue() {
        com.microsoft.playwright.WebStorage storage = mock(com.microsoft.playwright.WebStorage.class);
        when(browser.page().sessionStorage()).thenReturn(storage);
        when(storage.getItem("session")).thenReturn("xyz");

        new VerifyAction.Builder().storageSession("session", "xyz").build().execute(context);
    }

    @Test
    void shouldVerifyCookieValue() {
        browser.createContext("admin");
        Cookie cookie = new Cookie("session", "abc");
        cookie.domain = "example.com";
        cookie.path = "/";
        when(browser.context().cookies()).thenReturn(List.of(cookie));

        new VerifyAction.Builder().cookie("session", "abc").build().execute(context);
    }

    @Test
    void shouldFailFastWhenCountCheckMissingCount() {
        expectThrows(ValidationException.class,
                () -> new VerifyAction.Builder().locator(".el").check(VerifyAction.Check.COUNT).build());
    }

    @Test
    void shouldFailFastWhenLocatorMissing() {
        expectThrows(CitrusRuntimeException.class,
                () -> new VerifyAction.Builder().visible().build());
    }
}
