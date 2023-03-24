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

package org.citrusframework.selenium.config.xml;

import org.citrusframework.selenium.actions.*;
import org.citrusframework.selenium.pages.UserFormPage;
import org.citrusframework.testng.AbstractActionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumActionsParserTest extends AbstractActionParserTest<AbstractSeleniumAction> {

    @Test
    public void testActionParser() {
        assertActionCount(23);

        StartBrowserAction startAction = (StartBrowserAction) getNextTestActionFromTest();
        Assert.assertNotNull(startAction.getBrowser());
        Assert.assertEquals(startAction.getName(), "selenium:start");

        AlertAction alertAction = (AlertAction) getNextTestActionFromTest();
        Assert.assertNull(alertAction.getBrowser());
        Assert.assertEquals(alertAction.getName(), "selenium:alert");
        Assert.assertNull(alertAction.getText());
        Assert.assertTrue(alertAction.isAccept());

        alertAction = (AlertAction) getNextTestActionFromTest();
        Assert.assertNull(alertAction.getBrowser());
        Assert.assertEquals(alertAction.getName(), "selenium:alert");
        Assert.assertEquals(alertAction.getText(), "This is a warning message!");
        Assert.assertFalse(alertAction.isAccept());

        FindElementAction findElementAction = (FindElementAction) getNextTestActionFromTest();
        Assert.assertNull(findElementAction.getBrowser());
        Assert.assertEquals(findElementAction.getName(), "selenium:find");
        Assert.assertEquals(findElementAction.getProperty(), "class-name");
        Assert.assertEquals(findElementAction.getPropertyValue(), "clickable");
        Assert.assertEquals(findElementAction.getTagName(), "button");
        Assert.assertEquals(findElementAction.getText(), "Ok");
        Assert.assertEquals(findElementAction.getAttributes().size(), 1L);
        Assert.assertEquals(findElementAction.getAttributes().get("type"), "submit");
        Assert.assertEquals(findElementAction.getStyles().size(), 1L);
        Assert.assertEquals(findElementAction.getStyles().get("color"), "#000000");
        Assert.assertTrue(findElementAction.isDisplayed());
        Assert.assertFalse(findElementAction.isEnabled());

        PageAction pageAction = (PageAction) getNextTestActionFromTest();
        Assert.assertNull(pageAction.getBrowser());
        Assert.assertEquals(pageAction.getName(), "selenium:page");
        Assert.assertEquals(pageAction.getAction(), "setUserName");
        Assert.assertEquals(pageAction.getPage(), beanDefinitionContext.getBean("userForm"));
        Assert.assertNull(pageAction.getType());
        Assert.assertEquals(pageAction.getArguments().size(), 1L);
        Assert.assertEquals(pageAction.getArguments().get(0), "${username}");
        Assert.assertNull(pageAction.getValidator());

        pageAction = (PageAction) getNextTestActionFromTest();
        Assert.assertNull(pageAction.getBrowser());
        Assert.assertEquals(pageAction.getName(), "selenium:page");
        Assert.assertEquals(pageAction.getAction(), "validate");
        Assert.assertNull(pageAction.getPage());
        Assert.assertEquals(pageAction.getType(), UserFormPage.class.getName());
        Assert.assertEquals(pageAction.getArguments().size(), 0L);
        Assert.assertEquals(pageAction.getValidator(), beanDefinitionContext.getBean("pageValidator"));

        ClickAction clickAction = (ClickAction) getNextTestActionFromTest();
        Assert.assertNull(clickAction.getBrowser());
        Assert.assertEquals(clickAction.getName(), "selenium:click");
        Assert.assertEquals(clickAction.getProperty(), "id");
        Assert.assertEquals(clickAction.getPropertyValue(), "edit-link");

        HoverAction hoverAction = (HoverAction) getNextTestActionFromTest();
        Assert.assertNull(hoverAction.getBrowser());
        Assert.assertEquals(hoverAction.getName(), "selenium:hover");
        Assert.assertEquals(hoverAction.getProperty(), "id");
        Assert.assertEquals(hoverAction.getPropertyValue(), "edit-link");

        SetInputAction setInputAction = (SetInputAction) getNextTestActionFromTest();
        Assert.assertNull(setInputAction.getBrowser());
        Assert.assertEquals(setInputAction.getName(), "selenium:set-input");
        Assert.assertEquals(setInputAction.getProperty(), "tag-name");
        Assert.assertEquals(setInputAction.getPropertyValue(), "input");
        Assert.assertEquals(setInputAction.getValue(), "new-value");

        CheckInputAction checkInputAction = (CheckInputAction) getNextTestActionFromTest();
        Assert.assertNull(checkInputAction.getBrowser());
        Assert.assertEquals(checkInputAction.getName(), "selenium:check-input");
        Assert.assertEquals(checkInputAction.getProperty(), "xpath");
        Assert.assertEquals(checkInputAction.getPropertyValue(), "//input[@type='checkbox']");
        Assert.assertTrue(checkInputAction.isChecked());

        DropDownSelectAction dropDownSelect = (DropDownSelectAction) getNextTestActionFromTest();
        Assert.assertNull(dropDownSelect.getBrowser());
        Assert.assertEquals(dropDownSelect.getName(), "selenium:dropdown-select");
        Assert.assertEquals(dropDownSelect.getProperty(), "name");
        Assert.assertEquals(dropDownSelect.getPropertyValue(), "gender");
        Assert.assertEquals(dropDownSelect.getOption(), "male");
        Assert.assertEquals(dropDownSelect.getOptions().size(), 0L);

        DropDownSelectAction dropDownMultiSelect = (DropDownSelectAction) getNextTestActionFromTest();
        Assert.assertNull(dropDownMultiSelect.getBrowser());
        Assert.assertEquals(dropDownMultiSelect.getName(), "selenium:dropdown-select");
        Assert.assertEquals(dropDownMultiSelect.getProperty(), "id");
        Assert.assertEquals(dropDownMultiSelect.getPropertyValue(), "title");
        Assert.assertNull(dropDownMultiSelect.getOption());
        Assert.assertEquals(dropDownMultiSelect.getOptions().size(), 2L);

        WaitUntilAction waitUntilAction = (WaitUntilAction) getNextTestActionFromTest();
        Assert.assertNull(waitUntilAction.getBrowser());
        Assert.assertEquals(waitUntilAction.getName(), "selenium:wait");
        Assert.assertEquals(waitUntilAction.getProperty(), "id");
        Assert.assertEquals(waitUntilAction.getPropertyValue(), "dialog");
        Assert.assertEquals(waitUntilAction.getCondition(), "hidden");

        JavaScriptAction javaScriptAction = (JavaScriptAction) getNextTestActionFromTest();
        Assert.assertNull(javaScriptAction.getBrowser());
        Assert.assertEquals(javaScriptAction.getName(), "selenium:javascript");
        Assert.assertEquals(javaScriptAction.getScript(), "alert('This is awesome!')");
        Assert.assertEquals(javaScriptAction.getExpectedErrors().size(), 1L);
        Assert.assertEquals(javaScriptAction.getExpectedErrors().get(0), "Something went wrong");

        MakeScreenshotAction screenshotAction = (MakeScreenshotAction) getNextTestActionFromTest();
        Assert.assertNotNull(screenshotAction.getBrowser());
        Assert.assertEquals(screenshotAction.getName(), "selenium:screenshot");
        Assert.assertEquals(screenshotAction.getOutputDir(), "/tmp/storage");

        NavigateAction navigateAction = (NavigateAction) getNextTestActionFromTest();
        Assert.assertNull(navigateAction.getBrowser());
        Assert.assertEquals(navigateAction.getName(), "selenium:navigate");
        Assert.assertEquals(navigateAction.getPage(), "back");

        OpenWindowAction openWindowAction = (OpenWindowAction) getNextTestActionFromTest();
        Assert.assertNull(openWindowAction.getBrowser());
        Assert.assertEquals(openWindowAction.getName(), "selenium:open-window");
        Assert.assertEquals(openWindowAction.getWindowName(), "newWindow");

        SwitchWindowAction switchWindowAction = (SwitchWindowAction) getNextTestActionFromTest();
        Assert.assertNull(switchWindowAction.getBrowser());
        Assert.assertEquals(switchWindowAction.getName(), "selenium:switch-window");
        Assert.assertEquals(switchWindowAction.getWindowName(), "switchWindow");

        CloseWindowAction closeWindowAction = (CloseWindowAction) getNextTestActionFromTest();
        Assert.assertNull(closeWindowAction.getBrowser());
        Assert.assertEquals(closeWindowAction.getName(), "selenium:close-window");
        Assert.assertEquals(closeWindowAction.getWindowName(), "closeWindow");

        StoreFileAction storeFileAction = (StoreFileAction) getNextTestActionFromTest();
        Assert.assertNull(storeFileAction.getBrowser());
        Assert.assertEquals(storeFileAction.getName(), "selenium:store-file");
        Assert.assertEquals(storeFileAction.getFilePath(), "classpath:download/file.txt");

        GetStoredFileAction getStoredFileAction = (GetStoredFileAction) getNextTestActionFromTest();
        Assert.assertNull(getStoredFileAction.getBrowser());
        Assert.assertEquals(getStoredFileAction.getName(), "selenium:get-stored-file");
        Assert.assertEquals(getStoredFileAction.getFileName(), "file.txt");

        ClearBrowserCacheAction clearCacheAction = (ClearBrowserCacheAction) getNextTestActionFromTest();
        Assert.assertNull(clearCacheAction.getBrowser());
        Assert.assertEquals(clearCacheAction.getName(), "selenium:clear-cache");

        StopBrowserAction stopAction = (StopBrowserAction) getNextTestActionFromTest();
        Assert.assertNotNull(stopAction.getBrowser());
        Assert.assertEquals(stopAction.getName(), "selenium:stop");
    }

}
