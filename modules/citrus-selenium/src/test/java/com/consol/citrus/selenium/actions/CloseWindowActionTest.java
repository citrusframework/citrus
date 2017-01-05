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

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class CloseWindowActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private ChromeDriver webDriver = Mockito.mock(ChromeDriver.class);
    private WebDriver.TargetLocator locator = Mockito.mock(WebDriver.TargetLocator.class);

    private CloseWindowAction action;

    @BeforeMethod
    public void setup() {
        reset(webDriver, locator);

        seleniumBrowser.setWebDriver(webDriver);

        action =  new CloseWindowAction();
        action.setBrowser(seleniumBrowser);

        when(webDriver.switchTo()).thenReturn(locator);
    }

    @Test
    public void testCloseActiveWindow() throws Exception {
        Set<String> windows = new HashSet<>();
        windows.add("active_window");
        windows.add("last_window");

        when(webDriver.getWindowHandles()).thenReturn(windows);
        when(webDriver.getWindowHandle()).thenReturn("active_window");

        context.setVariable(SeleniumHeaders.SELENIUM_LAST_WINDOW, "last_window");
        context.setVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW, "active_window");

        action.execute(context);

        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_LAST_WINDOW), "last_window");
        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW), "last_window");

        verify(webDriver).close();
        verify(locator).window("last_window");
    }

    @Test
    public void testCloseActiveWindowReturnToDefault() throws Exception {
        Set<String> windows = new HashSet<>();
        windows.add("active_window");
        windows.add("main_window");

        when(webDriver.getWindowHandles()).thenReturn(windows);
        when(webDriver.getWindowHandle()).thenReturn("active_window").thenReturn("active_window").thenReturn("main_window");

        context.setVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW, "active_window");

        action.execute(context);

        Assert.assertFalse(context.getVariables().containsKey(SeleniumHeaders.SELENIUM_LAST_WINDOW));
        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW), "main_window");

        verify(webDriver).close();
        verify(locator).defaultContent();
    }

    @Test
    public void testCloseOtherWindow() throws Exception {
        Set<String> windows = new HashSet<>();
        windows.add("active_window");
        windows.add("last_window");
        windows.add("other_window");

        when(webDriver.getWindowHandles()).thenReturn(windows);
        when(webDriver.getWindowHandle()).thenReturn("active_window");

        context.setVariable(SeleniumHeaders.SELENIUM_LAST_WINDOW, "last_window");
        context.setVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW, "active_window");
        context.setVariable("myWindow", "other_window");

        action.setWindowName("myWindow");
        action.execute(context);

        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_LAST_WINDOW), "last_window");
        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW), "active_window");

        verify(webDriver).close();
        verify(locator).window("other_window");
        verify(locator).window("active_window");
    }

    @Test
    public void testCloseOtherWindowNoActiveWindow() throws Exception {
        Set<String> windows = new HashSet<>();
        windows.add("active_window");
        windows.add("other_window");

        when(webDriver.getWindowHandles()).thenReturn(windows);
        when(webDriver.getWindowHandle()).thenReturn("active_window");

        context.setVariable("myWindow", "other_window");

        action.setWindowName("myWindow");
        action.execute(context);

        Assert.assertFalse(context.getVariables().containsKey(SeleniumHeaders.SELENIUM_LAST_WINDOW));
        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW), "active_window");

        verify(webDriver).close();
        verify(locator).window("other_window");
        verify(locator).window("active_window");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to find window handle.*")
    public void testCloseWindowInvalidWindowName() throws Exception {
        action.setWindowName("myWindow");
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to find window.*")
    public void testCloseWindowNotFound() throws Exception {
        Set<String> windows = new HashSet<>();
        windows.add("active_window");
        windows.add("last_window");

        when(webDriver.getWindowHandles()).thenReturn(windows);
        when(webDriver.getWindowHandle()).thenReturn("active_window");

        context.setVariable(SeleniumHeaders.SELENIUM_LAST_WINDOW, "last_window");
        context.setVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW, "active_window");
        context.setVariable("myWindow", "other_window");

        action.setWindowName("myWindow");
        action.execute(context);
    }

}