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

package org.citrusframework.selenium.groovy;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.api.actions.selenium.PageValidator;
import org.citrusframework.api.actions.selenium.WebPage;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.selenium.actions.*;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumBrowserConfiguration;
import org.citrusframework.selenium.pages.UserFormPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;


public class SeleniumTest extends AbstractGroovyActionDslTest {

    @Mock
    private SeleniumBrowser browser;

    @Mock
    private HtmlUnitDriver webDriver;

    @Mock
    private WebDriver.TargetLocator targetLocator;

    @Mock
    private Alert alert;

    @Mock
    private WebElement webElement;

    @Mock
    private WebElement option;

    @Mock
    private WebPage webPage;

    @Mock
    private PageValidator<?, ?> pageValidator;

    @Mock
    private WebDriver.Navigation navigation;

    @Mock
    private WebDriver.Options options;

    @BeforeEach
    public void setupMocks() {

        SeleniumBrowserConfiguration configuration = new SeleniumBrowserConfiguration();
        lenient().when(browser.getEndpointConfiguration()).thenReturn(configuration);
        lenient().when(browser.getName()).thenReturn("seleniumBrowser");
        lenient().when(browser.getWebDriver()).thenReturn(webDriver);
        lenient().when(webDriver.switchTo()).thenReturn(targetLocator);
        lenient().when(targetLocator.alert()).thenReturn(alert);
        lenient().when(alert.getText()).thenReturn("This is a warning message!");
        lenient().when(webDriver.findElement(any(By.class))).thenReturn(webElement);
        lenient().when(webElement.getText()).thenReturn("Ok");
        lenient().when(webElement.isDisplayed()).thenReturn(true, true, true, true, true, true, true, true, true, true, true, true, true)
                .thenReturn(false);
        lenient().when(webElement.isEnabled()).thenReturn(true);
        lenient().when(webElement.getAttribute("type")).thenReturn("submit");
        lenient().when(webElement.getAttribute("value")).thenReturn("Foo");
        lenient().when(webElement.getAttribute("enabled")).thenReturn("true");
        lenient().when(webElement.getCssValue("color")).thenReturn("#000000");
        lenient().when(webElement.getTagName()).thenReturn("button", "select");
        lenient().when(webElement.findElements(any(By.class))).thenReturn(List.of(option));
        lenient().when(option.isEnabled()).thenReturn(true);
        lenient().when(option.getText()).thenReturn("new-value");
        lenient().when(webDriver.executeScript(any(String.class), any(Object[].class))).thenReturn(Collections.emptyList()).thenReturn(List.of("Something went wrong"));
        lenient().when(webDriver.navigate()).thenReturn(navigation);
        lenient().when(webDriver.getWindowHandles()).thenReturn(Set.of("switchWindow", "closeWindow")).thenReturn(Set.of("newWindow", "switchWindow", "closeWindow"));
        lenient().when(webDriver.getWindowHandle()).thenReturn("newWindow", "switchWindow", "closeWindow");
        lenient().when(targetLocator.window(any(String.class))).thenReturn(webDriver);
        lenient().when(browser.getStoredFile(any(String.class))).thenReturn("file.txt");
        lenient().when(webDriver.manage()).thenReturn(options);
    }

    @Test
    public void shouldLoadSeleniumActions() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/selenium/groovy/selenium.citrus.test.groovy");

        context.getReferenceResolver().bind("seleniumBrowser", browser);
        context.getReferenceResolver().bind("userForm", webPage);
        context.getReferenceResolver().bind("pageValidator", pageValidator);

        context.setVariable("switchWindow", "switchWindow");
        context.setVariable("closeWindow", "closeWindow");

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals(result.getName(), "SeleniumTest");
        Assertions.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assertions.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assertions.assertEquals(result.getActionCount(), 25L);

        int actionIndex = 0;

        StartBrowserAction startAction = (StartBrowserAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(startAction.getBrowser());
        Assertions.assertEquals(startAction.getName(), "selenium:start");

        AlertAction alertAction = (AlertAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(alertAction.getBrowser());
        Assertions.assertEquals(alertAction.getName(), "selenium:alert");
        Assertions.assertNull(alertAction.getText());
        Assertions.assertTrue(alertAction.isAccept());

        alertAction = (AlertAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(alertAction.getBrowser());
        Assertions.assertEquals(alertAction.getName(), "selenium:alert");
        Assertions.assertEquals(alertAction.getText(), "This is a warning message!");
        Assertions.assertFalse(alertAction.isAccept());

        FindElementAction findElementAction = (FindElementAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(findElementAction.getBrowser());
        Assertions.assertEquals(findElementAction.getName(), "selenium:find");
        Assertions.assertEquals(findElementAction.getBy().toString(), "By.className: clickable");
        Assertions.assertEquals(findElementAction.getTagName(), "button");
        Assertions.assertEquals(findElementAction.getText(), "Ok");
        Assertions.assertEquals(findElementAction.getAttributes().size(), 1L);
        Assertions.assertEquals(findElementAction.getAttributes().get("type"), "submit");
        Assertions.assertEquals(findElementAction.getStyles().size(), 1L);
        Assertions.assertEquals(findElementAction.getStyles().get("color"), "#000000");
        Assertions.assertTrue(findElementAction.isDisplayed());
        Assertions.assertTrue(findElementAction.isEnabled());

        PageAction pageAction = (PageAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(pageAction.getBrowser());
        Assertions.assertEquals(pageAction.getName(), "selenium:page");
        Assertions.assertEquals(pageAction.getAction(), "setUserName");
        Assertions.assertEquals(pageAction.getPage(), context.getReferenceResolver().resolve("userForm"));
        Assertions.assertNull(pageAction.getType());
        Assertions.assertEquals(pageAction.getArguments().size(), 1L);
        Assertions.assertEquals(pageAction.getArguments().get(0), "${username}");
        Assertions.assertNull(pageAction.getValidator());

        pageAction = (PageAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(pageAction.getBrowser());
        Assertions.assertEquals(pageAction.getName(), "selenium:page");
        Assertions.assertEquals(pageAction.getAction(), "validate");
        Assertions.assertNull(pageAction.getPage());
        Assertions.assertEquals(pageAction.getType(), UserFormPage.class.getName());
        Assertions.assertEquals(pageAction.getArguments().size(), 0L);
        Assertions.assertEquals(pageAction.getValidator(), context.getReferenceResolver().resolve("pageValidator"));

        ClickAction clickAction = (ClickAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(clickAction.getBrowser());
        Assertions.assertEquals(clickAction.getName(), "selenium:click");
        Assertions.assertEquals(clickAction.getBy().toString(), "By.id: edit-link");

        HoverAction hoverAction = (HoverAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(hoverAction.getBrowser());
        Assertions.assertEquals(hoverAction.getName(), "selenium:hover");
        Assertions.assertEquals(hoverAction.getBy().toString(), "By.id: edit-link");

        SetInputAction setInputAction = (SetInputAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(setInputAction.getBrowser());
        Assertions.assertEquals(setInputAction.getName(), "selenium:set-input");
        Assertions.assertEquals(setInputAction.getBy().toString(), "By.tagName: input");
        Assertions.assertEquals(setInputAction.getValue(), "new-value");

        CheckInputAction checkInputAction = (CheckInputAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(checkInputAction.getBrowser());
        Assertions.assertEquals(checkInputAction.getName(), "selenium:check-input");
        Assertions.assertEquals(checkInputAction.getBy().toString(), "By.xpath: //input[@type: 'checkbox']");
        Assertions.assertTrue(checkInputAction.isChecked());

        DropDownSelectAction dropDownSelect = (DropDownSelectAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(dropDownSelect.getBrowser());
        Assertions.assertEquals(dropDownSelect.getName(), "selenium:dropdown-select");
        Assertions.assertEquals(dropDownSelect.getBy().toString(), "By.name: gender");
        Assertions.assertEquals(dropDownSelect.getOption(), "male");
        Assertions.assertEquals(dropDownSelect.getOptions().size(), 0L);

        DropDownSelectAction dropDownMultiSelect = (DropDownSelectAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(dropDownMultiSelect.getBrowser());
        Assertions.assertEquals(dropDownMultiSelect.getName(), "selenium:dropdown-select");
        Assertions.assertEquals(dropDownMultiSelect.getBy().toString(), "By.id: title");
        Assertions.assertNull(dropDownMultiSelect.getOption());
        Assertions.assertEquals(dropDownMultiSelect.getOptions().size(), 2L);

        FillFormAction fillForm = (FillFormAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(fillForm.getBrowser());
        Assertions.assertEquals(fillForm.getName(), "selenium:fill-form");
        Assertions.assertNull(fillForm.getSubmitButton());
        Assertions.assertEquals(fillForm.getFormFields().size(), 2L);
        Assertions.assertEquals(fillForm.getFormFields().get(By.id("username")), "foo_user");
        Assertions.assertEquals(fillForm.getFormFields().get(By.id("password")), "secret");

        fillForm = (FillFormAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(fillForm.getBrowser());
        Assertions.assertEquals(fillForm.getName(), "selenium:fill-form");
        Assertions.assertNotNull(fillForm.getSubmitButton());
        Assertions.assertEquals(fillForm.getSubmitButton(), By.id("save"));
        Assertions.assertEquals(fillForm.getFormFields().size(), 2L);
        Assertions.assertEquals(fillForm.getFormFields().get(By.id("username")), "foo_user");
        Assertions.assertEquals(fillForm.getFormFields().get(By.id("password")), "secret");

        WaitUntilAction waitUntilAction = (WaitUntilAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(waitUntilAction.getBrowser());
        Assertions.assertEquals(waitUntilAction.getName(), "selenium:wait");
        Assertions.assertEquals(waitUntilAction.getBy().toString(), "By.id: dialog");
        Assertions.assertEquals(waitUntilAction.getCondition(), "hidden");

        JavaScriptAction javaScriptAction = (JavaScriptAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(javaScriptAction.getBrowser());
        Assertions.assertEquals(javaScriptAction.getName(), "selenium:javascript");
        Assertions.assertEquals(javaScriptAction.getScript(), "alert('This is awesome!')");
        Assertions.assertEquals(javaScriptAction.getExpectedErrors().size(), 1L);
        Assertions.assertEquals(javaScriptAction.getExpectedErrors().get(0), "Something went wrong");

        MakeScreenshotAction screenshotAction = (MakeScreenshotAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(screenshotAction.getBrowser());
        Assertions.assertEquals(screenshotAction.getName(), "selenium:screenshot");
        Assertions.assertEquals(screenshotAction.getOutputDir(), "/tmp/storage");

        NavigateAction navigateAction = (NavigateAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(navigateAction.getBrowser());
        Assertions.assertEquals(navigateAction.getName(), "selenium:navigate");
        Assertions.assertEquals(navigateAction.getPage(), "back");

        OpenWindowAction openWindowAction = (OpenWindowAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(openWindowAction.getBrowser());
        Assertions.assertEquals(openWindowAction.getName(), "selenium:open-window");
        Assertions.assertEquals(openWindowAction.getWindowName(), "newWindow");

        SwitchWindowAction switchWindowAction = (SwitchWindowAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(switchWindowAction.getBrowser());
        Assertions.assertEquals(switchWindowAction.getName(), "selenium:switch-window");
        Assertions.assertEquals(switchWindowAction.getWindowName(), "switchWindow");

        CloseWindowAction closeWindowAction = (CloseWindowAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(closeWindowAction.getBrowser());
        Assertions.assertEquals(closeWindowAction.getName(), "selenium:close-window");
        Assertions.assertEquals(closeWindowAction.getWindowName(), "closeWindow");

        StoreFileAction storeFileAction = (StoreFileAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(storeFileAction.getBrowser());
        Assertions.assertEquals(storeFileAction.getName(), "selenium:store-file");
        Assertions.assertEquals(storeFileAction.getFilePath(), "classpath:download/file.txt");

        GetStoredFileAction getStoredFileAction = (GetStoredFileAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(getStoredFileAction.getBrowser());
        Assertions.assertEquals(getStoredFileAction.getName(), "selenium:get-stored-file");
        Assertions.assertEquals(getStoredFileAction.getFileName(), "file.txt");

        ClearBrowserCacheAction clearCacheAction = (ClearBrowserCacheAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(clearCacheAction.getBrowser());
        Assertions.assertEquals(clearCacheAction.getName(), "selenium:clear-cache");

        StopBrowserAction stopAction = (StopBrowserAction) result.getTestAction(actionIndex);
        Assertions.assertNotNull(stopAction.getBrowser());
        Assertions.assertEquals(stopAction.getName(), "selenium:stop");
    }
}
