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

import java.util.Collections;

import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Browser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class DropDownSelectActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser;
    private WebDriver webDriver = Mockito.mock(WebDriver.class);
    private WebElement element = Mockito.mock(WebElement.class);

    @BeforeMethod
    public void setup() {
        reset(webDriver, element);

        seleniumBrowser = new SeleniumBrowser();
        seleniumBrowser.setWebDriver(webDriver);

        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);
        when(element.getTagName()).thenReturn("select");
    }

    @Test
    public void testExecuteSelect() throws Exception {
        WebElement option = Mockito.mock(WebElement.class);

        when(webDriver.findElement(any(By.class))).thenReturn(element);

        when(element.findElements(any(By.class))).thenReturn(Collections.singletonList(option));
        when(option.isEnabled()).thenReturn(true);
        when(option.isSelected()).thenReturn(false);

        DropDownSelectAction action =  new DropDownSelectAction.Builder()
                .browser(seleniumBrowser)
                .element("name", "dropdown")
                .option("select_me")
                .build();
        action.execute(context);

        verify(option).click();
    }

    @Test
    public void testExecuteMultiSelect() throws Exception {
        WebElement option = Mockito.mock(WebElement.class);

        seleniumBrowser.getEndpointConfiguration().setBrowserType(Browser.IE.browserName());

        when(webDriver.findElement(any(By.class))).thenReturn(element);

        when(element.findElements(any(By.class))).thenReturn(Collections.singletonList(option));
        when(option.isEnabled()).thenReturn(true);
        when(option.isSelected()).thenReturn(false);

        DropDownSelectAction action =  new DropDownSelectAction.Builder()
                .browser(seleniumBrowser)
                .element("name", "dropdown")
                .options("option1", "option2")
                .build();
        action.execute(context);

        verify(option, times(2)).click();
    }



}
