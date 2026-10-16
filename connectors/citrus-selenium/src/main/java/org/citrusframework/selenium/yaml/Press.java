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
import org.citrusframework.selenium.actions.PressKeysAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;

public class Press extends AbstractSeleniumAction.Builder<PressKeysAction, Press> implements ElementAware {

    private final PressKeysAction.Builder delegate = new PressKeysAction.Builder();

    @SchemaProperty
    public void setKeys(String keys) {
        this.delegate.keys(keys);
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
    public Press description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Press actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Press browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    @Override
    public PressKeysAction build() {
        return delegate.build();
    }
}
