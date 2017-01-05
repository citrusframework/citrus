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

import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SetInputActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private WebDriver webDriver = Mockito.mock(WebDriver.class);
    private WebElement element = Mockito.mock(WebElement.class);

    private SetInputAction action;

    @BeforeMethod
    public void setup() {
        reset(webDriver, element);

        seleniumBrowser.setWebDriver(webDriver);

        action =  new SetInputAction();
        action.setBrowser(seleniumBrowser);

        action.setProperty("name");
        action.setPropertyValue("textField");

        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);
        when(element.getTagName()).thenReturn("input");
    }

    @Test
    public void testExecute() throws Exception {
        when(webDriver.findElement(any(By.class))).thenReturn(element);

        action.setValue("new_value");

        action.execute(context);

        verify(element).clear();
        verify(element).sendKeys("new_value");
    }

    @Test
    public void testExecuteOnSelect() throws Exception {
        WebElement option = Mockito.mock(WebElement.class);

        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.getTagName()).thenReturn("select");

        when(element.findElements(any(By.class))).thenReturn(Collections.singletonList(option));
        when(option.isSelected()).thenReturn(false);

        action.setValue("option");

        action.execute(context);

        verify(option).click();
    }

}