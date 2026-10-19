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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FrameActionTest extends AbstractTestNGUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final WebDriver webDriver = Mockito.mock(WebDriver.class);
    private final WebDriver.TargetLocator targetLocator = Mockito.mock(WebDriver.TargetLocator.class);
    private final WebElement frameElement = Mockito.mock(WebElement.class);

    @BeforeMethod
    public void setup() {
        Mockito.reset(webDriver, targetLocator, frameElement);

        seleniumBrowser.setWebDriver(webDriver);
        when(webDriver.switchTo()).thenReturn(targetLocator);
    }

    @Test
    public void shouldSwitchToFrameByElement() {
        when(webDriver.findElement(any(By.class))).thenReturn(frameElement);

        new FrameAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "editor")
                .build()
                .execute(context);

        verify(webDriver).findElement(By.id("editor"));
        verify(targetLocator).frame(frameElement);
    }

    @Test
    public void shouldSwitchToFrameByIndex() {
        new FrameAction.Builder()
                .browser(seleniumBrowser)
                .index(1)
                .build()
                .execute(context);

        verify(targetLocator).frame(1);
    }

    @Test
    public void shouldSwitchToFrameByNameOrId() {
        new FrameAction.Builder()
                .browser(seleniumBrowser)
                .nameOrId("editor")
                .build()
                .execute(context);

        verify(targetLocator).frame("editor");
    }

    @Test
    public void shouldResolveNameOrIdVariable() {
        context.setVariable("frameName", "editor");

        new FrameAction.Builder()
                .browser(seleniumBrowser)
                .nameOrId("${frameName}")
                .build()
                .execute(context);

        verify(targetLocator).frame("editor");
    }

    @Test
    public void shouldSwitchToParentFrame() {
        new FrameAction.Builder()
                .browser(seleniumBrowser)
                .parent()
                .build()
                .execute(context);

        verify(targetLocator).parentFrame();
    }

    @Test
    public void shouldSwitchToDefaultContent() {
        new FrameAction.Builder()
                .browser(seleniumBrowser)
                .defaultContent()
                .build()
                .execute(context);

        verify(targetLocator).defaultContent();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Missing frame target - use element, index, name, parent or default")
    public void shouldFailOnMissingTarget() {
        new FrameAction.Builder()
                .browser(seleniumBrowser)
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to find frame element 'By\\.id: missing'")
    public void shouldFailOnMissingFrameElement() {
        when(webDriver.findElement(any(By.class))).thenThrow(new NoSuchElementException("no such element"));

        new FrameAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "missing")
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to switch to frame")
    public void shouldFailOnMissingFrameIndex() {
        Mockito.doThrow(new NoSuchFrameException("no such frame")).when(targetLocator).frame(eq(5));

        new FrameAction.Builder()
                .browser(seleniumBrowser)
                .index(5)
                .build()
                .execute(context);
    }
}
