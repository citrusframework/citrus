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

import java.util.stream.Stream;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @since 2.7
 */
public class FindElementActionTest extends AbstractUnitTest {

    private final SeleniumBrowser seleniumBrowser = new SeleniumBrowser();
    private final WebDriver webDriver = Mockito.mock(WebDriver.class);
    private final WebElement element = Mockito.mock(WebElement.class);

    @BeforeEach
    public void setup() {
        reset(webDriver, element);

        seleniumBrowser.setWebDriver(webDriver);

        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);
        when(element.getTagName()).thenReturn("button");
    }

    @ParameterizedTest(name = "findByProvider={0}")
    @MethodSource("findByProvider")
    public void testExecuteFindBy(String property, String value, final By by) {
        when(webDriver.findElement(any(By.class))).thenAnswer((Answer<WebElement>) invocation -> {
            By select = (By) invocation.getArguments()[0];

            Assertions.assertEquals(select.getClass(), by.getClass());
            Assertions.assertEquals(select.toString(), by.toString());
            return element;
        });

        FindElementAction action =  new FindElementAction.Builder()
                .browser(seleniumBrowser)
                .element(property, value)
                .build();
        action.execute(context);

        Assertions.assertEquals(context.getVariableObject("button"), element);
    }

    static Stream<Arguments> findByProvider() {
        return Stream.of(
            Arguments.of("id", "myId", By.id("myId")),
            Arguments.of("name", "myName", By.name("myName")),
            Arguments.of("tag-name", "button", By.tagName("button")),
            Arguments.of("class-name", "myClass", By.className("myClass")),
            Arguments.of("link-text", "myLinkText", By.linkText("myLinkText")),
            Arguments.of("css-selector", "myCss", By.cssSelector("myCss")),
            Arguments.of("xpath", "myXpath", By.xpath("myXpath"))
        );
    }

    @Test
    public void testExecuteFindByVariableSupport() {
        when(webDriver.findElement(any(By.class))).thenAnswer((Answer<WebElement>) invocation -> {
            By select = (By) invocation.getArguments()[0];

            Assertions.assertEquals(select.getClass(), By.ById.class);
            Assertions.assertEquals(select.toString(), By.id("clickMe").toString());
            return element;
        });

        context.setVariable("myId", "clickMe");

        FindElementAction action =  new FindElementAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "${myId}")
                .build();
        action.execute(context);

        Assertions.assertEquals(context.getVariableObject("button"), element);
    }

    @Test
    public void testExecuteFindByValidation() {
        when(element.getText()).thenReturn("Click Me!");
        when(element.getAttribute("type")).thenReturn("submit");
        when(element.getCssValue("color")).thenReturn("red");

        when(webDriver.findElement(any(By.class))).thenAnswer((Answer<WebElement>) invocation -> {
            By select = (By) invocation.getArguments()[0];

            Assertions.assertEquals(select.getClass(), By.ByName.class);
            Assertions.assertEquals(select.toString(), By.name("clickMe").toString());
            return element;
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

        Assertions.assertEquals(context.getVariableObject("button"), element);
    }

    @ParameterizedTest(name = "validationErrorProvider={0}")
    @MethodSource("validationErrorProvider")
    public void testExecuteFindByValidationFailed(String tagName, String text, String attribute, String cssStyle, boolean displayed, boolean enabled, String errorMsg) {
        when(element.getTagName()).thenReturn("button");
        when(element.getText()).thenReturn("Click Me!");
        when(element.getAttribute("type")).thenReturn("submit");
        when(element.getCssValue("color")).thenReturn("red");

        when(webDriver.findElement(any(By.class))).thenAnswer((Answer<WebElement>) invocation -> {
            By select = (By) invocation.getArguments()[0];

            Assertions.assertEquals(select.getClass(), By.ByName.class);
            Assertions.assertEquals(select.toString(), By.name("clickMe").toString());
            return element;
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
            Assertions.fail("Missing exception to to validation error");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().endsWith(errorMsg), e.getMessage());
        }
    }

    static Stream<Arguments> validationErrorProvider() {
        return Stream.of(
                Arguments.of("input", "Click Me!", "submit", "red", true, true, "tag-name expected 'input', but was 'button'"),
                Arguments.of("button", "Click!", "submit", "red", true, true, "text expected 'Click!', but was 'Click Me!'"),
                Arguments.of("button", "Click Me!", "cancel", "red", true, true, "attribute 'type' expected 'cancel', but was 'submit'"),
                Arguments.of("button", "Click Me!", "submit", "red", false, true, "'displayed' expected 'false', but was 'true'"),
                Arguments.of("button", "Click Me!", "submit", "red", true, false, "'enabled' expected 'false', but was 'true'"),
                Arguments.of("button", "Click Me!", "submit", "blue", true, true, "css style 'color' expected 'blue', but was 'red'")
        );
    }

    @Test
    public void testElementNotFound() {
        when(webDriver.findElement(any(By.class))).thenThrow(new NoSuchElementException("no such element: Unable to locate element"));

        FindElementAction action =  new FindElementAction.Builder()
                .browser(seleniumBrowser)
                .element("id", "myButton")
                .build();

        try {
            action.execute(context);
            Assertions.fail("Missing CitrusRuntimeException on missing element");
        } catch (CitrusRuntimeException e) {
            Assertions.assertEquals(e.getMessage(), "Failed to find element 'By.id: myButton' on page");
            Assertions.assertTrue(e.getCause() instanceof NoSuchElementException, e.getCause().getClass().getName());
        }
    }

    @Test
    public void testElementUnsupportedProperty() {
        var e = Assertions.assertThrows(CitrusRuntimeException.class, () -> {
        FindElementAction action =  new FindElementAction.Builder()
                .browser(seleniumBrowser)
                .element("unsupported", "wrong")
                .build();
        action.execute(context);
    });
        Assertions.assertTrue(e.getMessage().matches("Unknown selector type: unsupported"), e.getMessage());
    }
}
