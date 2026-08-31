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
import org.citrusframework.playwright.actions.MouseAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Base class for YAML mouse action wrappers sharing the element property.
 */
abstract class MouseActionSupport<B extends MouseActionSupport<B>> extends AbstractPlaywrightAction.Builder<MouseAction, B> {

    private final MouseAction.Builder delegate;

    protected MouseActionSupport(MouseAction.Command command) {
        this.delegate = new MouseAction.Builder(command);
    }

    @SchemaProperty
    public void setElement(Element element) {
        delegate.locator(element.toLocatorSpec());
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

    protected final MouseAction.Builder delegate() {
        return delegate;
    }

    @Override
    public MouseAction build() {
        return delegate.build();
    }
}
