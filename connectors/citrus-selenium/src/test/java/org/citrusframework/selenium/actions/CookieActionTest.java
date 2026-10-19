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
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CookieActionTest extends AbstractTestNGUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final WebDriver webDriver = Mockito.mock(WebDriver.class);
    private final WebDriver.Options options = Mockito.mock(WebDriver.Options.class);

    @BeforeMethod
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
        Assert.assertEquals(cookie.getName(), "token");
        Assert.assertEquals(cookie.getValue(), "abc");
        Assert.assertEquals(cookie.getDomain(), "localhost");
        Assert.assertEquals(cookie.getPath(), "/");
        Assert.assertNotNull(cookie.getExpiry());
        Assert.assertTrue(cookie.isSecure());
        Assert.assertTrue(cookie.isHttpOnly());
        Assert.assertEquals(cookie.getSameSite(), "Lax");
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
        Assert.assertEquals(cookieCaptor.getValue().getExpiry(), expiry);
    }

    @Test
    public void shouldReadCookieIntoVariable() {
        when(options.getCookieNamed("lang")).thenReturn(new Cookie("lang", "en"));

        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .read("lang", "language")
                .build()
                .execute(context);

        Assert.assertEquals(context.getVariable("language"), "en");
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

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Selenium validation failed, cookie 'token' expected 'absent', but was present")
    public void shouldFailOnVerifyAbsentWhenPresent() {
        when(options.getCookieNamed("token")).thenReturn(new Cookie("token", "abc"));

        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .verifyAbsent("token")
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Selenium validation failed, cookie 'lang' expected 'de', but was 'en'")
    public void shouldFailOnVerifyMismatch() {
        when(options.getCookieNamed("lang")).thenReturn(new Cookie("lang", "en"));

        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .verify("lang", "de")
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to find cookie 'token'")
    public void shouldFailOnReadMissingCookie() {
        when(options.getCookieNamed("token")).thenReturn(null);

        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .read("token", "t")
                .build()
                .execute(context);
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

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Missing cookies command - use one of add, read, verify, verify-absent, delete, delete-all")
    public void shouldFailOnMissingCommand() {
        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Unsupported cookies command 'unknown' - use one of add, read, verify, verify-absent, delete, delete-all")
    public void shouldFailOnUnsupportedCommand() {
        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .command("unknown")
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Missing variable name for cookies command 'read'")
    public void shouldFailOnReadWithoutVariable() {
        when(options.getCookieNamed("lang")).thenReturn(new Cookie("lang", "en"));

        new CookieAction.Builder()
                .browser(seleniumBrowser)
                .command("read")
                .name("lang")
                .build()
                .execute(context);
    }
}
