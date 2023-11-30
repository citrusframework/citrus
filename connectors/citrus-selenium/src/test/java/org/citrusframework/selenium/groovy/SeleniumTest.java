/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.selenium.groovy;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.selenium.actions.*;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumBrowserConfiguration;
import org.citrusframework.selenium.model.PageValidator;
import org.citrusframework.selenium.model.WebPage;
import org.citrusframework.selenium.pages.UserFormPage;
import org.mockito.Mock;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
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
    private PageValidator<?> pageValidator;

    @Mock
    private WebDriver.Navigation navigation;

    @Mock
    private WebDriver.Options options;

    @BeforeClass
    @Override
    public void setupMocks() {
        super.setupMocks();

        SeleniumBrowserConfiguration configuration = new SeleniumBrowserConfiguration();
        when(browser.getEndpointConfiguration()).thenReturn(configuration);
        when(browser.getName()).thenReturn("seleniumBrowser");
        when(browser.getWebDriver()).thenReturn(webDriver);
        when(webDriver.switchTo()).thenReturn(targetLocator);
        when(targetLocator.alert()).thenReturn(alert);
        when(alert.getText()).thenReturn("This is a warning message!");
        when(webDriver.findElement(any(By.class))).thenReturn(webElement);
        when(webElement.getText()).thenReturn("Ok");
        when(webElement.isDisplayed()).thenReturn(true, true, true, true, true, true, true, true, true, true, true, true, true)
                .thenReturn(false);
        when(webElement.isEnabled()).thenReturn(true);
        when(webElement.getAttribute("type")).thenReturn("submit");
        when(webElement.getAttribute("value")).thenReturn("Foo");
        when(webElement.getAttribute("enabled")).thenReturn("true");
        when(webElement.getCssValue("color")).thenReturn("#000000");
        when(webElement.getTagName()).thenReturn("button", "select");
        when(webElement.findElements(any(By.class))).thenReturn(List.of(option));
        when(option.isEnabled()).thenReturn(true);
        when(option.getText()).thenReturn("new-value");
        when(webDriver.executeScript(any(String.class), any(Object[].class))).thenReturn(Collections.emptyList()).thenReturn(List.of("Something went wrong"));
        when(webDriver.navigate()).thenReturn(navigation);
        when(webDriver.getWindowHandles()).thenReturn(Set.of("switchWindow", "closeWindow")).thenReturn(Set.of("newWindow", "switchWindow", "closeWindow"));
        when(webDriver.getWindowHandle()).thenReturn("newWindow", "switchWindow", "closeWindow");
        when(targetLocator.window(any(String.class))).thenReturn(webDriver);
        when(browser.getStoredFile(any(String.class))).thenReturn("file.txt");
        when(webDriver.manage()).thenReturn(options);
    }

    @Test
    public void shouldLoadSeleniumActions() throws IOException {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/selenium/groovy/selenium.test.groovy");

        context.getReferenceResolver().bind("seleniumBrowser", browser);
        context.getReferenceResolver().bind("userForm", webPage);
        context.getReferenceResolver().bind("pageValidator", pageValidator);

        context.setVariable("switchWindow", "switchWindow");
        context.setVariable("closeWindow", "closeWindow");

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "SeleniumTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 25L);

        int actionIndex = 0;

        StartBrowserAction startAction = (StartBrowserAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(startAction.getBrowser());
        Assert.assertEquals(startAction.getName(), "selenium:start");

        AlertAction alertAction = (AlertAction) result.getTestAction(actionIndex++);
        Assert.assertNull(alertAction.getBrowser());
        Assert.assertEquals(alertAction.getName(), "selenium:alert");
        Assert.assertNull(alertAction.getText());
        Assert.assertTrue(alertAction.isAccept());

        alertAction = (AlertAction) result.getTestAction(actionIndex++);
        Assert.assertNull(alertAction.getBrowser());
        Assert.assertEquals(alertAction.getName(), "selenium:alert");
        Assert.assertEquals(alertAction.getText(), "This is a warning message!");
        Assert.assertFalse(alertAction.isAccept());

        FindElementAction findElementAction = (FindElementAction) result.getTestAction(actionIndex++);
        Assert.assertNull(findElementAction.getBrowser());
        Assert.assertEquals(findElementAction.getName(), "selenium:find");
        Assert.assertEquals(findElementAction.getBy().toString(), "By.className: clickable");
        Assert.assertEquals(findElementAction.getTagName(), "button");
        Assert.assertEquals(findElementAction.getText(), "Ok");
        Assert.assertEquals(findElementAction.getAttributes().size(), 1L);
        Assert.assertEquals(findElementAction.getAttributes().get("type"), "submit");
        Assert.assertEquals(findElementAction.getStyles().size(), 1L);
        Assert.assertEquals(findElementAction.getStyles().get("color"), "#000000");
        Assert.assertTrue(findElementAction.isDisplayed());
        Assert.assertTrue(findElementAction.isEnabled());

        PageAction pageAction = (PageAction) result.getTestAction(actionIndex++);
        Assert.assertNull(pageAction.getBrowser());
        Assert.assertEquals(pageAction.getName(), "selenium:page");
        Assert.assertEquals(pageAction.getAction(), "setUserName");
        Assert.assertEquals(pageAction.getPage(), context.getReferenceResolver().resolve("userForm"));
        Assert.assertNull(pageAction.getType());
        Assert.assertEquals(pageAction.getArguments().size(), 1L);
        Assert.assertEquals(pageAction.getArguments().get(0), "${username}");
        Assert.assertNull(pageAction.getValidator());

        pageAction = (PageAction) result.getTestAction(actionIndex++);
        Assert.assertNull(pageAction.getBrowser());
        Assert.assertEquals(pageAction.getName(), "selenium:page");
        Assert.assertEquals(pageAction.getAction(), "validate");
        Assert.assertNull(pageAction.getPage());
        Assert.assertEquals(pageAction.getType(), UserFormPage.class.getName());
        Assert.assertEquals(pageAction.getArguments().size(), 0L);
        Assert.assertEquals(pageAction.getValidator(), context.getReferenceResolver().resolve("pageValidator"));

        ClickAction clickAction = (ClickAction) result.getTestAction(actionIndex++);
        Assert.assertNull(clickAction.getBrowser());
        Assert.assertEquals(clickAction.getName(), "selenium:click");
        Assert.assertEquals(clickAction.getBy().toString(), "By.id: edit-link");

        HoverAction hoverAction = (HoverAction) result.getTestAction(actionIndex++);
        Assert.assertNull(hoverAction.getBrowser());
        Assert.assertEquals(hoverAction.getName(), "selenium:hover");
        Assert.assertEquals(hoverAction.getBy().toString(), "By.id: edit-link");

        SetInputAction setInputAction = (SetInputAction) result.getTestAction(actionIndex++);
        Assert.assertNull(setInputAction.getBrowser());
        Assert.assertEquals(setInputAction.getName(), "selenium:set-input");
        Assert.assertEquals(setInputAction.getBy().toString(), "By.tagName: input");
        Assert.assertEquals(setInputAction.getValue(), "new-value");

        CheckInputAction checkInputAction = (CheckInputAction) result.getTestAction(actionIndex++);
        Assert.assertNull(checkInputAction.getBrowser());
        Assert.assertEquals(checkInputAction.getName(), "selenium:check-input");
        Assert.assertEquals(checkInputAction.getBy().toString(), "By.xpath: //input[@type: 'checkbox']");
        Assert.assertTrue(checkInputAction.isChecked());

        DropDownSelectAction dropDownSelect = (DropDownSelectAction) result.getTestAction(actionIndex++);
        Assert.assertNull(dropDownSelect.getBrowser());
        Assert.assertEquals(dropDownSelect.getName(), "selenium:dropdown-select");
        Assert.assertEquals(dropDownSelect.getBy().toString(), "By.name: gender");
        Assert.assertEquals(dropDownSelect.getOption(), "male");
        Assert.assertEquals(dropDownSelect.getOptions().size(), 0L);

        DropDownSelectAction dropDownMultiSelect = (DropDownSelectAction) result.getTestAction(actionIndex++);
        Assert.assertNull(dropDownMultiSelect.getBrowser());
        Assert.assertEquals(dropDownMultiSelect.getName(), "selenium:dropdown-select");
        Assert.assertEquals(dropDownMultiSelect.getBy().toString(), "By.id: title");
        Assert.assertNull(dropDownMultiSelect.getOption());
        Assert.assertEquals(dropDownMultiSelect.getOptions().size(), 2L);

        FillFormAction fillForm = (FillFormAction) result.getTestAction(actionIndex++);
        Assert.assertNull(fillForm.getBrowser());
        Assert.assertEquals(fillForm.getName(), "selenium:fill-form");
        Assert.assertNull(fillForm.getSubmitButton());
        Assert.assertEquals(fillForm.getFormFields().size(), 2L);
        Assert.assertEquals(fillForm.getFormFields().get(By.id("username")), "foo_user");
        Assert.assertEquals(fillForm.getFormFields().get(By.id("password")), "secret");

        fillForm = (FillFormAction) result.getTestAction(actionIndex++);
        Assert.assertNull(fillForm.getBrowser());
        Assert.assertEquals(fillForm.getName(), "selenium:fill-form");
        Assert.assertNotNull(fillForm.getSubmitButton());
        Assert.assertEquals(fillForm.getSubmitButton(), By.id("save"));
        Assert.assertEquals(fillForm.getFormFields().size(), 2L);
        Assert.assertEquals(fillForm.getFormFields().get(By.id("username")), "foo_user");
        Assert.assertEquals(fillForm.getFormFields().get(By.id("password")), "secret");

        WaitUntilAction waitUntilAction = (WaitUntilAction) result.getTestAction(actionIndex++);
        Assert.assertNull(waitUntilAction.getBrowser());
        Assert.assertEquals(waitUntilAction.getName(), "selenium:wait");
        Assert.assertEquals(waitUntilAction.getBy().toString(), "By.id: dialog");
        Assert.assertEquals(waitUntilAction.getCondition(), "hidden");

        JavaScriptAction javaScriptAction = (JavaScriptAction) result.getTestAction(actionIndex++);
        Assert.assertNull(javaScriptAction.getBrowser());
        Assert.assertEquals(javaScriptAction.getName(), "selenium:javascript");
        Assert.assertEquals(javaScriptAction.getScript(), "alert('This is awesome!')");
        Assert.assertEquals(javaScriptAction.getExpectedErrors().size(), 1L);
        Assert.assertEquals(javaScriptAction.getExpectedErrors().get(0), "Something went wrong");

        MakeScreenshotAction screenshotAction = (MakeScreenshotAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(screenshotAction.getBrowser());
        Assert.assertEquals(screenshotAction.getName(), "selenium:screenshot");
        Assert.assertEquals(screenshotAction.getOutputDir(), "/tmp/storage");

        NavigateAction navigateAction = (NavigateAction) result.getTestAction(actionIndex++);
        Assert.assertNull(navigateAction.getBrowser());
        Assert.assertEquals(navigateAction.getName(), "selenium:navigate");
        Assert.assertEquals(navigateAction.getPage(), "back");

        OpenWindowAction openWindowAction = (OpenWindowAction) result.getTestAction(actionIndex++);
        Assert.assertNull(openWindowAction.getBrowser());
        Assert.assertEquals(openWindowAction.getName(), "selenium:open-window");
        Assert.assertEquals(openWindowAction.getWindowName(), "newWindow");

        SwitchWindowAction switchWindowAction = (SwitchWindowAction) result.getTestAction(actionIndex++);
        Assert.assertNull(switchWindowAction.getBrowser());
        Assert.assertEquals(switchWindowAction.getName(), "selenium:switch-window");
        Assert.assertEquals(switchWindowAction.getWindowName(), "switchWindow");

        CloseWindowAction closeWindowAction = (CloseWindowAction) result.getTestAction(actionIndex++);
        Assert.assertNull(closeWindowAction.getBrowser());
        Assert.assertEquals(closeWindowAction.getName(), "selenium:close-window");
        Assert.assertEquals(closeWindowAction.getWindowName(), "closeWindow");

        StoreFileAction storeFileAction = (StoreFileAction) result.getTestAction(actionIndex++);
        Assert.assertNull(storeFileAction.getBrowser());
        Assert.assertEquals(storeFileAction.getName(), "selenium:store-file");
        Assert.assertEquals(storeFileAction.getFilePath(), "classpath:download/file.txt");

        GetStoredFileAction getStoredFileAction = (GetStoredFileAction) result.getTestAction(actionIndex++);
        Assert.assertNull(getStoredFileAction.getBrowser());
        Assert.assertEquals(getStoredFileAction.getName(), "selenium:get-stored-file");
        Assert.assertEquals(getStoredFileAction.getFileName(), "file.txt");

        ClearBrowserCacheAction clearCacheAction = (ClearBrowserCacheAction) result.getTestAction(actionIndex++);
        Assert.assertNull(clearCacheAction.getBrowser());
        Assert.assertEquals(clearCacheAction.getName(), "selenium:clear-cache");

        StopBrowserAction stopAction = (StopBrowserAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(stopAction.getBrowser());
        Assert.assertEquals(stopAction.getName(), "selenium:stop");
    }

}
