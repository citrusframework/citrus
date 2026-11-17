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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.options.Cookie;

class CookieActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;

    @BeforeEach
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
    }

    @AfterEach
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldAddCookie() {
        browser.createContext("admin");

        new CookieAction.Builder().add("session", "abc123").build().execute(context);

        verify(browser.context()).addCookies(anyList());
    }

    @Test
    void shouldClearCookies() {
        browser.createContext("admin");

        new CookieAction.Builder().clear().build().execute(context);

        verify(browser.context()).clearCookies();
    }

    @Test
    void shouldReadCookieValueIntoVariable() {
        browser.createContext("admin");
        Cookie cookie = new Cookie("session", "abc123");
        when(browser.context().cookies()).thenReturn(List.of(cookie));

        new CookieAction.Builder().read("session").variable("sessionValue").build().execute(context);

        assertEquals("abc123", context.getVariable("sessionValue"));
    }

    @Test
    void shouldVerifyCookieValue() {
        browser.createContext("admin");
        Cookie cookie = new Cookie("session", "abc123");
        when(browser.context().cookies()).thenReturn(List.of(cookie));

        new CookieAction.Builder().verify("session", "abc123").build().execute(context);
    }

    @Test
    void shouldFailValidationWhenCookieValueMismatches() {
        browser.createContext("admin");
        Cookie cookie = new Cookie("session", "abc123");
        when(browser.context().cookies()).thenReturn(List.of(cookie));

        CookieAction action = new CookieAction.Builder().verify("session", "wrong").build();

        ValidationException exception = assertThrows(ValidationException.class, () -> action.execute(context));
        assertTrue(exception.getMessage().contains("wrong"));
    }

    @Test
    void shouldFailWhenReadingMissingCookie() {
        browser.createContext("admin");
        when(browser.context().cookies()).thenReturn(List.of());

        CookieAction action = new CookieAction.Builder().read("missing").variable("v").build();

        assertThrows(CitrusRuntimeException.class, () -> action.execute(context));
    }

    @Test
    void shouldFailFastWhenCommandMissing() {
        assertThrows(CitrusRuntimeException.class, () -> new CookieAction.Builder().build());
    }

    @Test
    void shouldFailFastWhenReadVariableMissing() {
        assertThrows(CitrusRuntimeException.class, () -> new CookieAction.Builder().read("session").build());
    }

    @Test
    void shouldReadAndVerifyCookieFilteredByUrl() {
        browser.createContext("admin");
        context.setVariable("apiUrl", "https://app.example.com/api/orders");
        Cookie cookie = new Cookie("session", "scoped");
        when(browser.context().cookies("https://app.example.com/api/orders")).thenReturn(List.of(cookie));

        new CookieAction.Builder()
                .read("session")
                .url("${apiUrl}")
                .variable("scopedSession")
                .build()
                .execute(context);
        new CookieAction.Builder()
                .verify("session", "scoped")
                .url("${apiUrl}")
                .build()
                .execute(context);

        assertEquals("scoped", context.getVariable("scopedSession"));
        verify(browser.context(), org.mockito.Mockito.times(2)).cookies("https://app.example.com/api/orders");
    }

    @Test
    void shouldFailWithUrlInMessageWhenUrlFilteredCookieMissing() {
        browser.createContext("admin");
        when(browser.context().cookies("https://sub.example.com/")).thenReturn(List.of());

        CookieAction action = new CookieAction.Builder()
                .read("host_only")
                .url("https://sub.example.com/")
                .variable("v")
                .build();

        CitrusRuntimeException error = assertThrows(CitrusRuntimeException.class, () -> action.execute(context));
        assertTrue(error.getMessage().contains("https://sub.example.com/"), error.getMessage());
    }

    @Test
    void shouldReadAndVerifyNamelessCookie() {
        browser.createContext("admin");
        Cookie nameless = new Cookie("", "raw-token");
        when(browser.context().cookies()).thenReturn(List.of(nameless));

        new CookieAction.Builder().read("").variable("nameless").build().execute(context);
        new CookieAction.Builder().verify("", "raw-token").build().execute(context);

        assertEquals("raw-token", context.getVariable("nameless"));
    }
}
