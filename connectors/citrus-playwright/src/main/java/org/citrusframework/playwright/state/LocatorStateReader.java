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

package org.citrusframework.playwright.state;

import com.microsoft.playwright.Locator;

import java.util.List;

import org.citrusframework.playwright.model.BoundingBoxResult;
import org.citrusframework.playwright.model.SecretPatternRedactor;

/**
 * Reads stable, Citrus-facing state from Playwright locators.
 */
public class LocatorStateReader {

    private final SecretPatternRedactor redactor;

    /**
     * Creates a locator state reader with the supplied diagnostics redactor.
     *
     * @param redactor redactor used for text, HTML, and ARIA state
     */
    public LocatorStateReader(SecretPatternRedactor redactor) {
        this.redactor = redactor;
    }

    /**
     * Reads the current locator bounding box as a stable record.
     *
     * @param locator locator to inspect
     * @return bounding box record, or {@code null} when Playwright cannot compute one
     */
    public BoundingBoxResult boundingBox(Locator locator) {
        return BoundingBoxResult.from(locator.boundingBox());
    }

    /**
     * Reads all text contents matched by the locator.
     *
     * @param locator locator to inspect
     * @return sanitized text content list
     */
    public List<String> allTextContents(Locator locator) {
        return sanitizeList(locator.allTextContents());
    }

    /**
     * Reads all rendered inner text values matched by the locator.
     *
     * @param locator locator to inspect
     * @return sanitized inner text list
     */
    public List<String> allInnerTexts(Locator locator) {
        return sanitizeList(locator.allInnerTexts());
    }

    /**
     * Reads locator inner HTML.
     *
     * @param locator locator to inspect
     * @return sanitized inner HTML
     */
    public String innerHtml(Locator locator) {
        return redactor.sanitizeText(locator.innerHTML());
    }

    /**
     * Reads locator outer HTML through Playwright evaluation.
     *
     * @param locator locator to inspect
     * @return sanitized outer HTML
     */
    public String outerHtml(Locator locator) {
        return redactor.sanitizeText(String.valueOf(locator.evaluate("element => element.outerHTML")));
    }

    /**
     * Reads the locator element class list.
     *
     * @param locator locator to inspect
     * @return sanitized CSS class names
     */
    public List<String> cssClasses(Locator locator) {
        return sanitizeList(toStringList(locator.evaluate("element => Array.from(element.classList)")));
    }

    /**
     * Reads a computed CSS property value from the locator element.
     *
     * @param locator locator to inspect
     * @param property CSS property name
     * @return sanitized computed CSS property value
     */
    public String cssValue(Locator locator, String property) {
        return redactor.sanitizeText(String.valueOf(locator.evaluate(
                "(element, property) => getComputedStyle(element).getPropertyValue(property)", property)));
    }

    /**
     * Reads all option text values from a select locator.
     *
     * @param locator select locator to inspect
     * @return sanitized option text values
     */
    public List<String> optionTexts(Locator locator) {
        return sanitizeList(toStringList(locator.evaluate(
                "element => Array.from(element.options || []).map(option => option.textContent || '')")));
    }

    /**
     * Reads the first selected option text from a select locator.
     *
     * @param locator select locator to inspect
     * @return sanitized selected option text
     */
    public String selectedOptionText(Locator locator) {
        return redactor.sanitizeText(String.valueOf(locator.evaluate(
                "element => { const option = element.selectedOptions && element.selectedOptions[0]; return option ? option.textContent : ''; }")));
    }

    /**
     * Reads all selected option values from a select locator.
     *
     * @param locator select locator to inspect
     * @return sanitized selected option values
     */
    public List<String> selectedOptionValues(Locator locator) {
        return sanitizeList(toStringList(locator.evaluate(
                "element => Array.from(element.selectedOptions || []).map(option => option.value)")));
    }

    /**
     * Reads Playwright's ARIA snapshot for a locator.
     *
     * @param locator locator to inspect
     * @return sanitized ARIA snapshot text
     */
    public String ariaSnapshot(Locator locator) {
        return redactor.sanitizeText(locator.ariaSnapshot());
    }

    private List<String> sanitizeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream().map(redactor::sanitizeText).toList();
    }

    private List<String> toStringList(Object value) {
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        return values.stream().map(String::valueOf).toList();
    }
}
