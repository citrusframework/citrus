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
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class WaitUntilActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private WebDriver webDriver = Mockito.mock(WebDriver.class);
    private WebElement element = Mockito.mock(WebElement.class);

    private WaitUntilAction action;

    @BeforeMethod
    public void setup() {
        reset(webDriver, element);

        seleniumBrowser.setWebDriver(webDriver);

        action =  new WaitUntilAction();
        action.setBrowser(seleniumBrowser);

        action.setProperty("class-name");
        action.setPropertyValue("clickable");

        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);
        when(element.getTagName()).thenReturn("button");
    }

    @Test
    public void testWaitForHidden() throws Exception {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(false);

        action.setCondition("hidden");
        action.execute(context);

        verify(element).isDisplayed();
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void testWaitForHiddenTimeout() throws Exception {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);

        action.setTimeout(1000L);
        action.setCondition("hidden");
        action.execute(context);
    }

    @Test
    public void testWaitForVisible() throws Exception {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);

        action.setCondition("visible");
        action.execute(context);

        verify(element).isDisplayed();
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void testWaitForVisibleTimeout() throws Exception {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(false);

        action.setTimeout(1000L);
        action.setCondition("visible");
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testUnsupportedWaitCondition() throws Exception {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(false);

        action.setCondition("unknown");
        action.execute(context);
    }
}