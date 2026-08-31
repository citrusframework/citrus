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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.model.LocatorSpec;

/**
 * Locator element model shared by all element aware Playwright XML actions.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {})
public class Element {

    @XmlAttribute
    private String css;

    @XmlAttribute
    private String xpath;

    @XmlAttribute
    private String text;

    @XmlAttribute
    private String role;

    @XmlAttribute
    private String name;

    @XmlAttribute(name = "test-id")
    private String testId;

    @XmlAttribute
    private Integer nth;

    @XmlAttribute
    private Boolean first;

    @XmlAttribute
    private Boolean last;

    public void setCss(String css) {
        this.css = css;
    }

    public String getCss() {
        return css;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getXpath() {
        return xpath;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getTestId() {
        return testId;
    }

    public void setNth(Integer nth) {
        this.nth = nth;
    }

    public Integer getNth() {
        return nth;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    public Boolean getFirst() {
        return first;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public Boolean getLast() {
        return last;
    }

    /**
     * Converts the element properties into a {@link LocatorSpec}.
     * @return locator specification derived from this element.
     */
    public LocatorSpec toLocatorSpec() {
        LocatorSpec spec;
        if (css != null) {
            spec = LocatorSpec.css(css);
        } else if (xpath != null) {
            spec = LocatorSpec.xpath(xpath);
        } else if (text != null) {
            spec = LocatorSpec.text(text);
        } else if (role != null) {
            spec = LocatorSpec.role(role);
            if (name != null) {
                spec.name(name);
            }
        } else if (testId != null) {
            spec = LocatorSpec.testId(testId);
        } else {
            throw new CitrusRuntimeException("Missing Playwright locator selector - please provide one of css, xpath, text, role, or test-id");
        }

        if (nth != null) {
            spec.nth(nth);
        }
        if (Boolean.TRUE.equals(first)) {
            spec.first();
        }
        if (Boolean.TRUE.equals(last)) {
            spec.last();
        }
        return spec;
    }
}
