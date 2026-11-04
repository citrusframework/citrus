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

package org.citrusframework.selenium.config.xml;

import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.citrusframework.selenium.actions.*;
import org.citrusframework.selenium.pages.UserFormPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * @since 2.7
 */
public class SeleniumActionsParserTest extends AbstractActionParserTest<AbstractSeleniumAction> {

    @Test
    public void testActionParser() {
        assertActionCount(24);

        StartBrowserAction startAction = (StartBrowserAction) getNextTestActionFromTest();
        Assertions.assertNotNull(startAction.getBrowser());
        Assertions.assertEquals(startAction.getName(), "selenium:start");

        AlertAction alertAction = (AlertAction) getNextTestActionFromTest();
        Assertions.assertNull(alertAction.getBrowser());
        Assertions.assertEquals(alertAction.getName(), "selenium:alert");
        Assertions.assertNull(alertAction.getText());
        Assertions.assertTrue(alertAction.isAccept());

        alertAction = (AlertAction) getNextTestActionFromTest();
        Assertions.assertNull(alertAction.getBrowser());
        Assertions.assertEquals(alertAction.getName(), "selenium:alert");
        Assertions.assertEquals(alertAction.getText(), "This is a warning message!");
        Assertions.assertFalse(alertAction.isAccept());

        FindElementAction findElementAction = (FindElementAction) getNextTestActionFromTest();
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
        Assertions.assertFalse(findElementAction.isEnabled());

        findElementAction = (FindElementAction) getNextTestActionFromTest();
        Assertions.assertNull(findElementAction.getBrowser());
        Assertions.assertEquals(findElementAction.getName(), "selenium:find");
        Assertions.assertEquals(findElementAction.getProperty(), "tag-name");
        Assertions.assertEquals(findElementAction.getPropertyValue(), "span");
        Assertions.assertNull(findElementAction.getTagName());

        PageAction pageAction = (PageAction) getNextTestActionFromTest();
        Assertions.assertNull(pageAction.getBrowser());
        Assertions.assertEquals(pageAction.getName(), "selenium:page");
        Assertions.assertEquals(pageAction.getAction(), "setUserName");
        Assertions.assertEquals(pageAction.getPage(), beanDefinitionContext.getBean("userForm"));
        Assertions.assertNull(pageAction.getType());
        Assertions.assertEquals(pageAction.getArguments().size(), 1L);
        Assertions.assertEquals(pageAction.getArguments().get(0), "${username}");
        Assertions.assertNull(pageAction.getValidator());

        pageAction = (PageAction) getNextTestActionFromTest();
        Assertions.assertNull(pageAction.getBrowser());
        Assertions.assertEquals(pageAction.getName(), "selenium:page");
        Assertions.assertEquals(pageAction.getAction(), "validate");
        Assertions.assertNull(pageAction.getPage());
        Assertions.assertEquals(pageAction.getType(), UserFormPage.class.getName());
        Assertions.assertEquals(pageAction.getArguments().size(), 0L);
        Assertions.assertEquals(pageAction.getValidator(), beanDefinitionContext.getBean("pageValidator"));

        ClickAction clickAction = (ClickAction) getNextTestActionFromTest();
        Assertions.assertNull(clickAction.getBrowser());
        Assertions.assertEquals(clickAction.getName(), "selenium:click");
        Assertions.assertEquals(clickAction.getProperty(), "id");
        Assertions.assertEquals(clickAction.getPropertyValue(), "edit-link");

        HoverAction hoverAction = (HoverAction) getNextTestActionFromTest();
        Assertions.assertNull(hoverAction.getBrowser());
        Assertions.assertEquals(hoverAction.getName(), "selenium:hover");
        Assertions.assertEquals(hoverAction.getProperty(), "id");
        Assertions.assertEquals(hoverAction.getPropertyValue(), "edit-link");

        SetInputAction setInputAction = (SetInputAction) getNextTestActionFromTest();
        Assertions.assertNull(setInputAction.getBrowser());
        Assertions.assertEquals(setInputAction.getName(), "selenium:set-input");
        Assertions.assertEquals(setInputAction.getProperty(), "tag-name");
        Assertions.assertEquals(setInputAction.getPropertyValue(), "input");
        Assertions.assertEquals(setInputAction.getValue(), "new-value");

        CheckInputAction checkInputAction = (CheckInputAction) getNextTestActionFromTest();
        Assertions.assertNull(checkInputAction.getBrowser());
        Assertions.assertEquals(checkInputAction.getName(), "selenium:check-input");
        Assertions.assertEquals(checkInputAction.getProperty(), "xpath");
        Assertions.assertEquals(checkInputAction.getPropertyValue(), "//input[@type='checkbox']");
        Assertions.assertTrue(checkInputAction.isChecked());

        DropDownSelectAction dropDownSelect = (DropDownSelectAction) getNextTestActionFromTest();
        Assertions.assertNull(dropDownSelect.getBrowser());
        Assertions.assertEquals(dropDownSelect.getName(), "selenium:dropdown-select");
        Assertions.assertEquals(dropDownSelect.getProperty(), "name");
        Assertions.assertEquals(dropDownSelect.getPropertyValue(), "gender");
        Assertions.assertEquals(dropDownSelect.getOption(), "male");
        Assertions.assertEquals(dropDownSelect.getOptions().size(), 0L);

        DropDownSelectAction dropDownMultiSelect = (DropDownSelectAction) getNextTestActionFromTest();
        Assertions.assertNull(dropDownMultiSelect.getBrowser());
        Assertions.assertEquals(dropDownMultiSelect.getName(), "selenium:dropdown-select");
        Assertions.assertEquals(dropDownMultiSelect.getProperty(), "id");
        Assertions.assertEquals(dropDownMultiSelect.getPropertyValue(), "title");
        Assertions.assertNull(dropDownMultiSelect.getOption());
        Assertions.assertEquals(dropDownMultiSelect.getOptions().size(), 2L);

        WaitUntilAction waitUntilAction = (WaitUntilAction) getNextTestActionFromTest();
        Assertions.assertNull(waitUntilAction.getBrowser());
        Assertions.assertEquals(waitUntilAction.getName(), "selenium:wait");
        Assertions.assertEquals(waitUntilAction.getProperty(), "id");
        Assertions.assertEquals(waitUntilAction.getPropertyValue(), "dialog");
        Assertions.assertEquals(waitUntilAction.getCondition(), "hidden");

        JavaScriptAction javaScriptAction = (JavaScriptAction) getNextTestActionFromTest();
        Assertions.assertNull(javaScriptAction.getBrowser());
        Assertions.assertEquals(javaScriptAction.getName(), "selenium:javascript");
        Assertions.assertEquals(javaScriptAction.getScript(), "alert('This is awesome!')");
        Assertions.assertEquals(javaScriptAction.getExpectedErrors().size(), 1L);
        Assertions.assertEquals(javaScriptAction.getExpectedErrors().get(0), "Something went wrong");

        MakeScreenshotAction screenshotAction = (MakeScreenshotAction) getNextTestActionFromTest();
        Assertions.assertNotNull(screenshotAction.getBrowser());
        Assertions.assertEquals(screenshotAction.getName(), "selenium:screenshot");
        Assertions.assertEquals(screenshotAction.getOutputDir(), "/tmp/storage");

        NavigateAction navigateAction = (NavigateAction) getNextTestActionFromTest();
        Assertions.assertNull(navigateAction.getBrowser());
        Assertions.assertEquals(navigateAction.getName(), "selenium:navigate");
        Assertions.assertEquals(navigateAction.getPage(), "back");

        OpenWindowAction openWindowAction = (OpenWindowAction) getNextTestActionFromTest();
        Assertions.assertNull(openWindowAction.getBrowser());
        Assertions.assertEquals(openWindowAction.getName(), "selenium:open-window");
        Assertions.assertEquals(openWindowAction.getWindowName(), "newWindow");

        SwitchWindowAction switchWindowAction = (SwitchWindowAction) getNextTestActionFromTest();
        Assertions.assertNull(switchWindowAction.getBrowser());
        Assertions.assertEquals(switchWindowAction.getName(), "selenium:switch-window");
        Assertions.assertEquals(switchWindowAction.getWindowName(), "switchWindow");

        CloseWindowAction closeWindowAction = (CloseWindowAction) getNextTestActionFromTest();
        Assertions.assertNull(closeWindowAction.getBrowser());
        Assertions.assertEquals(closeWindowAction.getName(), "selenium:close-window");
        Assertions.assertEquals(closeWindowAction.getWindowName(), "closeWindow");

        StoreFileAction storeFileAction = (StoreFileAction) getNextTestActionFromTest();
        Assertions.assertNull(storeFileAction.getBrowser());
        Assertions.assertEquals(storeFileAction.getName(), "selenium:store-file");
        Assertions.assertEquals(storeFileAction.getFilePath(), "classpath:download/file.txt");

        GetStoredFileAction getStoredFileAction = (GetStoredFileAction) getNextTestActionFromTest();
        Assertions.assertNull(getStoredFileAction.getBrowser());
        Assertions.assertEquals(getStoredFileAction.getName(), "selenium:get-stored-file");
        Assertions.assertEquals(getStoredFileAction.getFileName(), "file.txt");

        ClearBrowserCacheAction clearCacheAction = (ClearBrowserCacheAction) getNextTestActionFromTest();
        Assertions.assertNull(clearCacheAction.getBrowser());
        Assertions.assertEquals(clearCacheAction.getName(), "selenium:clear-cache");

        StopBrowserAction stopAction = (StopBrowserAction) getNextTestActionFromTest();
        Assertions.assertNotNull(stopAction.getBrowser());
        Assertions.assertEquals(stopAction.getName(), "selenium:stop");
    }
}
