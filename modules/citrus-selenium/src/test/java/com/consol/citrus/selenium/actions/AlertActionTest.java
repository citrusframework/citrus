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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class AlertActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private WebDriver webDriver = Mockito.mock(WebDriver.class);
    private WebDriver.TargetLocator locator = Mockito.mock(WebDriver.TargetLocator.class);

    private Alert alert = Mockito.mock(Alert.class);

    private AlertAction action;

    @BeforeMethod
    public void setup() {
        reset(webDriver, alert, locator);

        seleniumBrowser.setWebDriver(webDriver);

        action =  new AlertAction();
        action.setBrowser(seleniumBrowser);

        when(webDriver.switchTo()).thenReturn(locator);
        when(alert.getText()).thenReturn("This is a warning!");
    }

    @Test
    public void testExecuteAccept() throws Exception {
        when(locator.alert()).thenReturn(alert);

        action.execute(context);

        verify(alert).accept();
    }

    @Test
    public void testExecuteDismiss() throws Exception {
        WebDriver.TargetLocator locator = Mockito.mock(WebDriver.TargetLocator.class);
        when(webDriver.switchTo()).thenReturn(locator);
        when(locator.alert()).thenReturn(alert);

        action.setAccept(false);

        action.execute(context);

        verify(alert).dismiss();
    }

    @Test
    public void testExecuteTextValidation() throws Exception {
        when(locator.alert()).thenReturn(alert);

        action.setText("This is a warning!");
        action.execute(context);

        verify(alert).accept();
    }

    @Test
    public void testExecuteTextValidationVariableSupport() throws Exception {
        when(locator.alert()).thenReturn(alert);

        context.setVariable("alertText","This is a warning!");
        action.setText("${alertText}");
        action.execute(context);

        verify(alert).accept();
    }

    @Test
    public void testExecuteTextValidationMatcherSupport() throws Exception {
        when(locator.alert()).thenReturn(alert);

        action.setText("@startsWith('This is')@");
        action.execute(context);

        verify(alert).accept();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Failed to validate alert dialog text.*")
    public void testExecuteTextValidationError() throws Exception {
        when(locator.alert()).thenReturn(alert);

        action.setText("This is not a warning!");
        action.execute(context);

        verify(alert).accept();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to access alert dialog - not found")
    public void testAlertNotFound() {
        WebDriver.TargetLocator locator = Mockito.mock(WebDriver.TargetLocator.class);
        when(webDriver.switchTo()).thenReturn(locator);
        when(locator.alert()).thenReturn(null);

        action.execute(context);
    }

}