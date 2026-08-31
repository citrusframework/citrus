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

package org.citrusframework.playwright.yaml;

import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.ExtractAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.util.StringUtils;

/**
 * Extracts page, locator, storage, observability, or download values into a
 * Citrus test variable.
 */
public class Extract extends AbstractPlaywrightAction.Builder<ExtractAction, Extract> {

    private final ExtractAction.Builder delegate = new ExtractAction.Builder();

    private String value = "text";
    private String variable;
    private String attribute;
    private String property;
    private String key;
    private String frameSelector;

    @SchemaProperty(description = "Extraction value such as text (default), value, count, url, title, or storage-local.")
    public void setValue(String value) {
        this.value = value;
    }

    @SchemaProperty
    public void setVariable(String variable) {
        this.variable = variable;
    }

    @SchemaProperty(description = "Attribute name for the attribute extraction.")
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    @SchemaProperty(description = "CSS property name for the css-value extraction.")
    public void setCssProperty(String property) {
        this.property = property;
    }

    @SchemaProperty(description = "Storage or cookie key for the storage and cookie extractions.")
    public void setKey(String key) {
        this.key = key;
    }

    @SchemaProperty(description = "Frame selector for the frame-content extraction.")
    public void setFrameSelector(String frameSelector) {
        this.frameSelector = frameSelector;
    }

    @SchemaProperty
    public void setElement(Element element) {
        delegate.locator(element.toLocatorSpec());
    }

    @Override
    public Extract description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Extract actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Extract browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public ExtractAction build() {
        applyValue();
        if (StringUtils.hasText(variable)) {
            delegate.variable(variable);
        }
        return delegate.build();
    }

    private void applyValue() {
        if (!StringUtils.hasText(value)) {
            delegate.text();
            return;
        }

        switch (value.trim().toLowerCase().replace('-', '_')) {
            case "text" -> delegate.text();
            case "value", "input_value" -> delegate.value();
            case "attribute" -> delegate.attribute(attribute);
            case "count" -> delegate.count();
            case "all_text_contents" -> delegate.allTextContents();
            case "all_inner_texts" -> delegate.allInnerTexts();
            case "inner_html" -> delegate.innerHtml();
            case "outer_html" -> delegate.outerHtml();
            case "bounding_box" -> delegate.boundingBox();
            case "css_classes" -> delegate.cssClasses();
            case "css_value" -> delegate.cssValue(property);
            case "option_texts" -> delegate.optionTexts();
            case "selected_option_text" -> delegate.selectedOptionText();
            case "selected_option_values" -> delegate.selectedOptionValues();
            case "aria_snapshot" -> delegate.ariaSnapshot();
            case "url", "page_url" -> delegate.url();
            case "title", "page_title" -> delegate.title();
            case "page_count" -> delegate.pageCount();
            case "frame_content" -> delegate.frameContent(frameSelector);
            case "storage_local" -> delegate.storageLocal(key);
            case "storage_session" -> delegate.storageSession(key);
            case "cookie" -> delegate.cookie(key);
            case "console_messages" -> delegate.consoleMessages();
            case "network_records" -> delegate.networkRecords();
            case "download_metadata" -> delegate.downloadMetadata();
            default -> throw new IllegalArgumentException("Unsupported Playwright extract value: " + value);
        }
    }
}
