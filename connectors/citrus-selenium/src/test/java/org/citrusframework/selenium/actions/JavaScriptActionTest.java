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

import java.util.Collections;
import java.util.List;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumHeaders;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
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
    public void testExecute() {
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
    public void testExecuteInstallsErrorCollector() {
        when(webDriver.executeScript(eq("return window._selenide_jsErrors"))).thenReturn(Collections.emptyList());

        JavaScriptAction action =  new JavaScriptAction.Builder()
                .browser(seleniumBrowser)
                .script("alert('Hello')")
                .build();
        action.execute(context);

        InOrder inOrder = inOrder(webDriver);
        inOrder.verify(webDriver).executeScript(eq(JavaScriptAction.ERROR_COLLECTOR_SCRIPT));
        inOrder.verify(webDriver).executeScript(eq("alert('Hello')"));
        inOrder.verify(webDriver).executeScript(eq("return window._selenide_jsErrors"));
    }

    @Test
    public void testExecuteVariableSupport() {
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
    public void testExecuteStoresStringResultInVariable() {
        when(webDriver.executeScript(eq("return document.title;"))).thenReturn("Frames Test");
        when(webDriver.executeScript(eq("return window._selenide_jsErrors"))).thenReturn(Collections.emptyList());

        JavaScriptAction action =  new JavaScriptAction.Builder()
                .browser(seleniumBrowser)
                .script("return document.title;")
                .variable("pageTitle")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable("pageTitle"), "Frames Test");
    }

    @Test
    public void testExecuteStoresNumberResultInVariable() {
        when(webDriver.executeScript(eq("return document.images.length;"))).thenReturn(42L);
        when(webDriver.executeScript(eq("return window._selenide_jsErrors"))).thenReturn(Collections.emptyList());

        JavaScriptAction action =  new JavaScriptAction.Builder()
                .browser(seleniumBrowser)
                .script("return document.images.length;")
                .variable("imageCount")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable("imageCount"), "42");
    }

    @Test
    public void testExecuteStoresEmptyStringForNullResult() {
        when(webDriver.executeScript(eq("return undefined;"))).thenReturn(null);
        when(webDriver.executeScript(eq("return window._selenide_jsErrors"))).thenReturn(Collections.emptyList());

        JavaScriptAction action =  new JavaScriptAction.Builder()
                .browser(seleniumBrowser)
                .script("return undefined;")
                .variable("nothing")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable("nothing"), "");
    }

    @Test
    public void testExecuteWithoutVariableStoresNothing() {
        when(webDriver.executeScript(eq("return window._selenide_jsErrors"))).thenReturn(Collections.emptyList());

        JavaScriptAction action =  new JavaScriptAction.Builder()
                .browser(seleniumBrowser)
                .script("alert('Hello')")
                .build();
        action.execute(context);

        Assert.assertFalse(context.getVariables().containsKey("pageTitle"));
    }

    @Test
    public void testExecuteWithErrorValidation() {
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

    @Test
    public void testExecuteCollectsPageErrors() {
        HtmlUnitDriver htmlUnitDriver = new HtmlUnitDriver(true);
        try {
            htmlUnitDriver.get("about:blank");
            seleniumBrowser.setWebDriver(htmlUnitDriver);

            JavaScriptAction action =  new JavaScriptAction.Builder()
                    .browser(seleniumBrowser)
                    // NB: HtmlUnit ignores the ErrorEvent init dict, so build the error event the old way.
                    // Real browsers also accept: window.dispatchEvent(new ErrorEvent('error', {message: '...'}))
                    .script("var e = document.createEvent('Event'); e.initEvent('error', false, false); e.message = 'This went totally wrong!'; window.dispatchEvent(e);")
                    .error("This went totally wrong!")
                    .build();
            action.execute(context);

            Assert.assertEquals(((List<?>) context.getVariableObject(SeleniumHeaders.SELENIUM_JS_ERRORS)).size(), 1L);
        } finally {
            htmlUnitDriver.quit();
        }
    }

    @Test
    public void testExecuteInstallsErrorCollectorOnlyOnce() {
        HtmlUnitDriver htmlUnitDriver = new HtmlUnitDriver(true);
        try {
            htmlUnitDriver.get("about:blank");
            seleniumBrowser.setWebDriver(htmlUnitDriver);

            JavaScriptAction action =  new JavaScriptAction.Builder()
                    .browser(seleniumBrowser)
                    .script("var e = document.createEvent('Event'); e.initEvent('error', false, false); e.message = 'This went totally wrong!'; window.dispatchEvent(e);")
                    .build();
            action.execute(context);
            action.execute(context);

            Assert.assertEquals(((List<?>) context.getVariableObject(SeleniumHeaders.SELENIUM_JS_ERRORS)).size(), 2L);
        } finally {
            htmlUnitDriver.quit();
        }
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testExecuteMissingPageErrorValidationFailed() {
        HtmlUnitDriver htmlUnitDriver = new HtmlUnitDriver(true);
        try {
            htmlUnitDriver.get("about:blank");
            seleniumBrowser.setWebDriver(htmlUnitDriver);

            JavaScriptAction action =  new JavaScriptAction.Builder()
                    .browser(seleniumBrowser)
                    .script("var e = document.createEvent('Event'); e.initEvent('error', false, false); e.message = 'This went totally wrong!'; window.dispatchEvent(e);")
                    .error("A completely different error")
                    .build();
            action.execute(context);
        } finally {
            htmlUnitDriver.quit();
        }
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testExecuteWithErrorValidationFailed() {
        when(webDriver.executeScript(eq("return window._selenide_jsErrors"))).thenReturn(Collections.emptyList());

        JavaScriptAction action =  new JavaScriptAction.Builder()
                .browser(seleniumBrowser)
                .script("alert('Hello')")
                .errors("This went totally wrong!")
                .build();
        action.execute(context);
    }
}
