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

import java.util.Arrays;
import java.util.Collections;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExtractActionTest extends AbstractTestNGUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final WebDriver webDriver = Mockito.mock(WebDriver.class);
    private final WebElement element = Mockito.mock(WebElement.class);

    @BeforeMethod
    public void setup() {
        reset(webDriver, element);

        seleniumBrowser.setWebDriver(webDriver);

        when(webDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(element));
    }

    @Test
    public void shouldExtractUrl() {
        when(webDriver.getCurrentUrl()).thenReturn("https://citrusframework.org/orders/42");

        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .url()
                .variable("url")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable("url"), "https://citrusframework.org/orders/42");
        verify(webDriver).getCurrentUrl();
    }

    @Test
    public void shouldExtractTitle() {
        when(webDriver.getTitle()).thenReturn("Citrus orders");

        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .title()
                .variable("title")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable("title"), "Citrus orders");
        verify(webDriver).getTitle();
    }

    @Test
    public void shouldExtractPageSource() {
        when(webDriver.getPageSource()).thenReturn("<html><body>Citrus</body></html>");

        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .pageSource()
                .variable("pageSource")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable("pageSource"), "<html><body>Citrus</body></html>");
        verify(webDriver).getPageSource();
    }

    @Test
    public void shouldExtractText() {
        when(element.getText()).thenReturn("A-17");

        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "order-id")
                .text()
                .variable("orderId")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable("orderId"), "A-17");
        verify(webDriver).findElements(By.id("order-id"));
        verify(element).getText();
    }

    @Test
    public void shouldExtractTextWithDynamicLocator() {
        context.setVariable("field", "order-id");
        when(element.getText()).thenReturn("A-17");

        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "${field}")
                .text()
                .variable("orderId")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable("orderId"), "A-17");
        verify(webDriver).findElements(By.id("order-id"));
    }

    @Test
    public void shouldExtractValue() {
        when(element.getDomProperty("value")).thenReturn("new-value");

        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "username")
                .value()
                .variable("usernameValue")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable("usernameValue"), "new-value");
        verify(element).getDomProperty("value");
    }

    @Test
    public void shouldExtractAttribute() {
        when(element.getDomAttribute("href")).thenReturn("https://citrusframework.org");

        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "docs")
                .attribute("href")
                .variable("href")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable("href"), "https://citrusframework.org");
        verify(element).getDomAttribute("href");
    }

    @Test
    public void shouldExtractAttributeWithFallback() {
        when(element.getDomAttribute("href")).thenReturn(null);
        when(element.getAttribute("href")).thenReturn("https://citrusframework.org");

        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "docs")
                .attribute("href")
                .variable("href")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable("href"), "https://citrusframework.org");
        verify(element).getAttribute("href");
    }

    @Test
    public void shouldExtractCssValue() {
        when(element.getCssValue("color")).thenReturn("#000000");

        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "greeting")
                .cssValue("color")
                .variable("color")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable("color"), "#000000");
        verify(element).getCssValue("color");
    }

    @Test
    public void shouldExtractCount() {
        when(webDriver.findElements(By.className("order"))).thenReturn(Arrays.asList(element, element, element));

        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "order")
                .count()
                .variable("orderCount")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable("orderCount"), "3");
        verify(webDriver).findElements(By.className("order"));
    }

    @Test
    public void shouldExtractCountZeroWhenNothingMatches() {
        when(webDriver.findElements(By.className("order"))).thenReturn(Collections.emptyList());

        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .element("class-name", "order")
                .count()
                .variable("orderCount")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariable("orderCount"), "0");
    }

    @Test
    public void shouldFailOnMissingElementForText() {
        when(webDriver.findElements(By.id("order-id"))).thenReturn(Collections.emptyList());

        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "order-id")
                .text()
                .variable("orderId")
                .build();

        Assert.assertThrows(CitrusRuntimeException.class, () -> action.execute(context));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Failed to find element 'By\\.id: order-id' on page")
    public void shouldFailOnMissingElementWithMessage() {
        when(webDriver.findElements(By.id("order-id"))).thenReturn(Collections.emptyList());

        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "order-id")
                .text()
                .variable("orderId")
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Missing variable name for extract action")
    public void shouldFailOnMissingVariable() {
        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .url()
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Missing extract value - use one of url, title, page-source, text, value, attribute, css-value, count")
    public void shouldFailOnMissingValue() {
        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .variable("value")
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Unsupported extract value 'unknown' - use one of url, title, page-source, text, value, attribute, css-value, count")
    public void shouldFailOnUnsupportedValue() {
        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .value("unknown")
                .variable("value")
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Missing element selector for extract value 'text'")
    public void shouldFailOnElementValueWithoutElementSelector() {
        ExtractAction action = new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .text()
                .variable("orderId")
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Failed to extract attribute 'href' from element 'By\\.id: docs'")
    public void shouldFailOnMissingAttribute() {
        when(element.getDomAttribute("href")).thenReturn(null);
        when(element.getAttribute("href")).thenReturn(null);

        new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "docs")
                .attribute("href")
                .variable("href")
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Failed to extract value from element 'By\\.id: username'")
    public void shouldFailOnMissingExtractedValue() {
        when(element.getDomProperty("value")).thenReturn(null);

        new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "username")
                .value()
                .variable("usernameValue")
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Missing attribute name for extract value 'attribute'")
    public void shouldFailOnAttributeWithoutName() {
        new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "docs")
                .value("attribute")
                .variable("href")
                .build()
                .execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Missing attribute name for extract value 'css-value'")
    public void shouldFailOnCssValueWithoutName() {
        new ExtractAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "docs")
                .value("css-value")
                .variable("color")
                .build()
                .execute(context);
    }
}
