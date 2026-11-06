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

package org.citrusframework.selenium.actions;

import org.citrusframework.api.actions.selenium.PageValidator;
import org.citrusframework.api.actions.selenium.WebPage;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.pages.UserFormPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * @since 2.7
 */
@ExtendWith(MockitoExtension.class)
public class PageActionTest extends AbstractUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();

    @Mock
    private WebDriver webDriver;
    @Mock
    private WebElement formElement;
    @Mock
    private WebElement inputElement;
    @Mock
    private PageValidator<WebPage, SeleniumBrowser> validator;

    @BeforeEach
    public void setup() {
        reset(webDriver, formElement, inputElement);

        seleniumBrowser.setWebDriver(webDriver);

        lenient().when(formElement.getTagName()).thenReturn("form");
        lenient().when(formElement.isEnabled()).thenReturn(true);
        lenient().when(formElement.isDisplayed()).thenReturn(true);
        lenient().when(inputElement.getTagName()).thenReturn("input");
        lenient().when(inputElement.isEnabled()).thenReturn(true);
        lenient().when(inputElement.isDisplayed()).thenReturn(true);

        lenient().when(webDriver.findElement(By.id("userForm"))).thenReturn(formElement);
        lenient().when(webDriver.findElement(By.id("username"))).thenReturn(inputElement);
    }

    @Test
    public void testExecutePageValidation() {
        lenient().when(inputElement.getAttribute("value")).thenReturn("TestUser");

        PageAction action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("validate")
                .page(new UserFormPage())
                .build();
        action.execute(context);
    }

    @Test
    public void testExecutePageType() {
        lenient().when(inputElement.getAttribute("value")).thenReturn("TestUser");

        PageAction action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("validate")
                .type(UserFormPage.class.getName())
                .build();
        action.execute(context);
    }

    @Test
    public void testExecutePageValidator() {
        lenient().when(inputElement.getAttribute("value")).thenReturn("TestUser");

        UserFormPage userForm = new UserFormPage();

        PageAction action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("validate")
                .page(userForm)
                .validator(validator)
                .build();
        action.execute(context);

        verify(validator).adaptAndValidate(userForm, seleniumBrowser, context);
    }

    @Test
    public void testExecuteAction() {
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
    public void testExecuteActionWithArguments() {
        lenient().when(webDriver.findElement(By.id("form"))).thenReturn(formElement);

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

    @Test
    public void testExecuteActionNotMatchingArguments() {
        var e = Assertions.assertThrows(CitrusRuntimeException.class, () -> {
        lenient().when(webDriver.findElement(By.id("form"))).thenReturn(formElement);

        PageAction action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("submit")
                .page(new TestPage())
                .argument("Citrus")
                .build();
        action.execute(context);
    });
        Assertions.assertTrue(e.getMessage().matches("Unsupported method signature for page action.*"), e.getMessage());
    }

    @Test
    public void testExecuteValidationFailed() {
        var e = Assertions.assertThrows(ValidationException.class, () -> {
        PageAction action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("validate")
                .page(new UserFormPage())
                .build();
        action.execute(context);
    });
        Assertions.assertTrue(e.getMessage().matches("Page validation failed!"), e.getMessage());
    }

    @Test
    public void testInvalidPageType() {
        var e = Assertions.assertThrows(CitrusRuntimeException.class, () -> {

        PageAction action = new PageAction.Builder()
                .browser(seleniumBrowser)
                .action("validate")
                .type(UserFormPage.class.getPackage().getName() + ".UnknownPage")
                .build();
        action.execute(context);
    });
        Assertions.assertTrue(e.getMessage().matches("Failed to access page type.*"), e.getMessage());
    }

    public static class TestPage implements WebPage {
        @FindBy(id = "form")
        private WebElement form;

        public void submit() {
            form.submit();
        }

        public void submitWithContext(TestContext context) {
            Assertions.assertNotNull(context);
            form.submit();
        }

        public void submitWithArgument(String arg) {
            Assertions.assertNotNull(arg);
            form.submit();
        }

        public void submitWithArgumentAndContext(String arg, TestContext context) {
            Assertions.assertNotNull(arg);
            form.submit();
        }
    }
}
