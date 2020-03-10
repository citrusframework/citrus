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

package com.consol.citrus.dsl.runner;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.consol.citrus.TestCase;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.selenium.actions.AlertAction;
import com.consol.citrus.selenium.actions.CheckInputAction;
import com.consol.citrus.selenium.actions.ClearBrowserCacheAction;
import com.consol.citrus.selenium.actions.ClickAction;
import com.consol.citrus.selenium.actions.CloseWindowAction;
import com.consol.citrus.selenium.actions.FindElementAction;
import com.consol.citrus.selenium.actions.GetStoredFileAction;
import com.consol.citrus.selenium.actions.HoverAction;
import com.consol.citrus.selenium.actions.JavaScriptAction;
import com.consol.citrus.selenium.actions.NavigateAction;
import com.consol.citrus.selenium.actions.OpenWindowAction;
import com.consol.citrus.selenium.actions.SeleniumActionBuilder;
import com.consol.citrus.selenium.actions.SetInputAction;
import com.consol.citrus.selenium.actions.StartBrowserAction;
import com.consol.citrus.selenium.actions.StopBrowserAction;
import com.consol.citrus.selenium.actions.StoreFileAction;
import com.consol.citrus.selenium.actions.SwitchWindowAction;
import com.consol.citrus.selenium.actions.WaitUntilAction;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.endpoint.SeleniumBrowserConfiguration;
import com.consol.citrus.selenium.endpoint.SeleniumHeaders;
import com.consol.citrus.dsl.UnitTestSupport;
import org.mockito.Mockito;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.Coordinates;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.RemoteWebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumTestRunnerTest extends UnitTestSupport {

    private SeleniumBrowser seleniumBrowser = Mockito.mock(SeleniumBrowser.class);
    private SeleniumBrowserConfiguration seleniumBrowserConfiguration = Mockito.mock(SeleniumBrowserConfiguration.class);
    private ChromeDriver webDriver = Mockito.mock(ChromeDriver.class);
    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private WebElement element = Mockito.mock(WebElement.class);
    private WebElement button = Mockito.mock(WebElement.class);
    private RemoteWebElement link = Mockito.mock(RemoteWebElement.class);
    private WebElement input = Mockito.mock(WebElement.class);
    private WebElement checkbox = Mockito.mock(WebElement.class);
    private WebElement hidden = Mockito.mock(WebElement.class);
    private Alert alert = Mockito.mock(Alert.class);
    private WebDriver.Navigation navigation = Mockito.mock(WebDriver.Navigation.class);
    private WebDriver.TargetLocator locator = Mockito.mock(WebDriver.TargetLocator.class);
    private WebDriver.Options options = Mockito.mock(WebDriver.Options.class);

    private Mouse mouse = Mockito.mock(Mouse.class);
    private Keyboard keyboard = Mockito.mock(Keyboard.class);
    private Coordinates coordinates = Mockito.mock(Coordinates.class);

    @Test
    public void testSeleniumBuilder() {
        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        when(seleniumBrowser.getEndpointConfiguration()).thenReturn(seleniumBrowserConfiguration);
        when(seleniumBrowserConfiguration.getBrowserType()).thenReturn(BrowserType.CHROME);
        when(seleniumBrowser.getWebDriver()).thenReturn(webDriver);

        when(seleniumBrowser.getName()).thenReturn("mockBrowser");
        when(referenceResolver.resolve("mockBrowser", SeleniumBrowser.class)).thenReturn(seleniumBrowser);

        when(webDriver.navigate()).thenReturn(navigation);
        when(webDriver.manage()).thenReturn(options);
        when(webDriver.switchTo()).thenReturn(locator);
        when(locator.alert()).thenReturn(alert);
        when(alert.getText()).thenReturn("Hello!");

        when(webDriver.findElement(By.id("header"))).thenReturn(element);
        when(element.getTagName()).thenReturn("h1");
        when(element.isEnabled()).thenReturn(true);
        when(element.isDisplayed()).thenReturn(true);

        when(webDriver.findElement(By.linkText("Click Me!"))).thenReturn(link);
        when(link.getTagName()).thenReturn("a");
        when(link.isEnabled()).thenReturn(true);
        when(link.isDisplayed()).thenReturn(true);

        when(webDriver.findElement(By.linkText("Hover Me!"))).thenReturn(link);
        when(webDriver.getMouse()).thenReturn(mouse);
        when(webDriver.getKeyboard()).thenReturn(keyboard);

        when(link.getCoordinates()).thenReturn(coordinates);

        when(webDriver.findElement(By.name("username"))).thenReturn(input);
        when(input.getTagName()).thenReturn("input");
        when(input.isEnabled()).thenReturn(true);
        when(input.isDisplayed()).thenReturn(true);

        when(webDriver.findElement(By.name("hiddenButton"))).thenReturn(hidden);
        when(hidden.getTagName()).thenReturn("input");
        when(hidden.isEnabled()).thenReturn(true);
        when(hidden.isDisplayed()).thenReturn(false);

        when(webDriver.findElement(By.xpath("//input[@type='checkbox']"))).thenReturn(checkbox);
        when(checkbox.getTagName()).thenReturn("input");
        when(checkbox.isEnabled()).thenReturn(true);
        when(checkbox.isDisplayed()).thenReturn(true);
        when(checkbox.isSelected()).thenReturn(false);

        when(webDriver.executeScript(anyString())).thenReturn(Collections.singletonList("This went wrong!"));

        when(webDriver.findElement(By.className("btn"))).thenReturn(button);
        when(button.getTagName()).thenReturn("button");
        when(button.isEnabled()).thenReturn(false);
        when(button.isDisplayed()).thenReturn(false);
        when(button.getText()).thenReturn("Click Me!");
        when(button.getAttribute("type")).thenReturn("submit");
        when(button.getCssValue("color")).thenReturn("red");

        when(seleniumBrowser.getStoredFile("file.txt")).thenReturn("file.txt");
        Set<String> windows = new HashSet<>();
        windows.add("last_window");
        windows.add("new_window");
        when(webDriver.getWindowHandles()).thenReturn(Collections.singleton("last_window")).thenReturn(windows);
        when(webDriver.getWindowHandle()).thenReturn("last_window");

        context.setVariable("cssClass", "btn");

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                selenium(action -> action.start(seleniumBrowser));

                selenium(action -> action.navigate("http://localhost:9090"));

                selenium(action -> action.find().element(By.id("header")));
                selenium(action -> action.find().element("class-name", "${cssClass}")
                            .tagName("button")
                            .enabled(false)
                            .displayed(false)
                            .text("Click Me!")
                            .style("color", "red")
                            .attribute("type", "submit"));

                selenium(action -> action.click().element(By.linkText("Click Me!")));
                selenium(action -> action.hover().element(By.linkText("Hover Me!")));

                selenium(action -> action.setInput("Citrus").element(By.name("username")));
                selenium(action -> action.checkInput(false).element(By.xpath("//input[@type='checkbox']")));

                selenium(action -> action.javascript("alert('Hello!')")
                            .errors("This went wrong!"));

                selenium(action -> action.alert().text("Hello!").accept());

                selenium(SeleniumActionBuilder::clearCache);

                selenium(action -> action.store("classpath:download/file.txt"));
                selenium(action -> action.getStored("file.txt"));

                selenium(action -> action.open().window("my_window"));
                selenium(action -> action.focus().window("my_window"));
                selenium(action -> action.close().window("my_window"));

                selenium(action -> action.waitUntil().hidden().element(By.name("hiddenButton")));

                selenium(action -> action.stop(seleniumBrowser));
            }
        };

        TestCase test = builder.getTestCase();
        int actionIndex = 0;
        Assert.assertEquals(test.getActionCount(), 18);

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), StartBrowserAction.class);
        StartBrowserAction startBrowserAction = (StartBrowserAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(startBrowserAction.getName(), "selenium:start");
        Assert.assertNotNull(startBrowserAction.getBrowser());

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), NavigateAction.class);
        NavigateAction navigateAction = (NavigateAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(navigateAction.getName(), "selenium:navigate");
        Assert.assertEquals(navigateAction.getPage(), "http://localhost:9090");

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), FindElementAction.class);
        FindElementAction findElementAction = (FindElementAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(findElementAction.getName(), "selenium:find");
        Assert.assertEquals(findElementAction.getBy(), By.id("header"));

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), FindElementAction.class);
        findElementAction = (FindElementAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(findElementAction.getName(), "selenium:find");
        Assert.assertEquals(findElementAction.getProperty(), "class-name");
        Assert.assertEquals(findElementAction.getPropertyValue(), "${cssClass}");
        Assert.assertEquals(findElementAction.getTagName(), "button");
        Assert.assertEquals(findElementAction.getText(), "Click Me!");
        Assert.assertFalse(findElementAction.isEnabled());
        Assert.assertFalse(findElementAction.isDisplayed());
        Assert.assertEquals(findElementAction.getStyles().size(), 1L);
        Assert.assertEquals(findElementAction.getStyles().get("color"), "red");
        Assert.assertEquals(findElementAction.getAttributes().size(), 1L);
        Assert.assertEquals(findElementAction.getAttributes().get("type"), "submit");

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), ClickAction.class);
        ClickAction clickAction = (ClickAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(clickAction.getName(), "selenium:click");
        Assert.assertEquals(clickAction.getBy(), By.linkText("Click Me!"));

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), HoverAction.class);
        HoverAction hoverAction = (HoverAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(hoverAction.getName(), "selenium:hover");
        Assert.assertEquals(hoverAction.getBy(), By.linkText("Hover Me!"));

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), SetInputAction.class);
        SetInputAction setInputAction = (SetInputAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(setInputAction.getName(), "selenium:set-input");
        Assert.assertEquals(setInputAction.getBy(), By.name("username"));
        Assert.assertEquals(setInputAction.getValue(), "Citrus");

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), CheckInputAction.class);
        CheckInputAction checkInputAction = (CheckInputAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(checkInputAction.getName(), "selenium:check-input");
        Assert.assertEquals(checkInputAction.getBy(), By.xpath("//input[@type='checkbox']"));
        Assert.assertFalse(checkInputAction.isChecked());

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), JavaScriptAction.class);
        JavaScriptAction javaScriptAction = (JavaScriptAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(javaScriptAction.getName(), "selenium:javascript");
        Assert.assertEquals(javaScriptAction.getScript(), "alert('Hello!')");
        Assert.assertEquals(javaScriptAction.getExpectedErrors().size(), 1L);
        Assert.assertEquals(javaScriptAction.getExpectedErrors().get(0), "This went wrong!");

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), AlertAction.class);
        AlertAction alertAction = (AlertAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(alertAction.getName(), "selenium:alert");
        Assert.assertEquals(alertAction.getText(), "Hello!");

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), ClearBrowserCacheAction.class);
        ClearBrowserCacheAction clearBrowserCacheAction = (ClearBrowserCacheAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(clearBrowserCacheAction.getName(), "selenium:clear-cache");

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), StoreFileAction.class);
        StoreFileAction storeFileAction = (StoreFileAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(storeFileAction.getName(), "selenium:store-file");
        Assert.assertEquals(storeFileAction.getFilePath(), "classpath:download/file.txt");

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), GetStoredFileAction.class);
        GetStoredFileAction getStoredFileAction = (GetStoredFileAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(getStoredFileAction.getName(), "selenium:get-stored-file");
        Assert.assertEquals(getStoredFileAction.getFileName(), "file.txt");

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), OpenWindowAction.class);
        OpenWindowAction openWindowAction = (OpenWindowAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(openWindowAction.getName(), "selenium:open-window");
        Assert.assertEquals(openWindowAction.getWindowName(), "my_window");

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), SwitchWindowAction.class);
        SwitchWindowAction switchWindowAction = (SwitchWindowAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(switchWindowAction.getName(), "selenium:switch-window");
        Assert.assertEquals(switchWindowAction.getWindowName(), "my_window");

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), CloseWindowAction.class);
        CloseWindowAction closeWindowAction = (CloseWindowAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(closeWindowAction.getName(), "selenium:close-window");
        Assert.assertEquals(closeWindowAction.getWindowName(), "my_window");

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), WaitUntilAction.class);
        WaitUntilAction waitUntilAction = (WaitUntilAction) test.getActions().get(actionIndex++);
        Assert.assertEquals(waitUntilAction.getName(), "selenium:wait");
        Assert.assertEquals(waitUntilAction.getBy(), By.name("hiddenButton"));
        Assert.assertEquals(waitUntilAction.getCondition(), "hidden");

        Assert.assertEquals(test.getActions().get(actionIndex).getClass(), StopBrowserAction.class);
        StopBrowserAction stopBrowserAction = (StopBrowserAction) test.getActions().get(actionIndex);
        Assert.assertEquals(stopBrowserAction.getName(), "selenium:stop");
        Assert.assertNotNull(stopBrowserAction.getBrowser());

        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_ALERT_TEXT), "Hello!");
        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_DOWNLOAD_FILE), "file.txt");
        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_LAST_WINDOW), "last_window");
        Assert.assertEquals(context.getVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW), "new_window");
        Assert.assertEquals(context.getVariable("my_window"), "new_window");

        verify(alert).accept();
        verify(options).deleteAllCookies();
        verify(link).click();
        verify(input).clear();
        verify(input).sendKeys("Citrus");
    }
}
