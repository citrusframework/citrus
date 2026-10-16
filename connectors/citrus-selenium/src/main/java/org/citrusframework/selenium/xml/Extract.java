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

package org.citrusframework.selenium.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.TestActor;
import org.citrusframework.api.actions.selenium.SeleniumElementActionBuilderBase;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.ExtractAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;

@XmlRootElement(name = "extract")
public class Extract extends AbstractSeleniumAction.Builder<ExtractAction, Extract> implements ElementAware {

    private final ExtractAction.Builder delegate = new ExtractAction.Builder();

    private String value;
    private String variable;
    private String attribute;
    private String cssProperty;

    @XmlAttribute
    public void setValue(String value) {
        this.value = value;
    }

    @XmlAttribute
    public void setVariable(String variable) {
        this.variable = variable;
    }

    @XmlAttribute
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    @XmlAttribute(name = "css-property")
    public void setCssProperty(String cssProperty) {
        this.cssProperty = cssProperty;
    }

    @Override
    @XmlElement
    public void setElement(Element element) {
        ElementAware.super.setElement(element);
    }

    @Override
    public SeleniumElementActionBuilderBase<?, ?> getElementBuilder() {
        return delegate;
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
    public Extract browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    @Override
    public ExtractAction build() {
        if (variable != null) {
            delegate.variable(variable);
        }

        if (value != null) {
            delegate.value(value);
        }

        if (attribute != null) {
            delegate.attribute(attribute);
        }

        if (cssProperty != null) {
            delegate.cssValue(cssProperty);
        }

        return delegate.build();
    }
}
