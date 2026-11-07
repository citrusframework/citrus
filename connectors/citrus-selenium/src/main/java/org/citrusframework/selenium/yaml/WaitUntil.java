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
import org.citrusframework.api.actions.selenium.SeleniumElementActionBuilderBase;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.WaitUntilAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.api.yaml.SchemaProperty;

public class WaitUntil extends AbstractSeleniumAction.Builder<WaitUntilAction, WaitUntil> implements ElementAware {

    private final WaitUntilAction.Builder delegate = new WaitUntilAction.Builder();

    @Override
    @SchemaProperty(description = "Target element to wait for, identified by selector or property.")
    public void setElement(Element element) {
        ElementAware.super.setElement(element);
    }

    @SchemaProperty(description = "Maximum wait time in milliseconds.")
    public void setTimeout(long timeout) {
        this.delegate.timeout(timeout);
    }

    @SchemaProperty(description = "Condition to wait for, such as visible, hidden, or a custom page condition.")
    public void setCondition(String condition) {
        this.delegate.condition(condition);
    }

    @SchemaProperty(description = "Condition to wait for; alias for condition.")
    public void setUntil(String condition) {
        this.delegate.condition(condition);
    }

    @SchemaProperty(description = "Expected value required by the wait condition, when applicable.")
    public void setExpected(String expected) {
        this.delegate.expected(expected);
    }

    @Override
    public SeleniumElementActionBuilderBase<?, ?> getElementBuilder() {
        return delegate;
    }

    @Override
    public WaitUntil description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public WaitUntil actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public WaitUntil browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    @Override
    public WaitUntilAction build() {
        return delegate.build();
    }
}
