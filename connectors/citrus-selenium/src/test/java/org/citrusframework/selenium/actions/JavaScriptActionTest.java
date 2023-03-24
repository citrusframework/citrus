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

import java.util.Collections;
import java.util.List;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumHeaders;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class JavaScriptActionTest extends AbstractTestNGUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final ChromeDriver webDriver = Mockito.mock(ChromeDriver.class);

    @BeforeMethod
    public void setup() {
        reset(webDriver);

        seleniumBrowser.setWebDriver(webDriver);
    }

    @Test
    public void testExecute() throws Exception {
        when(webDriver.executeScript(eq("return window._selenide_jsErrors"))).thenReturn(Collections.emptyList());

        JavaScriptAction action =  new JavaScriptAction.Builder()
                .browser(seleniumBrowser)
                .script("alert('Hello')")
                .build();
        action.execute(context);

        Assert.assertNotNull(context.getVariableObject(SeleniumHeaders.SELENIUM_JS_ERRORS));
        Assert.assertEquals(((List<?>) context.getVariableObject(SeleniumHeaders.SELENIUM_JS_ERRORS)).size(), 0L);

        verify(webDriver).executeScript(eq("alert('Hello')"));
    }

    @Test
    public void testExecuteVariableSupport() throws Exception {
        when(webDriver.executeScript(eq("return window._selenide_jsErrors"))).thenReturn(Collections.emptyList());

        context.setVariable("text", "Hello");

        JavaScriptAction action =  new JavaScriptAction.Builder()
                .browser(seleniumBrowser)
                .script("alert('${text}')")
                .build();
        action.execute(context);

        Assert.assertNotNull(context.getVariableObject(SeleniumHeaders.SELENIUM_JS_ERRORS));
        Assert.assertEquals(((List<?>) context.getVariableObject(SeleniumHeaders.SELENIUM_JS_ERRORS)).size(), 0L);

        verify(webDriver).executeScript(eq("alert('Hello')"));
    }

    @Test
    public void testExecuteWithErrorValidation() throws Exception {
        when(webDriver.executeScript(eq("return window._selenide_jsErrors"))).thenReturn(Collections.singletonList("This went totally wrong!"));

        JavaScriptAction action =  new JavaScriptAction.Builder()
                .browser(seleniumBrowser)
                .script("alert('Hello')")
                .errors("This went totally wrong!")
                .build();
        action.execute(context);

        Assert.assertNotNull(context.getVariableObject(SeleniumHeaders.SELENIUM_JS_ERRORS));
        Assert.assertEquals(((List<?>) context.getVariableObject(SeleniumHeaders.SELENIUM_JS_ERRORS)).size(), 1L);

        verify(webDriver).executeScript(eq("alert('Hello')"));
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testExecuteWithErrorValidationFailed() throws Exception {
        when(webDriver.executeScript(eq("return window._selenide_jsErrors"))).thenReturn(Collections.emptyList());

        JavaScriptAction action =  new JavaScriptAction.Builder()
                .browser(seleniumBrowser)
                .script("alert('Hello')")
                .errors("This went totally wrong!")
                .build();
        action.execute(context);
    }

}
