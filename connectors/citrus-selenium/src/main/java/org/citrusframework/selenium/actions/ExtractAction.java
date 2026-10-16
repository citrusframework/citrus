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

import java.util.List;

import org.citrusframework.api.actions.selenium.SeleniumExtractActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.model.ElementLocator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Extracts the current page url, title, page source or element value and stores it in a test variable.
 * @since 5.1
 */
public class ExtractAction extends AbstractSeleniumAction {

    private static final String VALUE_NAMES = "url, title, page-source, text, value, attribute, css-value, count";

    /** The value to extract */
    private final String value;

    /** The variable name to store the extracted value */
    private final String variable;

    /** The attribute or css style property name */
    private final String property;

    /** The element locator */
    private final ElementLocator element;

    public ExtractAction(Builder builder) {
        super("extract", builder);

        this.value = builder.value;
        this.variable = builder.variable;
        this.property = builder.property;
        this.element = builder.element;
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        if (variable == null) {
            throw new CitrusRuntimeException("Missing variable name for extract action");
        }

        if (value == null) {
            throw new CitrusRuntimeException(String.format("Missing extract value - use one of %s", VALUE_NAMES));
        }

        Value valueToExtract = Value.from(value);

        String extracted;

        switch (valueToExtract) {
            case URL -> extracted = browser.getWebDriver().getCurrentUrl();
            case TITLE -> extracted = browser.getWebDriver().getTitle();
            case PAGE_SOURCE -> extracted = browser.getWebDriver().getPageSource();
            default -> extracted = extractElementValue(valueToExtract, browser, context);
        }

        context.setVariable(variable, extracted);
    }

    private String extractElementValue(Value valueToExtract, SeleniumBrowser browser, TestContext context) {
        By by = resolveElement(context);

        List<WebElement> elements = browser.getWebDriver().findElements(by);
        WebElement elementToExtract = elements.isEmpty() ? null : elements.get(0);

        return switch (valueToExtract) {
            case TEXT -> requireElement(elementToExtract, by).getText();
            case VALUE -> requireElement(elementToExtract, by).getDomProperty("value");
            case ATTRIBUTE -> {
                WebElement found = requireElement(elementToExtract, by);
                String actual = found.getDomAttribute(property);
                if (actual == null) {
                    actual = found.getAttribute(property);
                }
                yield actual;
            }
            case CSS_VALUE -> requireElement(elementToExtract, by).getCssValue(property);
            case COUNT -> String.valueOf(elements.size());
            default -> throw new CitrusRuntimeException(String.format("Unsupported extract value '%s' - use one of %s", value, VALUE_NAMES));
        };
    }

    private By resolveElement(TestContext context) {
        if (element == null) {
            throw new CitrusRuntimeException(String.format("Missing element selector for extract value '%s'", value));
        }

        return element.resolve(context);
    }

    private WebElement requireElement(WebElement elementToExtract, By by) {
        if (elementToExtract == null) {
            throw new CitrusRuntimeException(String.format("Failed to find element '%s' on page", by));
        }

        return elementToExtract;
    }

    public String getValue() {
        return value;
    }

    public String getVariable() {
        return variable;
    }

    public String getProperty() {
        return property;
    }

    public ElementLocator getElement() {
        return element;
    }

    private enum Value {
        URL("url"),
        TITLE("title"),
        PAGE_SOURCE("page-source"),
        TEXT("text"),
        VALUE("value"),
        ATTRIBUTE("attribute"),
        CSS_VALUE("css-value"),
        COUNT("count");

        private final String name;

        Value(String name) {
            this.name = name;
        }

        private static Value from(String value) {
            for (Value candidate : values()) {
                if (candidate.name.equals(value)) {
                    return candidate;
                }
            }

            throw new CitrusRuntimeException(String.format("Unsupported extract value '%s' - use one of %s", value, VALUE_NAMES));
        }
    }

    public static class Builder extends AbstractSeleniumAction.Builder<ExtractAction, Builder> implements SeleniumExtractActionBuilder<ExtractAction, Builder> {

        private String value;
        private String variable;
        private String property;
        private ElementLocator element;

        @Override
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        @Override
        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        @Override
        public Builder url() {
            this.value = "url";
            return this;
        }

        @Override
        public Builder title() {
            this.value = "title";
            return this;
        }

        @Override
        public Builder pageSource() {
            this.value = "page-source";
            return this;
        }

        @Override
        public Builder text() {
            this.value = "text";
            return this;
        }

        @Override
        public Builder value() {
            this.value = "value";
            return this;
        }

        @Override
        public Builder attribute(String name) {
            this.value = "attribute";
            this.property = name;
            return this;
        }

        @Override
        public Builder cssValue(String property) {
            this.value = "css-value";
            this.property = property;
            return this;
        }

        @Override
        public Builder count() {
            this.value = "count";
            return this;
        }

        @Override
        public Builder element(Object element) {
            if (element instanceof By byElement) {
                this.element = ElementLocator.of(byElement);
            } else {
                throw new CitrusRuntimeException(("Invalid element identifier object, expected By object but got %s").formatted(element.getClass().getName()));
            }
            return this;
        }

        @Override
        public Builder element(String property, String propertyValue) {
            this.element = ElementLocator.of(property, propertyValue);
            return this;
        }

        @Override
        public ExtractAction build() {
            return new ExtractAction(this);
        }
    }
}
