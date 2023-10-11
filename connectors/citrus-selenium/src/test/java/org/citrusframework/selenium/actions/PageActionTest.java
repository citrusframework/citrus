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

package org.citrusframework.selenium.actions;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.model.PageValidator;
import org.citrusframework.selenium.model.WebPage;
import org.citrusframework.selenium.pages.UserFormPage;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class PageActionTest extends AbstractTestNGUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final WebDriver webDriver = Mockito.mock(WebDriver.class);
    private final WebElement formElement = Mockito.mock(WebElement.class);
    private final WebElement inputElement = Mockito.mock(WebElement.class);

    @BeforeMethod
    public void setup() {
        reset(webDriver, formElement, inputElement);

        seleniumBrowser.setWebDriver(webDriver);

        when(formElement.getTagName()).thenReturn("form");
        when(formElement.isEnabled()).thenReturn(true);
        when(formElement.isDisplayed()).thenReturn(true);
        when(inputElement.getTagName()).thenReturn("input");
        when(inputElement.isEnabled()).thenReturn(true);
        when(inputElement.isDisplayed()).thenReturn(true);

        when(webDriver.findElement(By.id("userForm"))).thenReturn(formElement);
        when(webDriver.findElement(By.id("username"))).thenReturn(inputElement);
    }

    @Test
    public void testExecutePageValidation() throws Exception {
        when(inputElement.getAttribute("value")).thenReturn("TestUser");

        PageAction action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("validate")
                .page(new UserFormPage())
                .build();
        action.execute(context);
    }

    @Test
    public void testExecutePageType() throws Exception {
        when(inputElement.getAttribute("value")).thenReturn("TestUser");

        PageAction action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("validate")
                .type(UserFormPage.class.getName())
                .build();
        action.execute(context);
    }

    @Test
    public void testExecutePageValidator() throws Exception {
        PageValidator validator = Mockito.mock(PageValidator.class);

        when(inputElement.getAttribute("value")).thenReturn("TestUser");

        UserFormPage userForm = new UserFormPage();

        PageAction action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("validate")
                .page(userForm)
                .validator(validator)
                .build();
        action.execute(context);

        verify(validator).validate(userForm, seleniumBrowser, context);
    }

    @Test
    public void testExecuteAction() throws Exception {
        PageAction action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("setUserName")
                .argument("Citrus")
                .page(new UserFormPage())
                .build();
        action.execute(context);

        verify(inputElement).clear();
        verify(inputElement).sendKeys("Citrus");
    }

    @Test
    public void testExecuteActionWithArguments() throws Exception {
        when(webDriver.findElement(By.id("form"))).thenReturn(formElement);

        PageAction action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("submit")
                .page(new TestPage())
                .build();
        action.execute(context);

        action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("submitWithContext")
                .page(new TestPage())
                .build();
        action.execute(context);

        action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("submitWithArgument")
                .argument("ok")
                .page(new TestPage())
                .build();
        action.execute(context);

        action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("submitWithArgumentAndContext")
                .argument("ok")
                .page(new TestPage())
                .build();
        action.execute(context);

        verify(formElement, times(4)).submit();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Unsupported method signature for page action.*")
    public void testExecuteActionNotMatchingArguments() throws Exception {
        when(webDriver.findElement(By.id("form"))).thenReturn(formElement);

        PageAction action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("submit")
                .page(new TestPage())
                .argument("Citrus")
                .build();
        action.execute(context);

        verify(inputElement).clear();
        verify(inputElement).sendKeys("Citrus");

        verify(formElement).submit();
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Page validation failed!")
    public void testExecuteValidationFailed() throws Exception {
        PageAction action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("validate")
                .page(new UserFormPage())
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to access page type.*")
    public void testInvalidPageType() throws Exception {

        PageAction action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("validate")
                .type(UserFormPage.class.getPackage().getName() + ".UnknownPage")
                .build();
        action.execute(context);
    }

    public class TestPage implements WebPage {
        @FindBy(id = "form")
        private WebElement form;

        public void submit() {
            form.submit();
        }

        public void submitWithContext(TestContext context) {
            Assert.assertNotNull(context);
            form.submit();
        }

        public void submitWithArgument(String arg) {
            Assert.assertNotNull(arg);
            form.submit();
        }

        public void submitWithArgumentAndContext(String arg, TestContext context) {
            Assert.assertNotNull(arg);
            form.submit();
        }
    }

}
