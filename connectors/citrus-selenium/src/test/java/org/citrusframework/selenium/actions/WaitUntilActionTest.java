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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
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
    public void testWaitForVisible() {
        when(webDriver.findElement(any(By.class))).thenReturn(element);

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .condition("visible")
                .build();
        action.execute(context);

        verify(element).isDisplayed();
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void testWaitForVisibleTimeout() {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(false);

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .condition("visible")
                .timeout(500L)
                .build();
        action.execute(context);
    }

    @Test
    public void testWaitForHidden() {
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

    @Test
    public void testWaitForHiddenWhenElementIsMissing() {
        when(webDriver.findElement(any(By.class))).thenThrow(new NoSuchElementException("Not found"));

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .condition("hidden")
                .build();
        action.execute(context);

        verify(webDriver).findElement(any(By.class));
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void testWaitForHiddenTimeout() {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .condition("hidden")
                .timeout(500L)
                .build();
        action.execute(context);
    }

    @Test
    public void testWaitForPresent() {
        when(webDriver.findElement(any(By.class))).thenReturn(element);

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .present()
                .build();
        action.execute(context);

        verify(webDriver).findElement(By.className("clickable"));
    }

    @Test
    public void testWaitForElementToAppear() {
        when(webDriver.findElement(any(By.class)))
                .thenThrow(new NoSuchElementException("Not found"))
                .thenReturn(element);

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "myButton")
                .condition("present")
                .timeout(10000L)
                .build();
        action.execute(context);

        verify(webDriver, Mockito.times(2)).findElement(By.id("myButton"));
    }

    @Test
    public void testWaitForAbsent() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.emptyList());

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .absent()
                .build();
        action.execute(context);

        verify(webDriver).findElements(By.className("clickable"));
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void testWaitForAbsentTimeout() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .absent()
                .timeout(500L)
                .build();
        action.execute(context);
    }

    @Test
    public void testWaitForClickable() {
        when(webDriver.findElement(any(By.class))).thenReturn(element);

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .clickable()
                .build();
        action.execute(context);

        verify(element).isDisplayed();
        verify(element).isEnabled();
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void testWaitForClickableTimeout() {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.isEnabled()).thenReturn(false);

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .clickable()
                .timeout(500L)
                .build();
        action.execute(context);
    }

    @Test
    public void testWaitForText() {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.getText()).thenReturn("This is the element text");

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "myButton")
                .text("element text")
                .build();
        action.execute(context);

        verify(element).getText();
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void testWaitForTextTimeout() {
        when(webDriver.findElement(any(By.class))).thenReturn(element);
        when(element.getText()).thenReturn("This is the element text");

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "myButton")
                .text("other text")
                .timeout(500L)
                .build();
        action.execute(context);
    }

    @Test
    public void testWaitForUrl() {
        when(webDriver.getCurrentUrl()).thenReturn("https://citrusframework.org/selenium/test");

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .url("selenium")
                .build();
        action.execute(context);

        verify(webDriver).getCurrentUrl();
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void testWaitForUrlTimeout() {
        when(webDriver.getCurrentUrl()).thenReturn("https://citrusframework.org/selenium/test");

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .url("other")
                .timeout(500L)
                .build();
        action.execute(context);
    }

    @Test
    public void testWaitForTitle() {
        when(webDriver.getTitle()).thenReturn("Citrus Selenium Test");

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .title("Selenium")
                .build();
        action.execute(context);

        verify(webDriver).getTitle();
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void testWaitForTitleTimeout() {
        when(webDriver.getTitle()).thenReturn("Citrus Selenium Test");

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .title("other")
                .timeout(500L)
                .build();
        action.execute(context);
    }

    @Test
    public void testWaitWithDynamicContentInExpectedValue() {
        when(webDriver.getTitle()).thenReturn("Citrus Selenium Test");
        context.setVariable("titleFragment", "Selenium");

        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .title("${titleFragment}")
                .build();
        action.execute(context);

        verify(webDriver).getTitle();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Missing expected value for wait condition 'text'")
    public void testWaitForTextWithoutExpectedValue() {
        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "myButton")
                .condition("text")
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Missing expected value for wait condition 'url'")
    public void testWaitForUrlWithoutExpectedValue() {
        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .condition("url")
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Missing element selector for wait condition 'visible'")
    public void testWaitWithoutElementSelector() {
        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .condition("visible")
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Unsupported wait condition 'unknown' - use one of visible, hidden, present, absent, clickable, text, url, title")
    public void testUnsupportedWaitCondition() {
        WaitUntilAction action =  new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "clickable")
                .condition("unknown")
                .build();
        action.execute(context);
    }

    @Test
    public void testNullTimeoutKeepsDefault() {
        WaitUntilAction action = new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .timeout(null)
                .condition("title")
                .expected("Citrus")
                .build();
        Assert.assertEquals(action.getTimeout(), Long.valueOf(5000L));
    }

    @Test
    public void testConditionIsCaseInsensitive() {
        WaitUntilAction action = new WaitUntilAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "myButton")
                .condition("Visible")
                .build();
        Assert.assertEquals(action.getCondition(), "visible");
    }
}
