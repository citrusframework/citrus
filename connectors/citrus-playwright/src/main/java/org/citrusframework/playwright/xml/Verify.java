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

package org.citrusframework.playwright.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.TestActor;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.VerifyAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.util.StringUtils;

/**
 * Verifies page, locator, storage, observability, or download state.
 */
@XmlRootElement(name = "verify")
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

    @XmlAttribute(name = "check")
    public void setCheck(String check) {
        this.check = check;
    }

    @XmlAttribute
    public void setExpected(String expected) {
        this.expected = expected;
    }

    @XmlAttribute(name = "attribute")
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    @XmlAttribute(name = "count")
    public void setCount(Integer count) {
        this.count = count;
    }

    @XmlAttribute(name = "css-property")
    public void setCssProperty(String property) {
        this.property = property;
    }

    @XmlAttribute(name = "key")
    public void setKey(String key) {
        this.key = key;
    }

    @XmlAttribute(name = "frame-selector")
    public void setFrameSelector(String frameSelector) {
        this.frameSelector = frameSelector;
    }

    @XmlAttribute(name = "x")
    public void setX(Double x) {
        this.x = x;
    }

    @XmlAttribute(name = "y")
    public void setY(Double y) {
        this.y = y;
    }

    @XmlAttribute(name = "width")
    public void setWidth(Double width) {
        this.width = width;
    }

    @XmlAttribute(name = "height")
    public void setHeight(Double height) {
        this.height = height;
    }

    @XmlElement
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
