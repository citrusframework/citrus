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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VerifyActionTest extends AbstractTestNGUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final WebDriver webDriver = Mockito.mock(WebDriver.class);
    private final WebElement element = Mockito.mock(WebElement.class);

    @BeforeMethod
    public void setup() {
        Mockito.reset(webDriver, element);

        seleniumBrowser.setWebDriver(webDriver);
    }

    @Test
    public void shouldVerifyUrl() {
        when(webDriver.getCurrentUrl()).thenReturn("https://citrusframework.org/orders/42");

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .url("https://citrusframework.org/orders/42")
                .build()
                .execute(context);

        verify(webDriver).getCurrentUrl();
    }

    @Test
    public void shouldVerifyUrlWithMatcherExpression() {
        when(webDriver.getCurrentUrl()).thenReturn("https://citrusframework.org/orders/42");

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .url("@endsWith('/orders/42')@")
                .build()
                .execute(context);

        verify(webDriver).getCurrentUrl();
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Selenium validation failed, url expected 'https://citrusframework.org/orders/41', but was 'https://citrusframework.org/orders/42'")
    public void shouldFailOnUrlMismatch() {
        when(webDriver.getCurrentUrl()).thenReturn("https://citrusframework.org/orders/42");

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .url("https://citrusframework.org/orders/41")
                .build()
                .execute(context);
    }

    @Test
    public void shouldVerifyTitle() {
        when(webDriver.getTitle()).thenReturn("Citrus page");

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .title("@contains('Citrus')@")
                .build()
                .execute(context);

        verify(webDriver).getTitle();
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Selenium validation failed, title expected 'Other page', but was 'Citrus page'")
    public void shouldFailOnTitleMismatch() {
        when(webDriver.getTitle()).thenReturn("Citrus page");

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .title("Other page")
                .build()
                .execute(context);
    }

    @Test
    public void shouldVerifyElementPresent() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .present()
                .element("id", "greeting")
                .build()
                .execute(context);

        verify(webDriver).findElements(By.id("greeting"));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Selenium validation failed, element 'By\\.id: error' expected 'present', but was 'absent'")
    public void shouldFailOnElementPresentWhenMissing() {
        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .present()
                .element("id", "error")
                .build()
                .execute(context);
    }

    @Test
    public void shouldVerifyElementAbsent() {
        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .absent()
                .element("id", "error")
                .build()
                .execute(context);

        verify(webDriver).findElements(By.id("error"));
    }

    @Test
    public void shouldVerifyElementDisplayed() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
        when(element.isDisplayed()).thenReturn(true);

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .displayed()
                .element("id", "greeting")
                .build()
                .execute(context);

        verify(element).isDisplayed();
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Selenium validation failed, element 'By\\.id: greeting' expected 'displayed', but was 'hidden'")
    public void shouldFailOnElementDisplayedWhenHidden() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
        when(element.isDisplayed()).thenReturn(false);

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .displayed()
                .element("id", "greeting")
                .build()
                .execute(context);
    }

    @Test
    public void shouldVerifyElementHiddenWhenMissing() {
        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .hidden()
                .element("id", "error")
                .build()
                .execute(context);

        verify(webDriver).findElements(By.id("error"));
    }

    @Test
    public void shouldVerifyElementHiddenWhenNotDisplayed() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
        when(element.isDisplayed()).thenReturn(false);

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .hidden()
                .element("id", "error")
                .build()
                .execute(context);

        verify(element).isDisplayed();
    }

    @Test
    public void shouldVerifyElementEnabled() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
        when(element.isEnabled()).thenReturn(true);

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .enabled()
                .element("id", "submit")
                .build()
                .execute(context);

        verify(element).isEnabled();
    }

    @Test
    public void shouldVerifyElementDisabled() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
        when(element.isEnabled()).thenReturn(false);

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .disabled()
                .element("id", "submit")
                .build()
                .execute(context);

        verify(element).isEnabled();
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Selenium validation failed, element 'By\\.id: submit' expected 'disabled', but was 'enabled'")
    public void shouldFailOnElementDisabledWhenEnabled() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
        when(element.isEnabled()).thenReturn(true);

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .disabled()
                .element("id", "submit")
                .build()
                .execute(context);
    }

    @Test
    public void shouldVerifyElementSelected() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
        when(element.isSelected()).thenReturn(true);

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .selected()
                .element("id", "option")
                .build()
                .execute(context);

        verify(element).isSelected();
    }

    @Test
    public void shouldVerifyElementNotSelected() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
        when(element.isSelected()).thenReturn(false);

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .notSelected()
                .element("id", "option")
                .build()
                .execute(context);

        verify(element).isSelected();
    }

    @Test
    public void shouldVerifyTextWithMatcherExpression() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
        when(element.getText()).thenReturn("Hello Citrus");

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .text("@contains('Citrus')@")
                .element("id", "greeting")
                .build()
                .execute(context);

        verify(element).getText();
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Selenium validation failed, text expected 'Hello World', but was 'Hello Citrus'")
    public void shouldFailOnTextMismatch() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
        when(element.getText()).thenReturn("Hello Citrus");

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .text("Hello World")
                .element("id", "greeting")
                .build()
                .execute(context);
    }

    @Test
    public void shouldVerifyValue() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
        when(element.getDomProperty("value")).thenReturn("new-value");

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .value("new-value")
                .element("id", "username")
                .build()
                .execute(context);

        verify(element).getDomProperty("value");
    }

    @Test
    public void shouldVerifyAttribute() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
        when(element.getDomAttribute("href")).thenReturn("https://citrusframework.org");

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .attribute("href", "https://citrusframework.org")
                .element("id", "docs")
                .build()
                .execute(context);

        verify(element).getDomAttribute("href");
    }

    @Test
    public void shouldVerifyAttributeWithGetAttributeFallback() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
        when(element.getAttribute("href")).thenReturn("https://citrusframework.org");

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .attribute("href", "https://citrusframework.org")
                .element("id", "docs")
                .build()
                .execute(context);

        verify(element).getDomAttribute("href");
        verify(element).getAttribute("href");
    }

    @Test
    public void shouldVerifyCssValue() {
        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
        when(element.getCssValue("color")).thenReturn("#000000");

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .cssValue("color", "#000000")
                .element("id", "greeting")
                .build()
                .execute(context);

        verify(element).getCssValue("color");
    }

    @Test
    public void shouldVerifyCount() {
        when(webDriver.findElements(any(By.class))).thenReturn(List.of(element, element, element));

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .count("3")
                .element("css-selector", "tr.order")
                .build()
                .execute(context);

        verify(webDriver).findElements(By.cssSelector("tr.order"));
    }

    @Test
    public void shouldVerifyCountZeroWhenNothingMatches() {
        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .count("0")
                .element("css-selector", "li.item")
                .build()
                .execute(context);

        verify(webDriver).findElements(By.cssSelector("li.item"));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Selenium validation failed, count expected '2', but was '3'")
    public void shouldFailOnCountMismatch() {
        when(webDriver.findElements(any(By.class))).thenReturn(List.of(element, element, element));

        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .count("2")
                .element("css-selector", "tr.order")
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Missing verify check - use one of url, title, present, absent, displayed, hidden, enabled, disabled, selected, not-selected, text, value, attribute, css-value, count")
    public void shouldFailOnMissingCheck() {
        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Unsupported verify check 'unknown' - use one of url, title, present, absent, displayed, hidden, enabled, disabled, selected, not-selected, text, value, attribute, css-value, count")
    public void shouldFailOnUnsupportedCheck() {
        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .check("unknown")
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Missing element selector for verify check 'present'")
    public void shouldFailOnElementCheckWithoutElementSelector() {
        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .present()
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to find element 'By\\.id: missing' on page")
    public void shouldFailOnMissingElementForText() {
        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .text("Hello")
                .element("id", "missing")
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Missing attribute name for verify check 'attribute'")
    public void shouldFailOnAttributeWithoutName() {
        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .check("attribute")
                .element("id", "docs")
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Missing attribute name for verify check 'css-value'")
    public void shouldFailOnCssValueWithoutName() {
        new VerifyAction.Builder()
                .browser(seleniumBrowser)
                .check("css-value")
                .element("id", "docs")
                .build()
                .execute(context);
    }
}
