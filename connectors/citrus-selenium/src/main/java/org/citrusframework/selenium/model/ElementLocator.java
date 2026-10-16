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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.openqa.selenium.By;

/**
 * Locates a web element on the current page using a selector property and value. The locator
 * resolves the selector into a Selenium {@link By} instance during test execution so dynamic
 * content such as test variables in the selector value are resolved properly.
 *
 * Supported selector properties are {@code id}, {@code name}, {@code class-name},
 * {@code link-text}, {@code partial-link-text}, {@code css-selector}, {@code tag-name}
 * and {@code xpath}.
 *
 * @since 5.1
 */
public class ElementLocator {

    private final By by;
    private final String property;
    private final String propertyValue;

    private ElementLocator(By by, String property, String propertyValue) {
        this.by = by;
        this.property = property;
        this.propertyValue = propertyValue;
    }

    /**
     * Creates a locator from a pre-built {@link By} instance. The {@code By} instance is used as is.
     * @param by
     * @return
     */
    public static ElementLocator of(By by) {
        return new ElementLocator(by, null, null);
    }

    /**
     * Creates a locator from a selector property and value.
     * @param property
     * @param propertyValue
     * @return
     */
    public static ElementLocator of(String property, String propertyValue) {
        return new ElementLocator(null, property, propertyValue);
    }

    /**
     * Resolves the locator into a Selenium {@link By} instance. Any dynamic content in the
     * selector value gets resolved with the given test context.
     * @param context
     * @return
     */
    public By resolve(TestContext context) {
        if (by != null) {
            return by;
        }

        String value = context.replaceDynamicContentInString(propertyValue);

        return switch (property) {
            case "id" -> By.id(value);
            case "name" -> By.name(value);
            case "class-name" -> By.className(value);
            case "link-text" -> By.linkText(value);
            case "partial-link-text" -> By.partialLinkText(value);
            case "css-selector" -> By.cssSelector(value);
            case "tag-name" -> By.tagName(value);
            case "xpath" -> By.xpath(value);
            default -> throw new CitrusRuntimeException("Unknown selector type: " + property);
        };
    }

    /**
     * Gets the by.
     */
    public By getBy() {
        return by;
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
}
