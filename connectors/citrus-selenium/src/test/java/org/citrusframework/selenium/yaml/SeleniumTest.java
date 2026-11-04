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

package org.citrusframework.selenium.yaml;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.api.actions.selenium.PageValidator;
import org.citrusframework.api.actions.selenium.WebPage;
import org.citrusframework.selenium.actions.*;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumBrowserConfiguration;
import org.citrusframework.selenium.pages.UserFormPage;
import org.citrusframework.yaml.YamlTestLoader;
import org.citrusframework.yaml.actions.YamlTestActionBuilder;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;


public class SeleniumTest extends AbstractYamlActionTest {

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
    private WebElement dialog;

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
        lenient().when(targetLocator.activeElement()).thenReturn(webElement);
        lenient().when(targetLocator.alert()).thenReturn(alert);
        lenient().when(alert.getText()).thenReturn("This is a warning message!");
        lenient().when(webDriver.findElement(any(By.class))).thenReturn(webElement);
        lenient().when(webDriver.getCurrentUrl()).thenReturn("https://citrusframework.org/selenium");
        lenient().when(webDriver.getTitle()).thenReturn("Selenium page");
        lenient().when(webDriver.findElement(By.id("dialog"))).thenReturn(dialog);
        lenient().when(dialog.isDisplayed()).thenReturn(false);
        lenient().when(dialog.getText()).thenReturn("Ok");
        lenient().when(webElement.getText()).thenReturn("Ok");
        lenient().when(webElement.isDisplayed()).thenReturn(true);
        lenient().when(webElement.isEnabled()).thenReturn(true);
        lenient().when(webElement.getDomAttribute("multiple")).thenReturn("multiple");
        lenient().when(webElement.getAttribute("type")).thenReturn("submit");
        lenient().when(webElement.getAttribute("value")).thenReturn("Foo");
        lenient().when(webElement.getAttribute("enabled")).thenReturn("true");
        lenient().when(webElement.getCssValue(anyString())).thenReturn("visible");
        lenient().when(webElement.getCssValue("color")).thenReturn("#000000");
        lenient().when(webElement.getTagName()).thenReturn("button", "select");
        lenient().when(webElement.findElements(any(By.class))).thenReturn(List.of(option));
        lenient().when(option.isEnabled()).thenReturn(true);
        lenient().when(option.getText()).thenReturn("new-value");
        lenient().when(option.getCssValue(anyString())).thenReturn("visible");
        lenient().when(option.getAttribute("index")).thenReturn("1");
        lenient().when(webDriver.executeScript(any(String.class), any(Object[].class))).thenReturn(Collections.emptyList()).thenReturn(List.of("Something went wrong"));
        lenient().when(webDriver.navigate()).thenReturn(navigation);
        lenient().when(webDriver.getWindowHandles()).thenReturn(Set.of("switchWindow", "closeWindow")).thenReturn(Set.of("newWindow", "switchWindow", "closeWindow"));
        lenient().when(webDriver.getWindowHandle()).thenReturn("newWindow", "switchWindow", "closeWindow");
        lenient().when(targetLocator.window(any(String.class))).thenReturn(webDriver);
        lenient().when(browser.getStoredFile(any(String.class))).thenReturn("file.txt");
        lenient().when(webDriver.manage()).thenReturn(options);
        lenient().when(options.getCookieNamed("session")).thenReturn(new org.openqa.selenium.Cookie("session", "abc123"));
        lenient().when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(webElement));
        lenient().when(webDriver.findElements(By.id("dialog"))).thenReturn(List.of(dialog));
        lenient().when(webDriver.findElements(By.id("missing"))).thenReturn(Collections.emptyList());
        lenient().when(webDriver.findElements(By.className("order"))).thenReturn(List.of(webElement, webElement));
        lenient().when(webElement.getDomAttribute("href")).thenReturn("https://citrusframework.org");
    }

    @Test
    public void shouldLoadSeleniumActions() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/selenium/yaml/selenium.citrus.it.yaml");

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
        Assertions.assertEquals(result.getActionCount(), 59L);

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
        Assertions.assertEquals(findElementAction.getProperty(), "class-name");
        Assertions.assertEquals(findElementAction.getPropertyValue(), "clickable");
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
        Assertions.assertEquals(clickAction.getProperty(), "id");
        Assertions.assertEquals(clickAction.getPropertyValue(), "edit-link");

        HoverAction hoverAction = (HoverAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(hoverAction.getBrowser());
        Assertions.assertEquals(hoverAction.getName(), "selenium:hover");
        Assertions.assertEquals(hoverAction.getProperty(), "id");
        Assertions.assertEquals(hoverAction.getPropertyValue(), "edit-link");

        DoubleClickAction doubleClickAction = (DoubleClickAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(doubleClickAction.getBrowser());
        Assertions.assertEquals(doubleClickAction.getName(), "selenium:double-click");
        Assertions.assertEquals(doubleClickAction.getProperty(), "id");
        Assertions.assertEquals(doubleClickAction.getPropertyValue(), "double-click-me");

        RightClickAction rightClickAction = (RightClickAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(rightClickAction.getBrowser());
        Assertions.assertEquals(rightClickAction.getName(), "selenium:right-click");
        Assertions.assertEquals(rightClickAction.getProperty(), "id");
        Assertions.assertEquals(rightClickAction.getPropertyValue(), "right-click-me");

        DragAndDropAction dragAndDropAction = (DragAndDropAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(dragAndDropAction.getBrowser());
        Assertions.assertEquals(dragAndDropAction.getName(), "selenium:drag-and-drop");
        Assertions.assertEquals(dragAndDropAction.getProperty(), "id");
        Assertions.assertEquals(dragAndDropAction.getPropertyValue(), "source");
        Assertions.assertEquals(dragAndDropAction.getTarget().resolve(context).toString(), "By.id: target");

        PressKeysAction pressKeysAction = (PressKeysAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(pressKeysAction.getBrowser());
        Assertions.assertEquals(pressKeysAction.getName(), "selenium:press");
        Assertions.assertEquals(pressKeysAction.getProperty(), "id");
        Assertions.assertEquals(pressKeysAction.getPropertyValue(), "username");
        Assertions.assertEquals(pressKeysAction.getKeys(), "ENTER");

        pressKeysAction = (PressKeysAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(pressKeysAction.getBrowser());
        Assertions.assertEquals(pressKeysAction.getName(), "selenium:press");
        Assertions.assertNull(pressKeysAction.getBy());
        Assertions.assertEquals(pressKeysAction.getKeys(), "TAB");

        SetInputAction setInputAction = (SetInputAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(setInputAction.getBrowser());
        Assertions.assertEquals(setInputAction.getName(), "selenium:set-input");
        Assertions.assertEquals(setInputAction.getProperty(), "tag-name");
        Assertions.assertEquals(setInputAction.getPropertyValue(), "input");
        Assertions.assertEquals(setInputAction.getValue(), "new-value");

        CheckInputAction checkInputAction = (CheckInputAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(checkInputAction.getBrowser());
        Assertions.assertEquals(checkInputAction.getName(), "selenium:check-input");
        Assertions.assertEquals(checkInputAction.getProperty(), "xpath");
        Assertions.assertEquals(checkInputAction.getPropertyValue(), "//input[@type: 'checkbox']");
        Assertions.assertTrue(checkInputAction.isChecked());

        DropDownSelectAction dropDownSelect = (DropDownSelectAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(dropDownSelect.getBrowser());
        Assertions.assertEquals(dropDownSelect.getName(), "selenium:dropdown-select");
        Assertions.assertEquals(dropDownSelect.getProperty(), "name");
        Assertions.assertEquals(dropDownSelect.getPropertyValue(), "gender");
        Assertions.assertEquals(dropDownSelect.getOption(), "male");
        Assertions.assertEquals(dropDownSelect.getOptions().size(), 0L);

        DropDownSelectAction dropDownMultiSelect = (DropDownSelectAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(dropDownMultiSelect.getBrowser());
        Assertions.assertEquals(dropDownMultiSelect.getName(), "selenium:dropdown-select");
        Assertions.assertEquals(dropDownMultiSelect.getProperty(), "id");
        Assertions.assertEquals(dropDownMultiSelect.getPropertyValue(), "title");
        Assertions.assertNull(dropDownMultiSelect.getOption());
        Assertions.assertEquals(dropDownMultiSelect.getOptions().size(), 2L);

        DropDownSelectAction dropDownSelectByText = (DropDownSelectAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(dropDownSelectByText.getBrowser());
        Assertions.assertEquals(dropDownSelectByText.getName(), "selenium:dropdown-select");
        Assertions.assertEquals(dropDownSelectByText.getSelectionMode(), "text");
        Assertions.assertEquals(dropDownSelectByText.getOption(), "Mr.");

        DropDownSelectAction dropDownSelectByIndex = (DropDownSelectAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(dropDownSelectByIndex.getBrowser());
        Assertions.assertEquals(dropDownSelectByIndex.getName(), "selenium:dropdown-select");
        Assertions.assertEquals(dropDownSelectByIndex.getSelectionMode(), "index");
        Assertions.assertEquals(dropDownSelectByIndex.getOption(), "1");

        DropDownSelectAction dropDownDeselectAll = (DropDownSelectAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(dropDownDeselectAll.getBrowser());
        Assertions.assertEquals(dropDownDeselectAll.getName(), "selenium:dropdown-select");
        Assertions.assertTrue(dropDownDeselectAll.isDeselectAll());

        DropDownSelectAction dropDownDeselectOptions = (DropDownSelectAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(dropDownDeselectOptions.getBrowser());
        Assertions.assertEquals(dropDownDeselectOptions.getName(), "selenium:dropdown-select");
        Assertions.assertEquals(dropDownDeselectOptions.getDeselectOptions().size(), 2L);

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
        Assertions.assertEquals(waitUntilAction.getProperty(), "id");
        Assertions.assertEquals(waitUntilAction.getPropertyValue(), "dialog");
        Assertions.assertEquals(waitUntilAction.getCondition(), "hidden");

        WaitUntilAction waitPresentAction = (WaitUntilAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(waitPresentAction.getBrowser());
        Assertions.assertEquals(waitPresentAction.getName(), "selenium:wait");
        Assertions.assertEquals(waitPresentAction.getProperty(), "id");
        Assertions.assertEquals(waitPresentAction.getPropertyValue(), "dialog");
        Assertions.assertEquals(waitPresentAction.getCondition(), "present");

        WaitUntilAction waitClickableAction = (WaitUntilAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(waitClickableAction.getBrowser());
        Assertions.assertEquals(waitClickableAction.getName(), "selenium:wait");
        Assertions.assertEquals(waitClickableAction.getProperty(), "id");
        Assertions.assertEquals(waitClickableAction.getPropertyValue(), "submit-button");
        Assertions.assertEquals(waitClickableAction.getCondition(), "clickable");
        Assertions.assertEquals(waitClickableAction.getTimeout().longValue(), 10000L);

        WaitUntilAction waitTextAction = (WaitUntilAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(waitTextAction.getBrowser());
        Assertions.assertEquals(waitTextAction.getName(), "selenium:wait");
        Assertions.assertEquals(waitTextAction.getProperty(), "id");
        Assertions.assertEquals(waitTextAction.getPropertyValue(), "dialog");
        Assertions.assertEquals(waitTextAction.getCondition(), "text");
        Assertions.assertEquals(waitTextAction.getExpected(), "Ok");

        WaitUntilAction waitUrlAction = (WaitUntilAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(waitUrlAction.getBrowser());
        Assertions.assertEquals(waitUrlAction.getName(), "selenium:wait");
        Assertions.assertNull(waitUrlAction.getBy());
        Assertions.assertEquals(waitUrlAction.getCondition(), "url");
        Assertions.assertEquals(waitUrlAction.getExpected(), "selenium");

        WaitUntilAction waitTitleAction = (WaitUntilAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(waitTitleAction.getBrowser());
        Assertions.assertEquals(waitTitleAction.getName(), "selenium:wait");
        Assertions.assertNull(waitTitleAction.getBy());
        Assertions.assertEquals(waitTitleAction.getCondition(), "title");
        Assertions.assertEquals(waitTitleAction.getExpected(), "Selenium");

        JavaScriptAction javaScriptAction = (JavaScriptAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(javaScriptAction.getBrowser());
        Assertions.assertEquals(javaScriptAction.getName(), "selenium:javascript");
        Assertions.assertEquals(javaScriptAction.getScript(), "alert('This is awesome!')");
        Assertions.assertEquals(javaScriptAction.getVariable(), "scriptResult");
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

        VerifyAction verifyUrlAction = (VerifyAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(verifyUrlAction.getBrowser());
        Assertions.assertEquals(verifyUrlAction.getName(), "selenium:verify");
        Assertions.assertEquals(verifyUrlAction.getCheck(), "url");
        Assertions.assertEquals(verifyUrlAction.getExpected(), "@endsWith('/selenium')@");
        Assertions.assertNull(verifyUrlAction.getElement());

        VerifyAction verifyTitleAction = (VerifyAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(verifyTitleAction.getBrowser());
        Assertions.assertEquals(verifyTitleAction.getName(), "selenium:verify");
        Assertions.assertEquals(verifyTitleAction.getCheck(), "title");
        Assertions.assertEquals(verifyTitleAction.getExpected(), "Selenium page");
        Assertions.assertNull(verifyTitleAction.getElement());

        VerifyAction verifyPresentAction = (VerifyAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(verifyPresentAction.getBrowser());
        Assertions.assertEquals(verifyPresentAction.getName(), "selenium:verify");
        Assertions.assertEquals(verifyPresentAction.getCheck(), "present");
        Assertions.assertEquals(verifyPresentAction.getElement().getProperty(), "id");
        Assertions.assertEquals(verifyPresentAction.getElement().getPropertyValue(), "dialog");

        VerifyAction verifyAbsentAction = (VerifyAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(verifyAbsentAction.getBrowser());
        Assertions.assertEquals(verifyAbsentAction.getName(), "selenium:verify");
        Assertions.assertEquals(verifyAbsentAction.getCheck(), "absent");
        Assertions.assertEquals(verifyAbsentAction.getElement().getProperty(), "id");
        Assertions.assertEquals(verifyAbsentAction.getElement().getPropertyValue(), "missing");

        VerifyAction verifyDisplayedAction = (VerifyAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(verifyDisplayedAction.getBrowser());
        Assertions.assertEquals(verifyDisplayedAction.getName(), "selenium:verify");
        Assertions.assertEquals(verifyDisplayedAction.getCheck(), "displayed");
        Assertions.assertEquals(verifyDisplayedAction.getElement().getProperty(), "id");
        Assertions.assertEquals(verifyDisplayedAction.getElement().getPropertyValue(), "hello");

        VerifyAction verifyHiddenAction = (VerifyAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(verifyHiddenAction.getBrowser());
        Assertions.assertEquals(verifyHiddenAction.getName(), "selenium:verify");
        Assertions.assertEquals(verifyHiddenAction.getCheck(), "hidden");
        Assertions.assertEquals(verifyHiddenAction.getElement().getProperty(), "id");
        Assertions.assertEquals(verifyHiddenAction.getElement().getPropertyValue(), "dialog");

        VerifyAction verifyEnabledAction = (VerifyAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(verifyEnabledAction.getBrowser());
        Assertions.assertEquals(verifyEnabledAction.getName(), "selenium:verify");
        Assertions.assertEquals(verifyEnabledAction.getCheck(), "enabled");
        Assertions.assertEquals(verifyEnabledAction.getElement().getProperty(), "id");
        Assertions.assertEquals(verifyEnabledAction.getElement().getPropertyValue(), "hello");

        VerifyAction verifyTextAction = (VerifyAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(verifyTextAction.getBrowser());
        Assertions.assertEquals(verifyTextAction.getName(), "selenium:verify");
        Assertions.assertEquals(verifyTextAction.getCheck(), "text");
        Assertions.assertEquals(verifyTextAction.getExpected(), "@contains('Ok')@");
        Assertions.assertEquals(verifyTextAction.getElement().getProperty(), "id");
        Assertions.assertEquals(verifyTextAction.getElement().getPropertyValue(), "dialog");

        VerifyAction verifyAttributeAction = (VerifyAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(verifyAttributeAction.getBrowser());
        Assertions.assertEquals(verifyAttributeAction.getName(), "selenium:verify");
        Assertions.assertEquals(verifyAttributeAction.getCheck(), "attribute");
        Assertions.assertEquals(verifyAttributeAction.getProperty(), "href");
        Assertions.assertEquals(verifyAttributeAction.getExpected(), "https://citrusframework.org");
        Assertions.assertEquals(verifyAttributeAction.getElement().getProperty(), "id");
        Assertions.assertEquals(verifyAttributeAction.getElement().getPropertyValue(), "hello");

        VerifyAction verifyCountAction = (VerifyAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(verifyCountAction.getBrowser());
        Assertions.assertEquals(verifyCountAction.getName(), "selenium:verify");
        Assertions.assertEquals(verifyCountAction.getCheck(), "count");
        Assertions.assertEquals(verifyCountAction.getExpected(), "2");
        Assertions.assertEquals(verifyCountAction.getElement().getProperty(), "class-name");
        Assertions.assertEquals(verifyCountAction.getElement().getPropertyValue(), "order");

        VerifyAction verifyCssValueAction = (VerifyAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(verifyCssValueAction.getBrowser());
        Assertions.assertEquals(verifyCssValueAction.getName(), "selenium:verify");
        Assertions.assertEquals(verifyCssValueAction.getCheck(), "css-value");
        Assertions.assertEquals(verifyCssValueAction.getProperty(), "color");
        Assertions.assertEquals(verifyCssValueAction.getExpected(), "#000000");
        Assertions.assertEquals(verifyCssValueAction.getElement().getProperty(), "id");
        Assertions.assertEquals(verifyCssValueAction.getElement().getPropertyValue(), "hello");

        ExtractAction extractTitleAction = (ExtractAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(extractTitleAction.getBrowser());
        Assertions.assertEquals(extractTitleAction.getName(), "selenium:extract");
        Assertions.assertEquals(extractTitleAction.getValue(), "title");
        Assertions.assertEquals(extractTitleAction.getVariable(), "pageTitle");
        Assertions.assertNull(extractTitleAction.getElement());

        ExtractAction extractTextAction = (ExtractAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(extractTextAction.getBrowser());
        Assertions.assertEquals(extractTextAction.getName(), "selenium:extract");
        Assertions.assertEquals(extractTextAction.getValue(), "text");
        Assertions.assertEquals(extractTextAction.getVariable(), "dialogText");
        Assertions.assertEquals(extractTextAction.getElement().getProperty(), "id");
        Assertions.assertEquals(extractTextAction.getElement().getPropertyValue(), "dialog");

        ExtractAction extractCountAction = (ExtractAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(extractCountAction.getBrowser());
        Assertions.assertEquals(extractCountAction.getName(), "selenium:extract");
        Assertions.assertEquals(extractCountAction.getValue(), "count");
        Assertions.assertEquals(extractCountAction.getVariable(), "orderCount");
        Assertions.assertEquals(extractCountAction.getElement().getProperty(), "class-name");
        Assertions.assertEquals(extractCountAction.getElement().getPropertyValue(), "order");

        ExtractAction extractAttributeAction = (ExtractAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(extractAttributeAction.getBrowser());
        Assertions.assertEquals(extractAttributeAction.getName(), "selenium:extract");
        Assertions.assertEquals(extractAttributeAction.getValue(), "attribute");
        Assertions.assertEquals(extractAttributeAction.getProperty(), "href");
        Assertions.assertEquals(extractAttributeAction.getVariable(), "docsHref");
        Assertions.assertEquals(extractAttributeAction.getElement().getProperty(), "id");
        Assertions.assertEquals(extractAttributeAction.getElement().getPropertyValue(), "hello");

        ClickAction partialLinkTextClickAction = (ClickAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(partialLinkTextClickAction.getBrowser());
        Assertions.assertEquals(partialLinkTextClickAction.getName(), "selenium:click");
        Assertions.assertEquals(partialLinkTextClickAction.getProperty(), "partial-link-text");
        Assertions.assertEquals(partialLinkTextClickAction.getPropertyValue(), "Click");

        FrameAction frameNameAction = (FrameAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(frameNameAction.getBrowser());
        Assertions.assertEquals(frameNameAction.getName(), "selenium:frame");
        Assertions.assertEquals(frameNameAction.getNameOrId(), "menu-frame");

        FrameAction frameIndexAction = (FrameAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(frameIndexAction.getBrowser());
        Assertions.assertEquals(frameIndexAction.getName(), "selenium:frame");
        Assertions.assertEquals(frameIndexAction.getIndex(), Integer.valueOf(0));

        CookieAction cookieAddAction = (CookieAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(cookieAddAction.getBrowser());
        Assertions.assertEquals(cookieAddAction.getName(), "selenium:cookies");
        Assertions.assertEquals(cookieAddAction.getCommand(), "add");
        Assertions.assertEquals(cookieAddAction.getCookieName(), "session");
        Assertions.assertEquals(cookieAddAction.getValue(), "abc123");
        Assertions.assertEquals(cookieAddAction.getPath(), "/");
        Assertions.assertEquals(cookieAddAction.getDomain(), "citrusframework.org");

        CookieAction cookieVerifyAction = (CookieAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(cookieVerifyAction.getBrowser());
        Assertions.assertEquals(cookieVerifyAction.getName(), "selenium:cookies");
        Assertions.assertEquals(cookieVerifyAction.getCommand(), "verify");
        Assertions.assertEquals(cookieVerifyAction.getCookieName(), "session");
        Assertions.assertEquals(cookieVerifyAction.getExpected(), "abc123");

        StopBrowserAction stopAction = (StopBrowserAction) result.getTestAction(actionIndex);
        Assertions.assertNotNull(stopAction.getBrowser());
        Assertions.assertEquals(stopAction.getName(), "selenium:stop");
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Assertions.assertTrue(YamlTestActionBuilder.lookup().containsKey("selenium"));
        Assertions.assertTrue(YamlTestActionBuilder.lookup("selenium").isPresent());
        Assertions.assertEquals(YamlTestActionBuilder.lookup("selenium").get().getClass(), Selenium.class);
    }
}
