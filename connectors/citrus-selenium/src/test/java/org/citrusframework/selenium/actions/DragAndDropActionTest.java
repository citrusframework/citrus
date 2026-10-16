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
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Coordinates;
import org.openqa.selenium.remote.RemoteWebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class DragAndDropActionTest extends AbstractTestNGUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final ChromeDriver webDriver = Mockito.mock(ChromeDriver.class);

    private final RemoteWebElement source = Mockito.mock(RemoteWebElement.class);
    private final RemoteWebElement target = Mockito.mock(RemoteWebElement.class);

    @BeforeMethod
    public void setup() {
        reset(webDriver, source, target);

        seleniumBrowser.setWebDriver(webDriver);

        when(source.isDisplayed()).thenReturn(true);
        when(source.isEnabled()).thenReturn(true);
        when(source.getTagName()).thenReturn("button");

        when(source.getCoordinates()).thenReturn(Mockito.mock(Coordinates.class));

        when(target.isDisplayed()).thenReturn(true);
        when(target.isEnabled()).thenReturn(true);
        when(target.getTagName()).thenReturn("div");

        when(target.getCoordinates()).thenReturn(Mockito.mock(Coordinates.class));
    }

    @Test
    public void testExecute() {
        when(webDriver.findElement(By.id("source"))).thenReturn(source);
        when(webDriver.findElement(By.id("target"))).thenReturn(target);

        DragAndDropAction action = new DragAndDropAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "source")
                .target("id", "target")
                .build();
        action.execute(context);
    }

    @Test
    public void testExecuteWithBySelectors() {
        when(webDriver.findElement(By.id("source"))).thenReturn(source);
        when(webDriver.findElement(By.cssSelector(".drop-zone"))).thenReturn(target);

        DragAndDropAction action = new DragAndDropAction.Builder()
                .browser(seleniumBrowser)
                .element(By.id("source"))
                .target(By.cssSelector(".drop-zone"))
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Failed to find element 'By.id: target' on page")
    public void testMissingTargetElement() {
        when(webDriver.findElement(By.id("source"))).thenReturn(source);
        when(webDriver.findElement(By.id("target"))).thenThrow(new NoSuchElementException("No such element"));

        DragAndDropAction action = new DragAndDropAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "source")
                .target("id", "target")
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Missing target element for drag and drop action")
    public void testMissingTargetSelector() {
        when(webDriver.findElement(By.id("source"))).thenReturn(source);

        DragAndDropAction action = new DragAndDropAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "source")
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Invalid element identifier object, expected By object but got null")
    public void testNullTargetObject() {
        new DragAndDropAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "source")
                .target((Object) null)
                .build();
    }
}
