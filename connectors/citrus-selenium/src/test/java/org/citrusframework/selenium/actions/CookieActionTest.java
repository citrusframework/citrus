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

package org.citrusframework.selenium.actions;

import java.util.Date;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CookieActionTest extends AbstractUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final WebDriver webDriver = Mockito.mock(WebDriver.class);
    private final WebDriver.Options options = Mockito.mock(WebDriver.Options.class);

    @BeforeEach
    public void setup() {
        Mockito.reset(webDriver, options);

        seleniumBrowser.setWebDriver(webDriver);
        when(webDriver.manage()).thenReturn(options);
    }

    @Test
    public void shouldAddCookieWithAllAttributes() {
        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .add("token", "abc")
                .domain("localhost")
                .path("/")
                .expiry("1893456000")
                .secure(true)
                .httpOnly(true)
                .sameSite("Lax")
                .build()
                .execute(context);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(options).addCookie(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue();
        Assertions.assertEquals(cookie.getName(), "token");
        Assertions.assertEquals(cookie.getValue(), "abc");
        Assertions.assertEquals(cookie.getDomain(), "localhost");
        Assertions.assertEquals(cookie.getPath(), "/");
        Assertions.assertNotNull(cookie.getExpiry());
        Assertions.assertTrue(cookie.isSecure());
        Assertions.assertTrue(cookie.isHttpOnly());
        Assertions.assertEquals(cookie.getSameSite(), "Lax");
    }

    @Test
    public void shouldAddCookieWithExpiryDate() {
        Date expiry = new Date(1893456000000L);

        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .add("token", "abc")
                .expiry(expiry)
                .build()
                .execute(context);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(options).addCookie(cookieCaptor.capture());
        Assertions.assertEquals(cookieCaptor.getValue().getExpiry(), expiry);
    }

    @Test
    public void shouldReadCookieIntoVariable() {
        when(options.getCookieNamed("lang")).thenReturn(new Cookie("lang", "en"));

        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .read("lang", "language")
                .build()
                .execute(context);

        Assertions.assertEquals(context.getVariable("language"), "en");
    }

    @Test
    public void shouldVerifyCookieValue() {
        when(options.getCookieNamed("lang")).thenReturn(new Cookie("lang", "en"));

        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .verify("lang", "en")
                .build()
                .execute(context);

        verify(options).getCookieNamed("lang");
    }

    @Test
    public void shouldVerifyCookieValueWithMatcher() {
        when(options.getCookieNamed("lang")).thenReturn(new Cookie("lang", "en-US"));

        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .verify("lang", "@contains('en')@")
                .build()
                .execute(context);

        verify(options).getCookieNamed("lang");
    }

    @Test
    public void shouldVerifyCookieAbsent() {
        when(options.getCookieNamed("token")).thenReturn(null);

        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .verifyAbsent("token")
                .build()
                .execute(context);

        verify(options).getCookieNamed("token");
    }

    @Test
    public void shouldFailOnVerifyAbsentWhenPresent() {
        var e = Assertions.assertThrows(ValidationException.class, () -> {
        when(options.getCookieNamed("token")).thenReturn(new Cookie("token", "abc"));

        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .verifyAbsent("token")
                .build()
                .execute(context);
    });
        Assertions.assertTrue(e.getMessage().matches("Selenium validation failed, cookie 'token' expected 'absent', but was present"), e.getMessage());
    }

    @Test
    public void shouldFailOnVerifyMismatch() {
        var e = Assertions.assertThrows(ValidationException.class, () -> {
        when(options.getCookieNamed("lang")).thenReturn(new Cookie("lang", "en"));

        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .verify("lang", "de")
                .build()
                .execute(context);
    });
        Assertions.assertTrue(e.getMessage().matches("Selenium validation failed, cookie 'lang' expected 'de', but was 'en'"), e.getMessage());
    }

    @Test
    public void shouldFailOnReadMissingCookie() {
        var e = Assertions.assertThrows(CitrusRuntimeException.class, () -> {
        when(options.getCookieNamed("token")).thenReturn(null);

        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .read("token", "t")
                .build()
                .execute(context);
    });
        Assertions.assertTrue(e.getMessage().matches("Failed to find cookie 'token'"), e.getMessage());
    }

    @Test
    public void shouldDeleteCookie() {
        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .delete("token")
                .build()
                .execute(context);

        verify(options).deleteCookieNamed("token");
    }

    @Test
    public void shouldDeleteAllCookies() {
        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .deleteAll()
                .build()
                .execute(context);

        verify(options).deleteAllCookies();
    }

    @Test
    public void shouldFailOnMissingCommand() {
        var e = Assertions.assertThrows(CitrusRuntimeException.class, () -> {
        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .build()
                .execute(context);
    });
        Assertions.assertTrue(e.getMessage().matches("Missing cookies command - use one of add, read, verify, verify-absent, delete, delete-all"), e.getMessage());
    }

    @Test
    public void shouldFailOnUnsupportedCommand() {
        var e = Assertions.assertThrows(CitrusRuntimeException.class, () -> {
        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .command("unknown")
                .build()
                .execute(context);
    });
        Assertions.assertTrue(e.getMessage().matches("Unsupported cookies command 'unknown' - use one of add, read, verify, verify-absent, delete, delete-all"), e.getMessage());
    }

    @Test
    public void shouldFailOnReadWithoutVariable() {
        var e = Assertions.assertThrows(CitrusRuntimeException.class, () -> {
        when(options.getCookieNamed("lang")).thenReturn(new Cookie("lang", "en"));

        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .command("read")
                .name("lang")
                .build()
                .execute(context);
    });
        Assertions.assertTrue(e.getMessage().matches("Missing variable name for cookies command 'read'"), e.getMessage());
    }
}
