/*
 * Copyright 2006-2016 the original author or authors.
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
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Finds element in DOM tree on current page and validates its properties and settings.
 * Test action fails in case no element is found or the validation expectations are not met.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class FindElementAction extends AbstractSeleniumAction {

    /** Optional by used in Java DSL */
    private final By by;

    /** Element selector property */
    private final String property;
    private final String propertyValue;

    /** Optional validation expectations on element */
    private String tagName;
    private Map<String, String> attributes = Collections.emptyMap();
    private Map<String, String> styles = Collections.emptyMap();
    private boolean displayed = true;
    private boolean enabled = true;
    private String text;

    /**
     * Default constructor.
     */
    public FindElementAction(Builder builder) {
        this("find", builder);

        this.attributes = builder.attributes;
        this.styles = builder.styles;
        this.displayed = builder.displayed;
        this.enabled = builder.enabled;
        this.text = builder.text;
        this.tagName = builder.tagName;
    }

    /**
     * Constructor with name.
     * @param name
     */
    protected FindElementAction(String name, ElementActionBuilder<?, ?> builder) {
        super(name, builder);

        this.by = builder.by;
        this.property = builder.property;
        this.propertyValue = builder.propertyValue;
    }

    @Override
    protected final void execute(SeleniumBrowser browser, TestContext context) {
        By findBy = createBy(context);
        WebElement element = browser.getWebDriver().findElement(findBy);

        if (element == null) {
            throw new CitrusRuntimeException(String.format("Failed to find element '%s' on page", findBy));
        }

        validate(element, browser, context);

        execute(element, browser, context);
    }

    /**
     * Validates found web element with expected content.
     * @param element
     * @param browser
     * @param context
     */
    protected void validate(WebElement element, SeleniumBrowser browser, TestContext context) {
        validateElementProperty("tag-name", tagName, element.getTagName(), context);
        validateElementProperty("text", text, element.getText(), context);

        if (!displayed == element.isDisplayed()) {
            throw new ValidationException(String.format("Selenium web element validation failed, " +
                "property 'displayed' expected '%s', but was '%s'", displayed, element.isDisplayed()));
        }

        if (!enabled == element.isEnabled()) {
            throw new ValidationException(String.format("Selenium web element validation failed, " +
                "property 'enabled' expected '%s', but was '%s'", enabled, element.isEnabled()));
        }

        for (Map.Entry<String, String> attributeEntry : attributes.entrySet()) {
            validateElementProperty(String.format("attribute '%s'", attributeEntry.getKey()), attributeEntry.getValue(), element.getAttribute(attributeEntry.getKey()), context);
        }

        for (Map.Entry<String, String> styleEntry : styles.entrySet()) {
            validateElementProperty(String.format("css style '%s'", styleEntry.getKey()), styleEntry.getValue(), element.getCssValue(styleEntry.getKey()), context);
        }
    }

    /**
     * Validates web element property value with validation matcher support.
     * @param propertyName
     * @param controlValue
     * @param resultValue
     * @param context
     */
    private void validateElementProperty(String propertyName, String controlValue, String resultValue, TestContext context) {
        if (StringUtils.hasText(controlValue)) {
            String control = context.replaceDynamicContentInString(controlValue);

            if (ValidationMatcherUtils.isValidationMatcherExpression(control)) {
                ValidationMatcherUtils.resolveValidationMatcher("payload", resultValue, control, context);
            } else {
                if (!control.equals(resultValue)) {
                    throw new ValidationException(String.format("Selenium web element validation failed, %s expected '%s', but was '%s'", propertyName, control, resultValue));
                }
            }
        }
    }

    /**
     * Subclasses may override this method in order to add element actions.
     * @param element
     * @param browser
     * @param context
     */
    protected void execute(WebElement element, SeleniumBrowser browser, TestContext context) {
        if (StringUtils.hasText(element.getTagName())) {
            context.setVariable(element.getTagName(), element);
        }
    }

    /**
     * Create by selector from type information.
     * @return
     */
    protected By createBy(TestContext context) {
        if (by != null) {
            return by;
        }

        switch (property) {
            case "id":
                return By.id(context.replaceDynamicContentInString(propertyValue));
            case "class-name":
                return By.className(context.replaceDynamicContentInString(propertyValue));
            case "link-text":
                return By.linkText(context.replaceDynamicContentInString(propertyValue));
            case "css-selector":
                return By.cssSelector(context.replaceDynamicContentInString(propertyValue));
            case "name":
                return By.name(context.replaceDynamicContentInString(propertyValue));
            case "tag-name":
                return By.tagName(context.replaceDynamicContentInString(propertyValue));
            case "xpath":
                return By.xpath(context.replaceDynamicContentInString(propertyValue));
        }

        throw new CitrusRuntimeException("Unknown selector type: " + property);
    }

    /**
     * Gets the property.
     *
     * @return
     */
    public String getProperty() {
        return property;
    }

    /**
     * Gets the propertyValue.
     *
     * @return
     */
    public String getPropertyValue() {
        return propertyValue;
    }

    /**
     * Gets the tagName.
     *
     * @return
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * Gets the attributes.
     *
     * @return
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Gets the styles.
     *
     * @return
     */
    public Map<String, String> getStyles() {
        return styles;
    }

    /**
     * Gets the displayed.
     *
     * @return
     */
    public boolean isDisplayed() {
        return displayed;
    }

    /**
     * Gets the enabled.
     *
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the text.
     *
     * @return
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the by.
     *
     * @return
     */
    public By getBy() {
        return by;
    }

    /**
     * Action builder.
     */
    public static class Builder extends ElementActionBuilder<FindElementAction, Builder> {

        private final Map<String, String> attributes = new HashMap<>();
        private final Map<String, String> styles = new HashMap<>();
        private boolean displayed = true;
        private boolean enabled = true;
        private String text;
        private String tagName;

        /**
         * Add text validation.
         * @param text
         * @return
         */
        public Builder text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Add tag name validation.
         * @param tagName
         * @return
         */
        public Builder tagName(String tagName) {
            this.tagName = tagName;
            return this;
        }

        /**
         * Add attribute validation.
         * @param name
         * @param value
         * @return
         */
        public Builder attribute(String name, String value) {
            this.attributes.put(name, value);
            return this;
        }

        /**
         * Add css style validation.
         * @param name
         * @param value
         * @return
         */
        public Builder style(String name, String value) {
            this.styles.put(name, value);
            return this;
        }

        /**
         * Add enabled validation.
         * @param enabled
         * @return
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Add displayed validation.
         * @param displayed
         * @return
         */
        public Builder displayed(boolean displayed) {
            this.displayed = displayed;
            return this;
        }

        @Override
        public FindElementAction build() {
            return new FindElementAction(this);
        }
    }

    /**
     * Abstract element based action builder.
     * @param <T>
     * @param <B>
     */
    public static abstract class ElementActionBuilder<T extends FindElementAction, B extends ElementActionBuilder<T, B>> extends AbstractSeleniumAction.Builder<T, B> {

        protected By by;
        protected String property;
        protected String propertyValue;

        public B element(By by) {
            this.by = by;
            return self;
        }

        public B element(String property, String propertyValue) {
            this.property = property;
            this.propertyValue = propertyValue;
            return self;
        }
    }
}
