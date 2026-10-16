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

package org.citrusframework.selenium.model;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @since 5.1
 */
public class ElementLocatorTest extends AbstractTestNGUnitTest {

    @Test
    public void testResolveBy() {
        By by = By.id("myButton");
        assertEquals(ElementLocator.of(by).resolve(context), by);
    }

    @Test
    public void testResolveId() {
        assertEquals(ElementLocator.of("id", "myButton").resolve(context), By.id("myButton"));
    }

    @Test
    public void testResolveName() {
        assertEquals(ElementLocator.of("name", "userName").resolve(context), By.name("userName"));
    }

    @Test
    public void testResolveClassName() {
        assertEquals(ElementLocator.of("class-name", "clickable").resolve(context), By.className("clickable"));
    }

    @Test
    public void testResolveLinkText() {
        assertEquals(ElementLocator.of("link-text", "Click me").resolve(context), By.linkText("Click me"));
    }

    @Test
    public void testResolvePartialLinkText() {
        assertEquals(ElementLocator.of("partial-link-text", "Click").resolve(context), By.partialLinkText("Click"));
    }

    @Test
    public void testResolveCssSelector() {
        assertEquals(ElementLocator.of("css-selector", "#myButton").resolve(context), By.cssSelector("#myButton"));
    }

    @Test
    public void testResolveTagName() {
        assertEquals(ElementLocator.of("tag-name", "button").resolve(context), By.tagName("button"));
    }

    @Test
    public void testResolveXpath() {
        assertEquals(ElementLocator.of("xpath", "//button[@id='myButton']").resolve(context), By.xpath("//button[@id='myButton']"));
    }

    @Test
    public void testResolveDynamicContent() {
        context.setVariable("buttonId", "myButton");

        assertEquals(ElementLocator.of("id", "${buttonId}").resolve(context), By.id("myButton"));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Unknown selector type: unknown")
    public void testUnknownSelectorType() {
        ElementLocator.of("unknown", "myButton").resolve(context);
    }
}
