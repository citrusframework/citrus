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
import org.citrusframework.playwright.actions.VerifyAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.util.StringUtils;

/**
 * Verifies page, locator, storage, observability, or download state.
 */
public class Verify extends AbstractPlaywrightAction.Builder<VerifyAction, Verify> {

    private final VerifyAction.Builder delegate = new VerifyAction.Builder();

    private String check;
    private String expected;
    private String attribute;
    private Integer count;
    private String property;
    private String key;
    private String frameSelector;
    private Double x;
    private Double y;
    private Double width;
    private Double height;

    @SchemaProperty(description = "Verification check name such as visible, text, url, value, attribute, count, or storage-local.")
    public void setCheck(String check) {
        this.check = check;
    }

    @SchemaProperty
    public void setExpected(String expected) {
        this.expected = expected;
    }

    @SchemaProperty(description = "Attribute name for the attribute check.")
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    @SchemaProperty(description = "Expected count for the count and page-count checks.")
    public void setCount(Integer count) {
        this.count = count;
    }

    @SchemaProperty(description = "CSS property name for the css-value check.")
    public void setCssProperty(String property) {
        this.property = property;
    }

    @SchemaProperty(description = "Storage or cookie key for the storage and cookie checks.")
    public void setKey(String key) {
        this.key = key;
    }

    @SchemaProperty(description = "Frame selector for the frame-content-contains check.")
    public void setFrameSelector(String frameSelector) {
        this.frameSelector = frameSelector;
    }

    @SchemaProperty(description = "Expected left coordinate for bounding-box checks.")
    public void setX(Double x) {
        this.x = x;
    }

    @SchemaProperty(description = "Expected top coordinate for bounding-box checks.")
    public void setY(Double y) {
        this.y = y;
    }

    @SchemaProperty(description = "Expected width for bounds checks.")
    public void setWidth(Double width) {
        this.width = width;
    }

    @SchemaProperty(description = "Expected height for bounds checks.")
    public void setHeight(Double height) {
        this.height = height;
    }

    @SchemaProperty
    public void setElement(Element element) {
        delegate.locator(element.toLocatorSpec());
    }

    @Override
    public Verify description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Verify actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Verify browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public VerifyAction build() {
        applyCheck();
        return delegate.build();
    }

    private void applyCheck() {
        delegate.check(StringUtils.hasText(check) ? VerifyAction.Check.fromName(check) : VerifyAction.Check.VISIBLE)
                .expected(expected)
                .attributeName(attribute)
                .expectedCount(count)
                .cssProperty(property)
                .stateKey(key)
                .frameSelector(frameSelector)
                .x(x)
                .y(y)
                .width(width)
                .height(height);
    }
}
