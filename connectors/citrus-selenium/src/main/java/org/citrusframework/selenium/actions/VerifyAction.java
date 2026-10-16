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

import org.citrusframework.api.actions.selenium.SeleniumVerifyActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.model.ElementLocator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Verifies the current page url, title, element state or element value.
 * @since 5.1
 */
public class VerifyAction extends AbstractSeleniumAction {

    private static final String CHECK_NAMES = "url, title, present, absent, displayed, hidden, enabled, disabled, selected, not-selected, text, value, attribute, css-value, count";

    /** The check to perform */
    private final String check;

    /** The expected value */
    private final String expected;

    /** The attribute or css style property name */
    private final String property;

    /** The element locator */
    private final ElementLocator element;

    public VerifyAction(Builder builder) {
        super("verify", builder);

        this.check = builder.check;
        this.expected = builder.expected;
        this.property = builder.property;
        this.element = builder.element;
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        if (check == null) {
            throw new CitrusRuntimeException(String.format("Missing verify check - use one of %s", CHECK_NAMES));
        }

        Check checkToRun = Check.from(check);

        switch (checkToRun) {
            case URL -> ValueAssertions.assertValue("url", expected, browser.getWebDriver().getCurrentUrl(), context);
            case TITLE -> ValueAssertions.assertValue("title", expected, browser.getWebDriver().getTitle(), context);
            default -> executeElementCheck(checkToRun, browser, context);
        }
    }

    private void executeElementCheck(Check checkToRun, SeleniumBrowser browser, TestContext context) {
        By by = resolveElement(context);

        List<WebElement> elements = browser.getWebDriver().findElements(by);
        WebElement elementToCheck = elements.isEmpty() ? null : elements.get(0);

        switch (checkToRun) {
            case PRESENT -> assertElementState(by, "present", elementToCheck != null ? "present" : "absent", context);
            case ABSENT -> assertElementState(by, "absent", elementToCheck != null ? "present" : "absent", context);
            case DISPLAYED -> assertElementState(by, "displayed", elementToCheck == null ? "absent" : (elementToCheck.isDisplayed() ? "displayed" : "hidden"), context);
            case HIDDEN -> assertElementState(by, "hidden", elementToCheck == null || !elementToCheck.isDisplayed() ? "hidden" : "displayed", context);
            case ENABLED -> assertElementState(by, "enabled", elementToCheck == null ? "absent" : (elementToCheck.isEnabled() ? "enabled" : "disabled"), context);
            case DISABLED -> assertElementState(by, "disabled", elementToCheck == null ? "absent" : (elementToCheck.isEnabled() ? "enabled" : "disabled"), context);
            case SELECTED -> assertElementState(by, "selected", elementToCheck == null ? "absent" : (elementToCheck.isSelected() ? "selected" : "not selected"), context);
            case NOT_SELECTED -> assertElementState(by, "not selected", elementToCheck == null ? "absent" : (elementToCheck.isSelected() ? "selected" : "not selected"), context);
            case TEXT -> ValueAssertions.assertValue("text", expected, requireElement(elementToCheck, by).getText(), context);
            case VALUE -> ValueAssertions.assertValue("value", expected, requireElement(elementToCheck, by).getDomProperty("value"), context);
            case ATTRIBUTE -> {
                WebElement found = requireElement(elementToCheck, by);
                String actual = found.getDomAttribute(property);
                if (actual == null) {
                    actual = found.getAttribute(property);
                }
                ValueAssertions.assertValue(String.format("attribute '%s'", property), expected, actual, context);
            }
            case CSS_VALUE -> ValueAssertions.assertValue(String.format("css style '%s'", property), expected, requireElement(elementToCheck, by).getCssValue(property), context);
            case COUNT -> ValueAssertions.assertValue("count", expected, String.valueOf(elements.size()), context);
            default -> throw new CitrusRuntimeException(String.format("Unsupported verify check '%s' - use one of %s", check, CHECK_NAMES));
        }
    }

    private By resolveElement(TestContext context) {
        if (element == null) {
            throw new CitrusRuntimeException(String.format("Missing element selector for verify check '%s'", check));
        }

        return element.resolve(context);
    }

    private WebElement requireElement(WebElement elementToCheck, By by) {
        if (elementToCheck == null) {
            throw new CitrusRuntimeException(String.format("Failed to find element '%s' on page", by));
        }

        return elementToCheck;
    }

    private void assertElementState(By by, String expectedState, String actualState, TestContext context) {
        ValueAssertions.assertValue(String.format("element '%s'", by), expectedState, actualState, context);
    }

    public String getCheck() {
        return check;
    }

    public String getExpected() {
        return expected;
    }

    public String getProperty() {
        return property;
    }

    public ElementLocator getElement() {
        return element;
    }

    private enum Check {
        URL("url"),
        TITLE("title"),
        PRESENT("present"),
        ABSENT("absent"),
        DISPLAYED("displayed"),
        HIDDEN("hidden"),
        ENABLED("enabled"),
        DISABLED("disabled"),
        SELECTED("selected"),
        NOT_SELECTED("not-selected"),
        TEXT("text"),
        VALUE("value"),
        ATTRIBUTE("attribute"),
        CSS_VALUE("css-value"),
        COUNT("count");

        private final String name;

        Check(String name) {
            this.name = name;
        }

        private static Check from(String check) {
            for (Check candidate : values()) {
                if (candidate.name.equals(check)) {
                    return candidate;
                }
            }

            throw new CitrusRuntimeException(String.format("Unsupported verify check '%s' - use one of %s", check, CHECK_NAMES));
        }
    }

    public static class Builder extends AbstractSeleniumAction.Builder<VerifyAction, Builder> implements SeleniumVerifyActionBuilder<VerifyAction, Builder> {

        private String check;
        private String expected;
        private String property;
        private ElementLocator element;

        @Override
        public Builder check(String check) {
            this.check = check;
            return this;
        }

        @Override
        public Builder expected(String expected) {
            this.expected = expected;
            return this;
        }

        @Override
        public Builder url(String expected) {
            this.check = "url";
            this.expected = expected;
            return this;
        }

        @Override
        public Builder title(String expected) {
            this.check = "title";
            this.expected = expected;
            return this;
        }

        @Override
        public Builder present() {
            this.check = "present";
            return this;
        }

        @Override
        public Builder absent() {
            this.check = "absent";
            return this;
        }

        @Override
        public Builder displayed() {
            this.check = "displayed";
            return this;
        }

        @Override
        public Builder hidden() {
            this.check = "hidden";
            return this;
        }

        @Override
        public Builder enabled() {
            this.check = "enabled";
            return this;
        }

        @Override
        public Builder disabled() {
            this.check = "disabled";
            return this;
        }

        @Override
        public Builder selected() {
            this.check = "selected";
            return this;
        }

        @Override
        public Builder notSelected() {
            this.check = "not-selected";
            return this;
        }

        @Override
        public Builder text(String expected) {
            this.check = "text";
            this.expected = expected;
            return this;
        }

        @Override
        public Builder value(String expected) {
            this.check = "value";
            this.expected = expected;
            return this;
        }

        @Override
        public Builder attribute(String name, String expected) {
            this.check = "attribute";
            this.property = name;
            this.expected = expected;
            return this;
        }

        @Override
        public Builder cssValue(String property, String expected) {
            this.check = "css-value";
            this.property = property;
            this.expected = expected;
            return this;
        }

        @Override
        public Builder count(String expected) {
            this.check = "count";
            this.expected = expected;
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
        public VerifyAction build() {
            return new VerifyAction(this);
        }
    }
}
