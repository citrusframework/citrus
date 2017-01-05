/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.selenium.actions;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.endpoint.SeleniumHeaders;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class OpenWindowActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private ChromeDriver webDriver = Mockito.mock(ChromeDriver.class);
    private WebDriver.TargetLocator locator = Mockito.mock(WebDriver.TargetLocator.class);

    private OpenWindowAction action;

    @BeforeMethod
    public void setup() {
        reset(webDriver, locator);

        seleniumBrowser.setWebDriver(webDriver);

        action =  new OpenWindowAction();
        action.setBrowser(seleniumBrowser);

        when(webDriver.switchTo()).thenReturn(locator);
    }

    @Test
    public void testOpenWindow() throws Exception {
        Set<String> windows = new HashSet<>();
        windows.add("active_window");
        windows.add("new_window");

        when(webDriver.getWindowHandles()).thenReturn(Collections.singleton("active_window")).thenReturn(windows);
        when(webDriver.getWindowHandle()).thenReturn("active_window");

        when(webDriver.executeScript(eq("window.open();"))).thenReturn(Collections.emptyList());

        action.setWindowName("myNewWindow");

        action.execute(context);

        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_LAST_WINDOW), "active_window");
        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW), "new_window");
        Assert.assertEquals(context.getVariable("myNewWindow"), "new_window");

        verify(locator).window("new_window");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to open new window")
    public void testOpenWindowFailed() throws Exception {
        when(webDriver.getWindowHandles()).thenReturn(Collections.singleton("active_window"));
        when(webDriver.getWindowHandle()).thenReturn("active_window");

        when(webDriver.executeScript(eq("window.open();"))).thenReturn(Collections.emptyList());

        action.setWindowName("myNewWindow");

        action.execute(context);
    }

}