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

package org.citrusframework.selenium.actions;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class WaitUntilActionTest extends AbstractTestNGUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final WebDriver webDriver = Mockito.mock(WebDriver.class);
    private final WebElement element = Mockito.mock(WebElement.class);

    @BeforeMethod
    public void setup() {
        reset(webDriver, element);

        seleniumBrowser.setWebDriver(webDriver);

        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);
        when(element.getTagName()).thenReturn("button");
    }

    @Test
    public void testWaitForHidden() throws Exception {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(false);

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .condition("hidden")
                .build();
        action.execute(context);

        verify(element).isDisplayed();
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void testWaitForHiddenTimeout() throws Exception {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .condition("hidden")
                .timeout(1000L)
                .build();
        action.execute(context);
    }

    @Test
    public void testWaitForVisible() throws Exception {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .condition("visible")
                .build();
        action.execute(context);

        verify(element).isDisplayed();
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void testWaitForVisibleTimeout() throws Exception {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(false);

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .condition("visible")
                .timeout(1000L)
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testUnsupportedWaitCondition() throws Exception {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(false);

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .condition("unknown")
                .build();
        action.execute(context);
    }
}
