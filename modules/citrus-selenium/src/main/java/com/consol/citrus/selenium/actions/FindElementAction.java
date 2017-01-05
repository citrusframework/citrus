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

package com.consol.citrus.selenium.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.validation.matcher.ValidationMatcherUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Finds element in DOM tree on current page and validates its properties and settings.
 * Test action fails in case no element is found or the validation expectations are not met.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class FindElementAction extends AbstractSeleniumAction {

    /** Optional by used in Java DSL */
    private By by;

    /** Element selector property */
    private String property;
    private String propertyValue;

    /** Optional validation expectations on element */
    private String tagName;
    private Map<String, String> attributes = new HashMap<>();
    private Map<String, String> styles = new HashMap<>();
    private boolean displayed = true;
    private boolean enabled = true;
    private String text;

    /**
     * Default constructor.
     */
    public FindElementAction() {
        super("find");
    }

    /**
     * Constructor with name.
     * @param name
     */
    public FindElementAction(String name) {
        super(name);
    }

    @Override
    protected final void execute(SeleniumBrowser browser, TestContext context) {
        WebElement element = browser.getWebDriver().findElement(createBy(context));

        if (element == null) {
            throw new CitrusRuntimeException(String.format("Failed to find element '%s' on page", property + "=" + propertyValue));
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

        Assert.isTrue(displayed == element.isDisplayed(), String.format("Selenium web element validation failed, " +
                "property 'displayed' expected '%s', but was '%s'", displayed, element.isDisplayed()));
        Assert.isTrue(enabled == element.isEnabled(), String.format("Selenium web element validation failed, " +
                "property 'enabled' expected '%s', but was '%s'", enabled, element.isEnabled()));

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
                Assert.isTrue(control.equals(resultValue), String.format("Selenium web element validation failed, %s expected '%s', but was '%s'", propertyName, control, resultValue));
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
     * Sets the property.
     *
     * @param property
     */
    public void setProperty(String property) {
        this.property = property;
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
     * Sets the propertyValue.
     *
     * @param propertyValue
     */
    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
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
     * Sets the tagName.
     *
     * @param tagName
     */
    public void setTagName(String tagName) {
        this.tagName = tagName;
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
     * Sets the attributes.
     *
     * @param attributes
     */
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
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
     * Sets the styles.
     *
     * @param styles
     */
    public void setStyles(Map<String, String> styles) {
        this.styles = styles;
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
     * Sets the displayed.
     *
     * @param displayed
     */
    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
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
     * Sets the enabled.
     *
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
     * Sets the text.
     *
     * @param text
     */
    public void setText(String text) {
        this.text = text;
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
     * Sets the by.
     *
     * @param by
     */
    public void setBy(By by) {
        this.by = by;
    }
}
