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
import static org.testng.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
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

import com.microsoft.playwright.options.Cookie;

class CookieActionTest {

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

        ValidationException exception = expectThrows(ValidationException.class, () -> action.execute(context));
        assertTrue(exception.getMessage().contains("wrong"));
    }

    @Test
    void shouldFailWhenReadingMissingCookie() {
        browser.createContext("admin");
        when(browser.context().cookies()).thenReturn(List.of());

        CookieAction action = new CookieAction.Builder().read("missing").variable("v").build();

        expectThrows(CitrusRuntimeException.class, () -> action.execute(context));
    }

    @Test
    void shouldFailFastWhenCommandMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new CookieAction.Builder().build());
    }

    @Test
    void shouldFailFastWhenReadVariableMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new CookieAction.Builder().read("session").build());
    }
}
