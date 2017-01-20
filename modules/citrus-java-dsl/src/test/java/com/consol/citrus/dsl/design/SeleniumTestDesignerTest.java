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

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.selenium.actions.*;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumTestDesignerTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = Mockito.mock(SeleniumBrowser.class);

    @Test
    public void testSeleniumBuilder() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                selenium().start(seleniumBrowser);

                selenium().navigate("http://localhost:9090");

                selenium().find().element(By.id("target"));
                selenium().find().element("class-name", "${cssClass}")
                            .tagName("button")
                            .enabled(false)
                            .displayed(false)
                            .text("Click Me!")
                            .style("color", "red")
                            .attribute("type", "submit");

                selenium().click().element(By.linkText("Click Me!"));
                selenium().hover().element(By.linkText("Hover Me!"));

                selenium().setInput("Citrus").element(By.name("username"));
                selenium().checkInput(false).element(By.xpath("//input[@type='checkbox']"));

                selenium().javascript("alert('Hello!')")
                            .errors("This went wrong!");

                selenium().alert().text("Hello!").accept();

                selenium().clearCache();

                selenium().store("classpath:download/file.txt");
                selenium().getStored("file.txt");

                selenium().open().window("my_window");
                selenium().focus().window("my_window");
                selenium().close().window("my_window");

                selenium().waitUntil().hidden().element(By.name("hiddenButton"));

                selenium().stop();
            }
        };

        builder.configure();

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
        Assert.assertNull(navigateAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), FindElementAction.class);
        FindElementAction findElementAction = (FindElementAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(findElementAction.getName(), "selenium:find");
        Assert.assertEquals(findElementAction.getBy(), By.id("target"));
        Assert.assertNull(findElementAction.getBrowser());

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
        Assert.assertNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), ClickAction.class);
        ClickAction clickAction = (ClickAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(clickAction.getName(), "selenium:click");
        Assert.assertEquals(clickAction.getBy(), By.linkText("Click Me!"));
        Assert.assertNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), HoverAction.class);
        HoverAction hoverAction = (HoverAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(hoverAction.getName(), "selenium:hover");
        Assert.assertEquals(hoverAction.getBy(), By.linkText("Hover Me!"));
        Assert.assertNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), SetInputAction.class);
        SetInputAction setInputAction = (SetInputAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(setInputAction.getName(), "selenium:set-input");
        Assert.assertEquals(setInputAction.getBy(), By.name("username"));
        Assert.assertEquals(setInputAction.getValue(), "Citrus");
        Assert.assertNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), CheckInputAction.class);
        CheckInputAction checkInputAction = (CheckInputAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(checkInputAction.getName(), "selenium:check-input");
        Assert.assertEquals(checkInputAction.getBy(), By.xpath("//input[@type='checkbox']"));
        Assert.assertFalse(checkInputAction.isChecked());
        Assert.assertNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), JavaScriptAction.class);
        JavaScriptAction javaScriptAction = (JavaScriptAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(javaScriptAction.getName(), "selenium:javascript");
        Assert.assertEquals(javaScriptAction.getScript(), "alert('Hello!')");
        Assert.assertEquals(javaScriptAction.getExpectedErrors().size(), 1L);
        Assert.assertEquals(javaScriptAction.getExpectedErrors().get(0), "This went wrong!");
        Assert.assertNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), AlertAction.class);
        AlertAction alertAction = (AlertAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(alertAction.getName(), "selenium:alert");
        Assert.assertEquals(alertAction.getText(), "Hello!");
        Assert.assertNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), ClearBrowserCacheAction.class);
        ClearBrowserCacheAction clearBrowserCacheAction = (ClearBrowserCacheAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(clearBrowserCacheAction.getName(), "selenium:clear-cache");
        Assert.assertNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), StoreFileAction.class);
        StoreFileAction storeFileAction = (StoreFileAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(storeFileAction.getName(), "selenium:store-file");
        Assert.assertEquals(storeFileAction.getFilePath(), "classpath:download/file.txt");
        Assert.assertNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), GetStoredFileAction.class);
        GetStoredFileAction getStoredFileAction = (GetStoredFileAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(getStoredFileAction.getName(), "selenium:get-stored-file");
        Assert.assertEquals(getStoredFileAction.getFileName(), "file.txt");
        Assert.assertNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), OpenWindowAction.class);
        OpenWindowAction openWindowAction = (OpenWindowAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(openWindowAction.getName(), "selenium:open-window");
        Assert.assertEquals(openWindowAction.getWindowName(), "my_window");
        Assert.assertNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), SwitchWindowAction.class);
        SwitchWindowAction switchWindowAction = (SwitchWindowAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(switchWindowAction.getName(), "selenium:switch-window");
        Assert.assertEquals(switchWindowAction.getWindowName(), "my_window");
        Assert.assertNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), CloseWindowAction.class);
        CloseWindowAction closeWindowAction = (CloseWindowAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(closeWindowAction.getName(), "selenium:close-window");
        Assert.assertEquals(closeWindowAction.getWindowName(), "my_window");
        Assert.assertNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), WaitUntilAction.class);
        WaitUntilAction waitUntilAction = (WaitUntilAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(waitUntilAction.getName(), "selenium:wait");
        Assert.assertEquals(waitUntilAction.getBy(), By.name("hiddenButton"));
        Assert.assertEquals(waitUntilAction.getCondition(), "hidden");
        Assert.assertNull(findElementAction.getBrowser());

        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(actionIndex)).getDelegate().getClass(), StopBrowserAction.class);
        StopBrowserAction stopBrowserAction = (StopBrowserAction) ((DelegatingTestAction)test.getActions().get(actionIndex++)).getDelegate();
        Assert.assertEquals(stopBrowserAction.getName(), "selenium:stop");
        Assert.assertNull(stopBrowserAction.getBrowser());
    }
}
