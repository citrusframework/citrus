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

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.endpoint.SeleniumHeaders;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class JavaScriptActionTest extends AbstractTestNGUnitTest {

    private SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private ChromeDriver webDriver = Mockito.mock(ChromeDriver.class);

    private JavaScriptAction action;

    @BeforeMethod
    public void setup() {
        reset(webDriver);

        seleniumBrowser.setWebDriver(webDriver);

        action =  new JavaScriptAction();
        action.setBrowser(seleniumBrowser);
    }

    @Test
    public void testExecute() throws Exception {
        when(webDriver.executeScript(eq("return window._selenide_jsErrors"))).thenReturn(Collections.emptyList());

        action.setScript("alert('Hello')");
        action.execute(context);

        Assert.assertNotNull(context.getVariableObject(SeleniumHeaders.SELENIUM_JS_ERRORS));
        Assert.assertEquals(((List) context.getVariableObject(SeleniumHeaders.SELENIUM_JS_ERRORS)).size(), 0L);

        verify(webDriver).executeScript(eq("alert('Hello')"));
    }

    @Test
    public void testExecuteVariableSupport() throws Exception {
        when(webDriver.executeScript(eq("return window._selenide_jsErrors"))).thenReturn(Collections.emptyList());

        context.setVariable("text", "Hello");

        action.setScript("alert('${text}')");
        action.execute(context);

        Assert.assertNotNull(context.getVariableObject(SeleniumHeaders.SELENIUM_JS_ERRORS));
        Assert.assertEquals(((List) context.getVariableObject(SeleniumHeaders.SELENIUM_JS_ERRORS)).size(), 0L);

        verify(webDriver).executeScript(eq("alert('Hello')"));
    }

    @Test
    public void testExecuteWithErrorValidation() throws Exception {
        when(webDriver.executeScript(eq("return window._selenide_jsErrors"))).thenReturn(Collections.singletonList("This went totally wrong!"));

        action.setScript("alert('Hello')");
        action.setExpectedErrors(Collections.singletonList("This went totally wrong!"));
        action.execute(context);

        Assert.assertNotNull(context.getVariableObject(SeleniumHeaders.SELENIUM_JS_ERRORS));
        Assert.assertEquals(((List) context.getVariableObject(SeleniumHeaders.SELENIUM_JS_ERRORS)).size(), 1L);

        verify(webDriver).executeScript(eq("alert('Hello')"));
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testExecuteWithErrorValidationFailed() throws Exception {
        when(webDriver.executeScript(eq("return window._selenide_jsErrors"))).thenReturn(Collections.emptyList());

        action.setScript("alert('Hello')");
        action.setExpectedErrors(Collections.singletonList("This went totally wrong!"));
        action.execute(context);
    }

}