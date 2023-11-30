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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class FillFormActionTest extends AbstractTestNGUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final WebDriver webDriver = Mockito.mock(WebDriver.class);
    private final WebElement element = Mockito.mock(WebElement.class);

    @BeforeMethod
    public void setup() {
        reset(webDriver, element);

        seleniumBrowser.setWebDriver(webDriver);

        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);
        when(element.getTagName()).thenReturn("input");
    }

    @Test
    public void testExecute() throws Exception {
        when(webDriver.findElement(any(By.class))).thenReturn(element);

        FillFormAction action =  new FillFormAction.Builder()
                .browser(seleniumBrowser)
                .field("username", "foo_user")
                .field("password", "secret")
                .build();
        action.execute(context);

        verify(element, times(2)).clear();
        verify(element).sendKeys("foo_user");
        verify(element).sendKeys("secret");
    }

    @Test
    public void testExecuteWithSelect() throws Exception {
        WebElement option = Mockito.mock(WebElement.class);

        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.getTagName()).thenReturn("select");

        when(element.findElements(any(By.class))).thenReturn(Collections.singletonList(option));
        when(option.isEnabled()).thenReturn(true);
        when(option.isSelected()).thenReturn(false);

        FillFormAction action =  new FillFormAction.Builder()
                .browser(seleniumBrowser)
                .field("remember-me", "yes")
                .build();
        action.execute(context);

        verify(option).click();
    }

    @Test
    public void testExecuteWithJson() throws Exception {
        when(webDriver.findElement(any(By.class))).thenReturn(element);

        FillFormAction action =  new FillFormAction.Builder()
                .browser(seleniumBrowser)
                .fromJson("""
                {
                    "username": "foo_user",
                    "password": "secret"
                }
                """)
                .build();
        action.execute(context);

        verify(element, times(2)).clear();
        verify(element).sendKeys("foo_user");
        verify(element).sendKeys("secret");
    }

    @Test
    public void testExecuteWithFormSubmit() throws Exception {
        when(webDriver.findElement(any(By.class))).thenReturn(element);

        FillFormAction action =  new FillFormAction.Builder()
                .browser(seleniumBrowser)
                .field("username", "foo_user")
                .field("password", "secret")
                .submit("save")
                .build();
        action.execute(context);

        verify(element, times(2)).clear();
        verify(element).sendKeys("foo_user");
        verify(element).sendKeys("secret");
        verify(element).click();
    }

}
