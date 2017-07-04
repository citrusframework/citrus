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

import com.consol.citrus.TestCase;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.selenium.actions.*;
import com.consol.citrus.selenium.endpoint.*;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.RemoteWebElement;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumTestRunnerTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = Mockito.mock(SeleniumBrowser.class);
    private SeleniumBrowserConfiguration seleniumBrowserConfiguration = Mockito.mock(SeleniumBrowserConfiguration.class);
    private ChromeDriver webDriver = Mockito.mock(ChromeDriver.class);
    private ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);
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
        when(applicationContextMock.getBean(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        when(seleniumBrowser.getEndpointConfiguration()).thenReturn(seleniumBrowserConfiguration);
        when(seleniumBrowserConfiguration.getBrowserType()).thenReturn(BrowserType.CHROME);
        when(seleniumBrowser.getWebDriver()).thenReturn(webDriver);

        when(seleniumBrowser.getName()).thenReturn("mockBrowser");
        when(applicationContextMock.getBean("mockBrowser", SeleniumBrowser.class)).thenReturn(seleniumBrowser);

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

        context.setApplicationContext(applicationContextMock);
        context.setVariable("cssClass", "btn");

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
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

                selenium(action -> action.clearCache());

                selenium(action -> action.store("classpath:download/file.txt"));
                selenium(action -> action.getStored("file.txt"));

                selenium(action -> action.open().window("my_window"));
                selenium(action -> action.focus().window("my_window"));
                selenium(action -> action.close().window("my_window"));

                selenium(action -> action.waitUntil().hidden().element(By.name("hiddenButton")));

                selenium(action -> action.stop());
            }
        };

        TestCase test = builder.getTestCase();
        int actionIndex = 0;
        Assert.assertEquals(test.getActionCount(), 18);

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), StartBrowserAction.class);
        StartBrowserAction startBrowserAction = (StartBrowserAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(startBrowserAction.getName(), "selenium:start");
        Assert.assertNotNull(startBrowserAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), NavigateAction.class);
        NavigateAction navigateAction = (NavigateAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(navigateAction.getName(), "selenium:navigate");
        Assert.assertEquals(navigateAction.getPage(), "http://localhost:9090");
        Assert.assertNotNull(navigateAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), FindElementAction.class);
        FindElementAction findElementAction = (FindElementAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(findElementAction.getName(), "selenium:find");
        Assert.assertEquals(findElementAction.getBy(), By.id("header"));
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), FindElementAction.class);
        findElementAction = (FindElementAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
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
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), ClickAction.class);
        ClickAction clickAction = (ClickAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(clickAction.getName(), "selenium:click");
        Assert.assertEquals(clickAction.getBy(), By.linkText("Click Me!"));
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), HoverAction.class);
        HoverAction hoverAction = (HoverAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(hoverAction.getName(), "selenium:hover");
        Assert.assertEquals(hoverAction.getBy(), By.linkText("Hover Me!"));
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), SetInputAction.class);
        SetInputAction setInputAction = (SetInputAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(setInputAction.getName(), "selenium:set-input");
        Assert.assertEquals(setInputAction.getBy(), By.name("username"));
        Assert.assertEquals(setInputAction.getValue(), "Citrus");
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), CheckInputAction.class);
        CheckInputAction checkInputAction = (CheckInputAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(checkInputAction.getName(), "selenium:check-input");
        Assert.assertEquals(checkInputAction.getBy(), By.xpath("//input[@type='checkbox']"));
        Assert.assertFalse(checkInputAction.isChecked());
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), JavaScriptAction.class);
        JavaScriptAction javaScriptAction = (JavaScriptAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(javaScriptAction.getName(), "selenium:javascript");
        Assert.assertEquals(javaScriptAction.getScript(), "alert('Hello!')");
        Assert.assertEquals(javaScriptAction.getExpectedErrors().size(), 1L);
        Assert.assertEquals(javaScriptAction.getExpectedErrors().get(0), "This went wrong!");
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), AlertAction.class);
        AlertAction alertAction = (AlertAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(alertAction.getName(), "selenium:alert");
        Assert.assertEquals(alertAction.getText(), "Hello!");
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), ClearBrowserCacheAction.class);
        ClearBrowserCacheAction clearBrowserCacheAction = (ClearBrowserCacheAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(clearBrowserCacheAction.getName(), "selenium:clear-cache");
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), StoreFileAction.class);
        StoreFileAction storeFileAction = (StoreFileAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(storeFileAction.getName(), "selenium:store-file");
        Assert.assertEquals(storeFileAction.getFilePath(), "classpath:download/file.txt");
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), GetStoredFileAction.class);
        GetStoredFileAction getStoredFileAction = (GetStoredFileAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(getStoredFileAction.getName(), "selenium:get-stored-file");
        Assert.assertEquals(getStoredFileAction.getFileName(), "file.txt");
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), OpenWindowAction.class);
        OpenWindowAction openWindowAction = (OpenWindowAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(openWindowAction.getName(), "selenium:open-window");
        Assert.assertEquals(openWindowAction.getWindowName(), "my_window");
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), SwitchWindowAction.class);
        SwitchWindowAction switchWindowAction = (SwitchWindowAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(switchWindowAction.getName(), "selenium:switch-window");
        Assert.assertEquals(switchWindowAction.getWindowName(), "my_window");
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), CloseWindowAction.class);
        CloseWindowAction closeWindowAction = (CloseWindowAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(closeWindowAction.getName(), "selenium:close-window");
        Assert.assertEquals(closeWindowAction.getWindowName(), "my_window");
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), WaitUntilAction.class);
        WaitUntilAction waitUntilAction = (WaitUntilAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(waitUntilAction.getName(), "selenium:wait");
        Assert.assertEquals(waitUntilAction.getBy(), By.name("hiddenButton"));
        Assert.assertEquals(waitUntilAction.getCondition(), "hidden");
        Assert.assertNotNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), StopBrowserAction.class);
        StopBrowserAction stopBrowserAction = (StopBrowserAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
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
