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
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.api.actions.selenium.SeleniumElementActionBuilderBase;
import org.citrusframework.api.actions.selenium.SeleniumFindElementActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.model.ElementLocator;
import org.citrusframework.util.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Finds element in DOM tree on current page and validates its properties and settings.
 * Test action fails in case no element is found or the validation expectations are not met.
 *
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

        if (!requiresElementBeforeExecute()) {
            executeWithoutElement(findBy, browser, context);
            return;
        }

        WebElement element;
        try {
            element = browser.getWebDriver().findElement(findBy);
        } catch (NoSuchElementException e) {
            throw new CitrusRuntimeException(String.format("Failed to find element '%s' on page", findBy), e);
        }

        validate(element, browser, context);

        execute(element, browser, context);
    }

    /**
     * Checks if this action requires the web element to be present on the page before execution.
     * By default, actions look up the element and fail in case it is not present. Subclasses may
     * override this method in case they need to perform logic while the element is not yet present,
     * e.g. waiting for the element to appear in the DOM tree.
     * @return
     */
    protected boolean requiresElementBeforeExecute() {
        return true;
    }

    /**
     * Subclasses may override this method in order to perform logic without an element present on
     * the page. This method is only invoked in case {@link #requiresElementBeforeExecute()} returns
     * {@code false}.
     * @param by
     * @param browser
     * @param context
     */
    protected void executeWithoutElement(By by, SeleniumBrowser browser, TestContext context) {
        throw new CitrusRuntimeException("Element is required for this action to execute");
    }

    /**
     * Validates found web element with expected content.
     * @param element
     * @param browser
     * @param context
     */
    protected void validate(WebElement element, SeleniumBrowser browser, TestContext context) {
        ValueAssertions.assertValue("tag-name", tagName, element.getTagName(), context);
        ValueAssertions.assertValue("text", text, element.getText(), context);

        if (!displayed == element.isDisplayed()) {
            throw new ValidationException(String.format("Selenium web element validation failed, " +
                "property 'displayed' expected '%s', but was '%s'", displayed, element.isDisplayed()));
        }

        if (!enabled == element.isEnabled()) {
            throw new ValidationException(String.format("Selenium web element validation failed, " +
                "property 'enabled' expected '%s', but was '%s'", enabled, element.isEnabled()));
        }

        for (Map.Entry<String, String> attributeEntry : attributes.entrySet()) {
            ValueAssertions.assertValue(String.format("attribute '%s'", attributeEntry.getKey()), attributeEntry.getValue(), element.getAttribute(attributeEntry.getKey()), context);
        }

        for (Map.Entry<String, String> styleEntry : styles.entrySet()) {
            ValueAssertions.assertValue(String.format("css style '%s'", styleEntry.getKey()), styleEntry.getValue(), element.getCssValue(styleEntry.getKey()), context);
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

        if (property == null) {
            return null;
        }

        return ElementLocator.of(property, propertyValue).resolve(context);
    }

    /**
     * Gets the property.
     */
    public String getProperty() {
        return property;
    }

    /**
     * Gets the propertyValue.
     */
    public String getPropertyValue() {
        return propertyValue;
    }

    /**
     * Gets the tagName.
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * Gets the attributes.
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Gets the styles.
     */
    public Map<String, String> getStyles() {
        return styles;
    }

    /**
     * Gets the displayed.
     */
    public boolean isDisplayed() {
        return displayed;
    }

    /**
     * Gets the enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the text.
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the by.
     */
    public By getBy() {
        return by;
    }

    /**
     * Action builder.
     */
    public static class Builder extends ElementActionBuilder<FindElementAction, Builder>
            implements SeleniumFindElementActionBuilder<FindElementAction, Builder> {

        private final Map<String, String> attributes = new HashMap<>();
        private final Map<String, String> styles = new HashMap<>();
        private boolean displayed = true;
        private boolean enabled = true;
        private String text;
        private String tagName;

        @Override
        public Builder text(String text) {
            this.text = text;
            return this;
        }

        @Override
        public Builder tagName(String tagName) {
            this.tagName = tagName;
            return this;
        }

        @Override
        public Builder attribute(String name, String value) {
            this.attributes.put(name, value);
            return this;
        }

        @Override
        public Builder style(String name, String value) {
            this.styles.put(name, value);
            return this;
        }

        @Override
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        @Override
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
    public static abstract class ElementActionBuilder<T extends FindElementAction, B extends ElementActionBuilder<T, B>>
            extends AbstractSeleniumAction.Builder<T, B> implements SeleniumElementActionBuilderBase<T, B> {

        protected By by;
        protected String property;
        protected String propertyValue;

        public B element(By by) {
            this.by = by;
            return self;
        }

        @Override
        public B element(Object o) {
            if (o instanceof By byElement) {
                this.by = byElement;
            } else {
                throw new CitrusRuntimeException(("Invalid element identifier object, " +
                        "expected By object but got %s").formatted(o.getClass().getName()));
            }

            return self;
        }

        @Override
        public B element(String property, String propertyValue) {
            this.property = property;
            this.propertyValue = propertyValue;
            return self;
        }
    }
}
