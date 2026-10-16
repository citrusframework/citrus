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

import java.util.Collections;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Browser;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
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
    public void testExecuteSelect() {
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
    public void testExecuteSelectWithDynamicContent() {
        WebElement option = Mockito.mock(WebElement.class);

        context.setVariable("optionToSelect", "select_me");

        when(webDriver.findElement(any(By.class))).thenReturn(element);

        when(element.findElements(any(By.class))).thenReturn(Collections.singletonList(option));
        when(option.isEnabled()).thenReturn(true);
        when(option.isSelected()).thenReturn(false);

        DropDownSelectAction action =  new DropDownSelectAction.Builder()
                .browser(seleniumBrowser)
                .element("name", "dropdown")
                .option("${optionToSelect}")
                .build();
        action.execute(context);

        verify(option).click();
    }

    @Test
    public void testExecuteSelectByText() {
        WebElement option = Mockito.mock(WebElement.class);

        when(webDriver.findElement(any(By.class))).thenReturn(element);

        when(element.findElements(any(By.class))).thenReturn(Collections.singletonList(option));
        when(element.getCssValue(anyString())).thenReturn("visible");
        when(option.getCssValue(anyString())).thenReturn("visible");
        when(option.isEnabled()).thenReturn(true);
        when(option.isSelected()).thenReturn(false);

        DropDownSelectAction action =  new DropDownSelectAction.Builder()
                .browser(seleniumBrowser)
                .element("name", "dropdown")
                .byText()
                .option("Option 1")
                .build();
        action.execute(context);

        verify(option).click();
    }

    @Test
    public void testExecuteSelectByIndex() {
        WebElement option = Mockito.mock(WebElement.class);

        when(webDriver.findElement(any(By.class))).thenReturn(element);

        when(element.findElements(any(By.class))).thenReturn(Collections.singletonList(option));
        when(option.getAttribute("index")).thenReturn("1");
        when(option.isEnabled()).thenReturn(true);
        when(option.isSelected()).thenReturn(false);

        DropDownSelectAction action =  new DropDownSelectAction.Builder()
                .browser(seleniumBrowser)
                .element("name", "dropdown")
                .byIndex()
                .option("1")
                .build();
        action.execute(context);

        verify(option).getAttribute("index");
        verify(option).click();
    }

    @Test
    public void testExecuteSelectByInvalidIndex() {
        when(webDriver.findElement(any(By.class))).thenReturn(element);

        when(element.findElements(any(By.class))).thenReturn(Collections.emptyList());

        DropDownSelectAction action =  new DropDownSelectAction.Builder()
                .browser(seleniumBrowser)
                .element("name", "dropdown")
                .byIndex()
                .option("noIndex")
                .build();

        try {
            action.execute(context);
            Assert.fail("Expected action to fail with invalid option index");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid option index 'noIndex'"),
                    "Unexpected error message: " + e.getMessage());
        }
    }

    @Test
    public void testExecuteDeselectAll() {
        WebElement option = Mockito.mock(WebElement.class);

        when(webDriver.findElement(any(By.class))).thenReturn(element);

        when(element.getDomAttribute("multiple")).thenReturn("multiple");
        when(element.findElements(any(By.class))).thenReturn(Collections.singletonList(option));
        when(option.isSelected()).thenReturn(true);

        DropDownSelectAction action =  new DropDownSelectAction.Builder()
                .browser(seleniumBrowser)
                .element("name", "dropdown")
                .deselectAll()
                .build();
        action.execute(context);

        verify(option).click();
    }

    @Test
    public void testExecuteDeselectByValue() {
        WebElement option = Mockito.mock(WebElement.class);

        when(webDriver.findElement(any(By.class))).thenReturn(element);

        when(element.getDomAttribute("multiple")).thenReturn("multiple");
        when(element.findElements(any(By.class))).thenReturn(Collections.singletonList(option));
        when(option.isSelected()).thenReturn(true);

        DropDownSelectAction action =  new DropDownSelectAction.Builder()
                .browser(seleniumBrowser)
                .element("name", "dropdown")
                .deselect("option1")
                .build();
        action.execute(context);

        verify(option).click();
    }

    @Test
    public void testExecuteDeselectByText() {
        WebElement option = Mockito.mock(WebElement.class);

        when(webDriver.findElement(any(By.class))).thenReturn(element);

        when(element.getDomAttribute("multiple")).thenReturn("multiple");
        when(element.findElements(any(By.class))).thenReturn(Collections.singletonList(option));
        when(option.isSelected()).thenReturn(true);

        DropDownSelectAction action =  new DropDownSelectAction.Builder()
                .browser(seleniumBrowser)
                .element("name", "dropdown")
                .byText()
                .deselect("Option 1")
                .build();
        action.execute(context);

        verify(option).click();
    }

    @Test
    public void testExecuteDeselectByIndex() {
        WebElement option = Mockito.mock(WebElement.class);

        when(webDriver.findElement(any(By.class))).thenReturn(element);

        when(element.getDomAttribute("multiple")).thenReturn("multiple");
        when(element.findElements(any(By.class))).thenReturn(Collections.singletonList(option));
        when(option.getAttribute("index")).thenReturn("1");
        when(option.isSelected()).thenReturn(true);

        DropDownSelectAction action =  new DropDownSelectAction.Builder()
                .browser(seleniumBrowser)
                .element("name", "dropdown")
                .byIndex()
                .deselect("1")
                .build();
        action.execute(context);

        verify(option).getAttribute("index");
        verify(option).click();
    }

    @Test
    public void testUnknownSelectionMode() {
        when(webDriver.findElement(any(By.class))).thenReturn(element);

        DropDownSelectAction action =  new DropDownSelectAction.Builder()
                .browser(seleniumBrowser)
                .element("name", "dropdown")
                .by("unknown")
                .option("select_me")
                .build();

        try {
            action.execute(context);
            Assert.fail("Expected action to fail with unknown selection mode");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Unknown selection mode 'unknown'"),
                    "Unexpected error message: " + e.getMessage());
        }
    }

    @Test
    public void testMultiSelectKeyPerOperatingSystem() {
        Assert.assertEquals(DropDownSelectAction.multiSelectKey("Mac OS X"), Keys.COMMAND);
        Assert.assertEquals(DropDownSelectAction.multiSelectKey("Mac"), Keys.COMMAND);
        Assert.assertEquals(DropDownSelectAction.multiSelectKey("Windows 11"), Keys.CONTROL);
        Assert.assertEquals(DropDownSelectAction.multiSelectKey("Linux"), Keys.CONTROL);
        Assert.assertEquals(DropDownSelectAction.multiSelectKey(null), Keys.CONTROL);
    }

    @Test
    public void testExecuteMultiSelect() {
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

    @Test
    public void testExecuteMultiSelectByIndex() {
        WebElement option = Mockito.mock(WebElement.class);

        seleniumBrowser.getEndpointConfiguration().setBrowserType(Browser.IE.browserName());

        when(webDriver.findElement(any(By.class))).thenReturn(element);

        when(element.findElements(any(By.class))).thenReturn(Collections.singletonList(option));
        when(option.getAttribute("index")).thenReturn("0");
        when(option.isEnabled()).thenReturn(true);
        when(option.isSelected()).thenReturn(false);

        DropDownSelectAction action =  new DropDownSelectAction.Builder()
                .browser(seleniumBrowser)
                .element("name", "dropdown")
                .byIndex()
                .options("0")
                .build();
        action.execute(context);

        verify(option).getAttribute("index");
        verify(option).click();
    }
}
