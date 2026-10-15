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

import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

/**
 * @since 2.7
 */
public class ClearBrowserCacheActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private WebDriver webDriver = Mockito.mock(WebDriver.class);
    private WebDriver.Options webDriverOptions = Mockito.mock(WebDriver.Options.class);

    @BeforeMethod
    public void setup() {
        reset(webDriver, webDriverOptions);

        seleniumBrowser.setWebDriver(webDriver);

        when(webDriver.manage()).thenReturn(webDriverOptions);
    }

    @Test
    public void testExecute() {
        ClearBrowserCacheAction action =  new ClearBrowserCacheAction.Builder()
                .browser(seleniumBrowser)
                .build();
        action.execute(context);

        verify(webDriverOptions).deleteAllCookies();
    }

    @Test
    public void testExecuteClearsWebStorage() {
        ChromeDriver chromeDriver = Mockito.mock(ChromeDriver.class);
        when(chromeDriver.manage()).thenReturn(webDriverOptions);

        seleniumBrowser.setWebDriver(chromeDriver);

        ClearBrowserCacheAction action =  new ClearBrowserCacheAction.Builder()
                .browser(seleniumBrowser)
                .build();
        action.execute(context);

        verify(webDriverOptions).deleteAllCookies();
        verify(chromeDriver).executeScript(contains("window.localStorage.clear()"));
        verify(chromeDriver).executeScript(contains("window.sessionStorage.clear()"));
    }

    @Test
    public void testExecuteClearsWebStorageWithRealDriver() {
        HtmlUnitDriver htmlUnitDriver = new HtmlUnitDriver(true);
        try {
            htmlUnitDriver.get("about:blank");
            htmlUnitDriver.executeScript("window.localStorage.setItem('key', 'value'); window.sessionStorage.setItem('key', 'value');", new Object[]{});

            seleniumBrowser.setWebDriver(htmlUnitDriver);

            ClearBrowserCacheAction action =  new ClearBrowserCacheAction.Builder()
                    .browser(seleniumBrowser)
                    .build();
            action.execute(context);

            Assert.assertNull(htmlUnitDriver.executeScript("return window.localStorage.getItem('key');", new Object[]{}));
            Assert.assertNull(htmlUnitDriver.executeScript("return window.sessionStorage.getItem('key');", new Object[]{}));
        } finally {
            htmlUnitDriver.quit();
        }
    }
}
