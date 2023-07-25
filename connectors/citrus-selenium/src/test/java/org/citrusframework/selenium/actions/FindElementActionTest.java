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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class FindElementActionTest extends AbstractTestNGUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final WebDriver webDriver = Mockito.mock(WebDriver.class);
    private final WebElement element = Mockito.mock(WebElement.class);

    @BeforeMethod
    public void setup() {
        reset(webDriver, element);

        seleniumBrowser.setWebDriver(webDriver);

        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);
        when(element.getTagName()).thenReturn("button");
    }

    @Test(dataProvider = "findByProvider")
    public void testExecuteFindBy(String property, String value, final By by) throws Exception {
        when(webDriver.findElement(any(By.class))).thenAnswer(new Answer<WebElement>() {
            @Override
            public WebElement answer(InvocationOnMock invocation) throws Throwable {
                By select = (By) invocation.getArguments()[0];

                Assert.assertEquals(select.getClass(), by.getClass());
                Assert.assertEquals(select.toString(), by.toString());
                return element;
            }
        });

        FindElementAction action =  new FindElementAction.Builder()
                .browser(seleniumBrowser)
                .element(property, value)
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariableObject("button"), element);
    }

    @DataProvider
    public Object[][] findByProvider() {
        return new Object[][] {
            new Object[] { "id", "myId", By.id("myId") },
            new Object[] { "name", "myName", By.name("myName") },
            new Object[] { "tag-name", "button", By.tagName("button") },
            new Object[] { "class-name", "myClass", By.className("myClass") },
            new Object[] { "link-text", "myLinkText", By.linkText("myLinkText") },
            new Object[] { "css-selector", "myCss", By.cssSelector("myCss") },
            new Object[] { "xpath", "myXpath", By.xpath("myXpath") }
        };
    }

    @Test
    public void testExecuteFindByVariableSupport() throws Exception {
        when(webDriver.findElement(any(By.class))).thenAnswer(new Answer<WebElement>() {
            @Override
            public WebElement answer(InvocationOnMock invocation) throws Throwable {
                By select = (By) invocation.getArguments()[0];

                Assert.assertEquals(select.getClass(), By.ById.class);
                Assert.assertEquals(select.toString(), By.id("clickMe").toString());
                return element;
            }
        });

        context.setVariable("myId", "clickMe");

        FindElementAction action =  new FindElementAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "${myId}")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariableObject("button"), element);
    }

    @Test
    public void testExecuteFindByValidation() throws Exception {
        when(element.getText()).thenReturn("Click Me!");
        when(element.getAttribute("type")).thenReturn("submit");
        when(element.getCssValue("color")).thenReturn("red");

        when(webDriver.findElement(any(By.class))).thenAnswer(new Answer<WebElement>() {
            @Override
            public WebElement answer(InvocationOnMock invocation) throws Throwable {
                By select = (By) invocation.getArguments()[0];

                Assert.assertEquals(select.getClass(), By.ByName.class);
                Assert.assertEquals(select.toString(), By.name("clickMe").toString());
                return element;
            }
        });

        FindElementAction action =  new FindElementAction.Builder()
                .browser(seleniumBrowser)
                .element("name", "clickMe")
                .tagName("button")
                .text("Click Me!")
                .attribute("type", "submit")
                .style("color", "red")
                .build();
        action.execute(context);

        Assert.assertEquals(context.getVariableObject("button"), element);
    }

    @Test(dataProvider = "validationErrorProvider")
    public void testExecuteFindByValidationFailed(String tagName, String text, String attribute, String cssStyle, boolean displayed, boolean enabled, String errorMsg) throws Exception {
        when(element.getTagName()).thenReturn("button");
        when(element.getText()).thenReturn("Click Me!");
        when(element.getAttribute("type")).thenReturn("submit");
        when(element.getCssValue("color")).thenReturn("red");

        when(webDriver.findElement(any(By.class))).thenAnswer(new Answer<WebElement>() {
            @Override
            public WebElement answer(InvocationOnMock invocation) throws Throwable {
                By select = (By) invocation.getArguments()[0];

                Assert.assertEquals(select.getClass(), By.ByName.class);
                Assert.assertEquals(select.toString(), By.name("clickMe").toString());
                return element;
            }
        });

        FindElementAction action =  new FindElementAction.Builder()
                .browser(seleniumBrowser)
                .element("name", "clickMe")
                .tagName(tagName)
                .text(text)
                .attribute("type", attribute)
                .style("color", cssStyle)
                .displayed(displayed)
                .enabled(enabled)
                .build();
        try {
            action.execute(context);
            Assert.fail("Missing exception to to validation error");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().endsWith(errorMsg), e.getMessage());
        }
    }

    @DataProvider
    public Object[][] validationErrorProvider() {
        return new Object[][] {
                new Object[] { "input", "Click Me!", "submit", "red", true, true, "tag-name expected 'input', but was 'button'" },
                new Object[] { "button", "Click!", "submit", "red", true, true, "text expected 'Click!', but was 'Click Me!'" },
                new Object[] { "button", "Click Me!", "cancel", "red", true, true, "attribute 'type' expected 'cancel', but was 'submit'" },
                new Object[] { "button", "Click Me!", "submit", "red", false, true, "'displayed' expected 'false', but was 'true'" },
                new Object[] { "button", "Click Me!", "submit", "red", true, false, "'enabled' expected 'false', but was 'true'" },
                new Object[] { "button", "Click Me!", "submit", "blue", true, true, "css style 'color' expected 'blue', but was 'red'" }
        };
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to find element 'By.id: myButton' on page")
    public void testElementNotFound() {
        when(webDriver.findElement(any(By.class))).thenReturn(null);

        FindElementAction action =  new FindElementAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "myButton")
                .build();
        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Unknown selector type: unsupported")
    public void testElementUnsupportedProperty() {
        FindElementAction action =  new FindElementAction.Builder()
                .browser(seleniumBrowser)
                .element("unsupported", "wrong")
                .build();
        action.execute(context);
    }

}
