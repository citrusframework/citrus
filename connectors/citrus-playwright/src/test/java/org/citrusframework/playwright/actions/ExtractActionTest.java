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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Proxy;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.model.LocatorSpec;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaSnapshotMode;
import com.microsoft.playwright.options.Cookie;

class ExtractActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;
    private Locator locator;

    @BeforeEach
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        locator = mock(Locator.class);
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        when(browser.page().locator(anyString())).thenReturn(locator);
    }

    @AfterEach
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
        when(storage.getItem("theme")).thenReturn("abc");

        new ExtractAction.Builder().storageLocal("theme").variable("stored").build().execute(context);

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
    void shouldExtractNamelessCookieValue() {
        browser.createContext("admin");
        Cookie cookie = new Cookie("", "nameless-token");
        when(browser.context().cookies()).thenReturn(List.of(cookie));

        new ExtractAction.Builder().cookie("").variable("nameless").build().execute(context);

        assertEquals("nameless-token", context.getVariable("nameless"));
    }

    @Test
    void shouldFailFastWhenCookieNameNull() {
        assertThrows(CitrusRuntimeException.class,
                () -> new ExtractAction.Builder().cookie(null).variable("nameless").build());
    }

    @Test
    void shouldStoreEmptyStringWhenResultNull() {
        when(locator.textContent()).thenReturn(null);

        new ExtractAction.Builder().locator(".el").text().variable("text").build().execute(context);

        assertEquals("", context.getVariable("text"));
    }

    @Test
    void shouldFailFastWhenVariableMissing() {
        assertThrows(CitrusRuntimeException.class,
                () -> new ExtractAction.Builder().locator(".el").text().build());
    }

    @Test
    void shouldFailFastWhenLocatorMissing() {
        assertThrows(CitrusRuntimeException.class,
                () -> new ExtractAction.Builder().text().variable("text").build());
        assertThrows(CitrusRuntimeException.class,
                () -> new ExtractAction.Builder().normalizedLocator().variable("norm").build());
    }

    @Test
    void shouldExtractPageAndLocatorAriaSnapshotWithAiOptions() {
        when(browser.page().ariaSnapshot()).thenReturn("- document");
        new ExtractAction.Builder().ariaSnapshot().variable("defaultPageSnap").build().execute(context);
        assertEquals("- document", context.getVariable("defaultPageSnap"));

        when(browser.page().ariaSnapshot(any(Page.AriaSnapshotOptions.class)))
                .thenReturn("- button \"Save\" [ref=e2]");
        new ExtractAction.Builder()
                .ariaSnapshot("ai")
                .boxes(true)
                .depth(2)
                .timeout(1200D)
                .variable("aiPageSnap")
                .build()
                .execute(context);
        assertEquals("- button \"Save\" [ref=e2]", context.getVariable("aiPageSnap"));

        ArgumentCaptor<Page.AriaSnapshotOptions> pageOptions = ArgumentCaptor.forClass(Page.AriaSnapshotOptions.class);
        verify(browser.page()).ariaSnapshot(pageOptions.capture());
        assertEquals(AriaSnapshotMode.AI, pageOptions.getValue().mode);
        assertEquals(Boolean.TRUE, pageOptions.getValue().boxes);
        assertEquals(Integer.valueOf(2), pageOptions.getValue().depth);
        assertEquals(Double.valueOf(1200D), pageOptions.getValue().timeout);

        when(locator.ariaSnapshot(any(Locator.AriaSnapshotOptions.class)))
                .thenReturn("- region \"Form\" [ref=e1]");
        new ExtractAction.Builder()
                .locator("#form")
                .ariaSnapshot(AriaSnapshotMode.AI)
                .depth(1)
                .variable("aiLocatorSnap")
                .build()
                .execute(context);
        assertEquals("- region \"Form\" [ref=e1]", context.getVariable("aiLocatorSnap"));
    }

    @Test
    void shouldExtractNormalizedLocatorFromRefLocator() {
        Locator refLocator = mock(Locator.class);
        Locator normalized = (Locator) Proxy.newProxyInstance(
                Locator.class.getClassLoader(),
                new Class<?>[]{Locator.class},
                (proxy, method, args) -> "toString".equals(method.getName())
                        ? "getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(\"Submit\"))"
                        : null);
        when(browser.page().getByRef("e2")).thenReturn(refLocator);
        when(refLocator.normalize()).thenReturn(normalized);

        new ExtractAction.Builder()
                .locator(LocatorSpec.ref("e2"))
                .normalizedLocator()
                .variable("normalizedCode")
                .build()
                .execute(context);

        assertEquals(
                "getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(\"Submit\"))",
                context.getVariable("normalizedCode"));
    }

    @Test
    void shouldRejectInvalidAriaSnapshotOptions() {
        assertThrows(CitrusRuntimeException.class,
                () -> new ExtractAction.Builder().locator(".el").text().ariaMode("ai").variable("out").build());
        assertThrows(CitrusRuntimeException.class,
                () -> new ExtractAction.Builder().ariaSnapshot("ai").depth(-1).variable("out").build());
        assertThrows(CitrusRuntimeException.class,
                () -> new ExtractAction.Builder().ariaSnapshot("unknown").variable("out").build());
    }
}
