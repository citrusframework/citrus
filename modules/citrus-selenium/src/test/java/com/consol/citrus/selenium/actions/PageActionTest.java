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

package com.consol.citrus.selenium.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.model.PageValidator;
import com.consol.citrus.selenium.model.WebPage;
import com.consol.citrus.selenium.pages.UserFormPage;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class PageActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private WebDriver webDriver = Mockito.mock(WebDriver.class);
    private WebElement formElement = Mockito.mock(WebElement.class);
    private WebElement inputElement = Mockito.mock(WebElement.class);

    private PageAction action;

    @BeforeMethod
    public void setup() {
        reset(webDriver, formElement, inputElement);

        seleniumBrowser.setWebDriver(webDriver);

        action =  new PageAction();
        action.setBrowser(seleniumBrowser);

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

        action.setAction("validate");
        action.setPage(new UserFormPage());

        action.execute(context);
    }

    @Test
    public void testExecutePageType() throws Exception {
        when(inputElement.getAttribute("value")).thenReturn("TestUser");

        action.setAction("validate");
        action.setType(UserFormPage.class.getName());

        action.execute(context);
    }

    @Test
    public void testExecutePageValidator() throws Exception {
        PageValidator validator = Mockito.mock(PageValidator.class);

        when(inputElement.getAttribute("value")).thenReturn("TestUser");

        UserFormPage userForm = new UserFormPage();
        action.setAction("validate");
        action.setValidator(validator);
        action.setPage(userForm);

        action.execute(context);

        verify(validator).validate(userForm, seleniumBrowser, context);
    }

    @Test
    public void testExecuteAction() throws Exception {
        action.setAction("setUserName");
        action.setArguments(Collections.singletonList("Citrus"));
        action.setPage(new UserFormPage());

        action.execute(context);

        verify(inputElement).clear();
        verify(inputElement).sendKeys("Citrus");
    }

    @Test
    public void testExecuteActionWithArguments() throws Exception {
        when(webDriver.findElement(By.id("form"))).thenReturn(formElement);

        action.setPage(new TestPage());

        action.setAction("submit");
        action.execute(context);

        action.setAction("submitWithContext");
        action.execute(context);

        action.setAction("submitWithArgument");
        action.setArguments(Collections.singletonList("ok"));
        action.execute(context);

        action.setAction("submitWithArgumentAndContext");
        action.setArguments(Collections.singletonList("ok"));
        action.execute(context);

        verify(formElement, times(4)).submit();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Unsupported method signature for page action.*")
    public void testExecuteActionNotMatchingArguments() throws Exception {
        when(webDriver.findElement(By.id("form"))).thenReturn(formElement);

        action.setPage(new TestPage());

        action.setAction("submit");
        action.setArguments(Collections.singletonList("Citrus"));
        action.execute(context);

        verify(inputElement).clear();
        verify(inputElement).sendKeys("Citrus");

        verify(formElement).submit();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testExecuteValidationFailed() throws Exception {
        action.setAction("validate");
        action.setPage(new UserFormPage());

        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to access page type.*")
    public void testInvalidPageType() throws Exception {
        action.setAction("validate");
        action.setType(UserFormPage.class.getPackage().getName() + ".UnknownPage");

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