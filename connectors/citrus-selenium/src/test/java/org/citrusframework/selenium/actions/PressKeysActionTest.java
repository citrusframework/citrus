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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PressKeysActionTest extends AbstractTestNGUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final WebDriver webDriver = Mockito.mock(WebDriver.class);
    private final WebElement element = Mockito.mock(WebElement.class);
    private final WebDriver.TargetLocator targetLocator = Mockito.mock(WebDriver.TargetLocator.class);

    @BeforeMethod
    public void setup() {
        reset(webDriver, element, targetLocator);

        seleniumBrowser.setWebDriver(webDriver);

        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);
        when(element.getTagName()).thenReturn("input");

        when(webDriver.switchTo()).thenReturn(targetLocator);
        when(targetLocator.activeElement()).thenReturn(element);
    }

    @Test
    public void testPressEnterKey() {
        when(webDriver.findElement(By.id("username"))).thenReturn(element);

        PressKeysAction action = new PressKeysAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "username")
                .keys("ENTER")
                .build();
        action.execute(context);

        verify(element).sendKeys(Keys.ENTER);
    }

    @Test
    public void testPressKeyCombination() {
        when(webDriver.findElement(By.id("username"))).thenReturn(element);

        PressKeysAction action = new PressKeysAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "username")
                .keys("CONTROL+A")
                .build();
        action.execute(context);

        verify(element).sendKeys(Keys.CONTROL, "A");
    }

    @Test
    public void testPressSingleCharacter() {
        when(webDriver.findElement(By.id("username"))).thenReturn(element);

        PressKeysAction action = new PressKeysAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "username")
                .keys("a")
                .build();
        action.execute(context);

        verify(element).sendKeys("a");
    }

    @Test
    public void testPressKeysOnFocusedElement() {
        PressKeysAction action = new PressKeysAction.Builder()
                .browser(seleniumBrowser)
                .keys("ENTER")
                .build();
        action.execute(context);

        verify(webDriver).switchTo();
        verify(targetLocator).activeElement();
        verify(element).sendKeys(Keys.ENTER);
    }

    @Test
    public void testPressKeysWithDynamicContent() {
        context.setVariable("keyToPress", "TAB");

        when(webDriver.findElement(By.id("username"))).thenReturn(element);

        PressKeysAction action = new PressKeysAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "username")
                .keys("${keyToPress}")
                .build();
        action.execute(context);

        verify(element).sendKeys(Keys.TAB);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Unknown key 'ENTR' - use a Selenium key name such as ENTER, TAB or ESCAPE or a single character")
    public void testUnknownKeyName() {
        when(webDriver.findElement(By.id("username"))).thenReturn(element);

        PressKeysAction action = new PressKeysAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "username")
                .keys("ENTR")
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Missing keys to press")
    public void testMissingKeys() {
        when(webDriver.findElement(By.id("username"))).thenReturn(element);

        PressKeysAction action = new PressKeysAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "username")
                .build();
        action.execute(context);
    }
}
