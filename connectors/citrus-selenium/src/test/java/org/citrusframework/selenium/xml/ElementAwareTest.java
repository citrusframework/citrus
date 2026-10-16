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

package org.citrusframework.selenium.xml;

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

public class ElementAwareTest extends AbstractTestNGUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final WebDriver webDriver = Mockito.mock(WebDriver.class);
    private final WebElement webElement = Mockito.mock(WebElement.class);

    @BeforeMethod
    public void setup() {
        Mockito.reset(webDriver, webElement);

        seleniumBrowser.setWebDriver(webDriver);

        when(webDriver.findElement(any(By.class))).thenReturn(webElement);

        when(webElement.isDisplayed()).thenReturn(true);
        when(webElement.isEnabled()).thenReturn(true);
        when(webElement.getTagName()).thenReturn("button");
    }

    @Test
    public void shouldResolveIdLocator() {
        executeFindElement(element -> element.setId("${field}"));

        verify(webDriver).findElement(By.id("myButton"));
    }

    @Test
    public void shouldResolveNameLocator() {
        executeFindElement(element -> element.setName("${field}"));

        verify(webDriver).findElement(By.name("myButton"));
    }

    @Test
    public void shouldResolveLinkTextLocator() {
        executeFindElement(element -> element.setLinkText("${field}"));

        verify(webDriver).findElement(By.linkText("myButton"));
    }

    @Test
    public void shouldResolvePartialLinkTextLocator() {
        executeFindElement(element -> element.setPartialLinkText("${field}"));

        verify(webDriver).findElement(By.partialLinkText("myButton"));
    }

    @Test
    public void shouldResolveXpathLocator() {
        executeFindElement(element -> element.setXpath("${field}"));

        verify(webDriver).findElement(By.xpath("myButton"));
    }

    @Test
    public void shouldResolveCssSelectorLocator() {
        executeFindElement(element -> element.setCssSelector("${field}"));

        verify(webDriver).findElement(By.cssSelector("myButton"));
    }

    @Test
    public void shouldResolveClassNameLocator() {
        executeFindElement(element -> element.setClassName("${field}"));

        verify(webDriver).findElement(By.className("myButton"));
    }

    @Test
    public void shouldResolveTagNameLocator() {
        executeFindElement(element -> element.setTagName("${field}"));

        verify(webDriver).findElement(By.tagName("myButton"));
    }

    @Test
    public void shouldResolvePropertyLocator() {
        executeFindElement(element -> {
            ElementAware.Element.Property property = new ElementAware.Element.Property();
            property.setName("id");
            property.setValue("${field}");
            element.setProperty(property);
        });

        verify(webDriver).findElement(By.id("myButton"));
    }

    private void executeFindElement(java.util.function.Consumer<ElementAware.Element> locator) {
        context.setVariable("field", "myButton");

        ElementAware.Element element = new ElementAware.Element();
        locator.accept(element);

        FindElement findElement = new FindElement();
        findElement.setElement(element);

        findElement.browser(seleniumBrowser).build().execute(context);
    }
}
