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

import java.util.List;

import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.InputAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Base class for YAML input action wrappers sharing the element, value, and
 * values properties.
 */
abstract class InputActionSupport<B extends InputActionSupport<B>> extends AbstractPlaywrightAction.Builder<InputAction, B> {

    private final InputAction.Builder delegate;

    protected InputActionSupport(InputAction.Command command) {
        this.delegate = new InputAction.Builder(command);
    }

    @SchemaProperty
    public void setElement(Element element) {
        delegate.locator(element.toLocatorSpec());
    }

    @SchemaProperty
    public void setValue(String value) {
        delegate.value(value);
    }

    @SchemaProperty
    public void setValues(List<String> values) {
        delegate.values(values.toArray(String[]::new));
    }

    @Override
    public B description(String description) {
        delegate.description(description);
        return self;
    }

    @Override
    public B actor(TestActor actor) {
        delegate.actor(actor);
        return self;
    }

    @Override
    public B browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return self;
    }

    protected final InputAction.Builder delegate() {
        return delegate;
    }

    @Override
    public InputAction build() {
        return delegate.build();
    }
}
