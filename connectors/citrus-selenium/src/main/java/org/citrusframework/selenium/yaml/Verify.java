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

package org.citrusframework.selenium.yaml;

import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.actions.selenium.SeleniumElementActionBuilderBase;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.VerifyAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;

public class Verify extends AbstractSeleniumAction.Builder<VerifyAction, Verify> implements ElementAware {

    private final VerifyAction.Builder delegate = new VerifyAction.Builder();

    private String check;
    private String expected;
    private String attribute;
    private String cssProperty;

    @SchemaProperty
    public void setCheck(String check) {
        this.check = check;
    }

    @SchemaProperty
    public void setExpected(String expected) {
        this.expected = expected;
    }

    @SchemaProperty
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    @SchemaProperty
    public void setCssProperty(String cssProperty) {
        this.cssProperty = cssProperty;
    }

    @Override
    @SchemaProperty
    public void setElement(Element element) {
        ElementAware.super.setElement(element);
    }

    @Override
    public SeleniumElementActionBuilderBase<?, ?> getElementBuilder() {
        return delegate;
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
    public Verify browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    @Override
    public VerifyAction build() {
        if (check != null) {
            delegate.check(check);
        }

        if (expected != null) {
            delegate.expected(expected);
        }

        if (attribute != null) {
            delegate.attribute(attribute, expected);
        }

        if (cssProperty != null) {
            delegate.cssValue(cssProperty, expected);
        }

        return delegate.build();
    }
}
